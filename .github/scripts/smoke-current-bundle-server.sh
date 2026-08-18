#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
readonly MODULE_DIR="${DEADRECALL_MODULE_DIR:-${ROOT}/standalone-modules}"
readonly CORE_JAR="${DEADRECALL_CORE_JAR:-${MODULE_DIR}/totem-core-0.6.0.jar}"
readonly LOG_FILE="${ROOT}/server-smoke.log"

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
set +e
(
    sleep 90
    echo stop
) | timeout --signal=TERM --kill-after=30s 180s \
    ./gradlew \
      -PtotemCoreJar="${CORE_JAR}" \
      -PbundleModuleDirectory="${MODULE_DIR}" \
      runServer --no-daemon --stacktrace \
    2>&1 | tee "${LOG_FILE}"
status=${PIPESTATUS[1]}
set -e

if [[ "${status}" -ne 0 ]]; then
    printf 'Dedicated server smoke exited with status %s.\n' "${status}" >&2
    exit "${status}"
fi

grep -E 'Done \([^)]*\)!' "${LOG_FILE}" >/dev/null || {
    echo 'Dedicated server never reached the Done state.' >&2
    exit 1
}

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
