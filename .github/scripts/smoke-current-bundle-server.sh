#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
readonly MODULE_DIR="${DEADRECALL_MODULE_DIR:-${ROOT}/standalone-modules}"
readonly CORE_JAR="${DEADRECALL_CORE_JAR:-${MODULE_DIR}/totem-core-0.6.0.jar}"
readonly LOG_FILE="${ROOT}/server-smoke.log"
readonly STARTUP_TIMEOUT_SECONDS=180
readonly STOP_TIMEOUT_SECONDS=30

cd "${ROOT}"

test -d "${MODULE_DIR}"
test -f "${CORE_JAR}"
test "$(find "${MODULE_DIR}" -maxdepth 1 -type f -name '*.jar' | wc -l | tr -d ' ')" -eq 11

rm -rf run
mkdir -p run
printf 'eula=true\n' > run/eula.txt
cat > run/server.properties <<'PROPERTIES'
online-mode=false
spawn-protection=0
view-distance=4
simulation-distance=4
motd=DeadRecall lockstep smoke
PROPERTIES

rm -f "${LOG_FILE}"
fifo="$(mktemp -u "${ROOT}/.server-smoke-input.XXXXXX")"
mkfifo "${fifo}"
exec 3<>"${fifo}"
rm -f "${fifo}"

./gradlew \
  -PtotemCoreJar="${CORE_JAR}" \
  -PbundleModuleDirectory="${MODULE_DIR}" \
  runServer --no-daemon --stacktrace \
  <&3 >"${LOG_FILE}" 2>&1 &
server_pid=$!

done_seen=false
for ((second = 1; second <= STARTUP_TIMEOUT_SECONDS; second++)); do
    if grep -Eq 'Done \([^)]*\)!' "${LOG_FILE}" 2>/dev/null; then
        done_seen=true
        printf 'stop\n' >&3 || true
        break
    fi
    if ! kill -0 "${server_pid}" 2>/dev/null; then
        break
    fi
    sleep 1
done

if [[ "${done_seen}" != true ]]; then
    set +e
    wait "${server_pid}"
    status=$?
    set -e
    exec 3>&-
    cat "${LOG_FILE}"
    if [[ "${status}" -eq 0 ]]; then
        echo 'Dedicated server exited before reaching the Done state.' >&2
        exit 1
    fi
    printf 'Dedicated server failed before reaching Done (status %s).\n' "${status}" >&2
    exit "${status}"
fi

for ((second = 1; second <= STOP_TIMEOUT_SECONDS; second++)); do
    if ! kill -0 "${server_pid}" 2>/dev/null; then
        break
    fi
    sleep 1
done

if kill -0 "${server_pid}" 2>/dev/null; then
    echo 'Dedicated server did not stop after console stop; sending TERM.' >&2
    kill -TERM "${server_pid}" 2>/dev/null || true
    sleep 5
fi
if kill -0 "${server_pid}" 2>/dev/null; then
    echo 'Dedicated server still running after TERM; sending KILL.' >&2
    kill -KILL "${server_pid}" 2>/dev/null || true
fi

set +e
wait "${server_pid}"
status=$?
set -e
exec 3>&-
cat "${LOG_FILE}"

if [[ "${status}" -ne 0 ]]; then
    printf 'Dedicated server smoke exited with status %s after reaching Done.\n' "${status}" >&2
    exit "${status}"
fi

for module_id in \
    totem-core \
    totem-remnant \
    totem-discord-bridge \
    totem-automata \
    totem-alchemy \
    totem-enchanting \
    totem-excavation \
    totem-locksmith \
    totem-vanilla-tweaks \
    totem-nexus \
    totem-villagers; do
    grep -F "${module_id}" "${LOG_FILE}" >/dev/null || {
        printf 'Server smoke log did not mention required module %s.\n' "${module_id}" >&2
        exit 1
    }
done

printf 'DeadRecall current lockstep dedicated-server smoke passed.\n'
