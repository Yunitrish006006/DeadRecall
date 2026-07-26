#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly REPOSITORY_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
readonly MANIFEST="${REPOSITORY_ROOT}/openspec/changes/safe-multi-repo-modularization/lockstep-manifest.json"
readonly ASSEMBLER="${SCRIPT_DIR}/assemble-lockstep-bundle.sh"
readonly SURFACE_CHECK="${SCRIPT_DIR}/check-assembled-compatibility-surface.sh"
readonly REQUIRED_JAVA_MAJOR=25
readonly ISOLATED_SERVER_PORT=25570

usage() {
    cat <<'USAGE'
Usage: verify-lockstep-bundle.sh --output <new-directory> --java <java-command> \
  --deadrecall <jar> --fabric-api <jar> --server-launcher <jar> \
  --source <id=repository> [--source <id=repository> ...] \
  --artifact <id=jar> [--artifact <id=jar> ...] \
  [--extra-mod <jar> ...] [--world <existing-world-directory>] \
  [--completion-marker <file>] [--environment <KEY=VALUE> ...]

Validates the exact source commits and SHA-512-pinned artifacts in the current
lockstep graph, assembles them with DeadRecall and Fabric API, then starts an
isolated Java 25 Dedicated Server on port 25570. The output directory must not
exist so stale mods, worlds, logs and crash reports cannot make the evidence
pass accidentally.
USAGE
}

require_command() {
    command -v "$1" >/dev/null 2>&1 || {
        printf 'Required command is unavailable: %s\n' "$1" >&2
        exit 1
    }
}

extract_json_value() {
    local line="$1"
    local key="$2"
    local value="${line#*\"${key}\": \"}"
    value="${value%%\"*}"
    printf '%s\n' "${value}"
}

declare -A supplied_artifacts=()
declare -A supplied_sources=()
output=""
java_command=""
deadrecall_jar=""
fabric_api_jar=""
server_launcher_jar=""
world_source=""
completion_marker=""
declare -a extra_mods=()
declare -A runtime_environment=()

while [[ $# -gt 0 ]]; do
    case "$1" in
        --output) output="${2:-}"; shift 2 ;;
        --java) java_command="${2:-}"; shift 2 ;;
        --deadrecall) deadrecall_jar="${2:-}"; shift 2 ;;
        --fabric-api) fabric_api_jar="${2:-}"; shift 2 ;;
        --server-launcher) server_launcher_jar="${2:-}"; shift 2 ;;
        --artifact)
            spec="${2:-}"
            [[ "${spec}" == *=* ]] || { printf 'Artifact must use id=jar form: %s\n' "${spec}" >&2; exit 2; }
            id="${spec%%=*}"
            path="${spec#*=}"
            [[ -n "${id}" && -n "${path}" && -z "${supplied_artifacts[${id}]+x}" ]] || {
                printf 'Artifact id is missing or duplicated: %s\n' "${id}" >&2; exit 2;
            }
            supplied_artifacts["${id}"]="${path}"
            shift 2
            ;;
        --extra-mod)
            path="${2:-}"
            [[ -f "${path}" ]] || { printf 'Extra mod must be a file: %s\n' "${path}" >&2; exit 2; }
            extra_mods+=("${path}")
            shift 2
            ;;
        --world)
            world_source="${2:-}"
            [[ -d "${world_source}" ]] || { printf 'World source must be a directory: %s\n' "${world_source}" >&2; exit 2; }
            shift 2
            ;;
        --completion-marker)
            completion_marker="${2:-}"
            [[ -n "${completion_marker}" ]] || { printf 'Completion marker path is required.\n' >&2; exit 2; }
            [[ ! -e "${completion_marker}" ]] || { printf 'Completion marker already exists: %s\n' "${completion_marker}" >&2; exit 2; }
            shift 2
            ;;
        --environment)
            spec="${2:-}"
            [[ "${spec}" == *=* ]] || { printf 'Environment must use KEY=VALUE form: %s\n' "${spec}" >&2; exit 2; }
            key="${spec%%=*}"
            value="${spec#*=}"
            [[ "${key}" =~ ^[A-Z_][A-Z0-9_]*$ ]] || {
                printf 'Environment key must use uppercase letters, digits and underscores: %s\n' "${key}" >&2
                exit 2
            }
            [[ "${value}" != *$'\n'* && -z "${runtime_environment[${key}]+x}" ]] || {
                printf 'Environment value is invalid or key is duplicated: %s\n' "${key}" >&2
                exit 2
            }
            runtime_environment["${key}"]="${value}"
            shift 2
            ;;
        --source)
            spec="${2:-}"
            [[ "${spec}" == *=* ]] || { printf 'Source must use id=repository form: %s\n' "${spec}" >&2; exit 2; }
            id="${spec%%=*}"
            path="${spec#*=}"
            [[ -n "${id}" && -n "${path}" && -z "${supplied_sources[${id}]+x}" ]] || {
                printf 'Source id is missing or duplicated: %s\n' "${id}" >&2; exit 2;
            }
            supplied_sources["${id}"]="${path}"
            shift 2
            ;;
        --help|-h) usage; exit 0 ;;
        *) printf 'Unknown argument: %s\n' "$1" >&2; usage >&2; exit 2 ;;
    esac
done

[[ -n "${output}" && -n "${java_command}" && -n "${deadrecall_jar}" && -n "${fabric_api_jar}" && -n "${server_launcher_jar}" ]] || {
    usage >&2
    exit 2
}
[[ ! -e "${output}" ]] || { printf 'Output directory must not exist: %s\n' "${output}" >&2; exit 2; }
[[ -f "${MANIFEST}" ]] || { printf 'Missing lockstep manifest: %s\n' "${MANIFEST}" >&2; exit 1; }
[[ -f "${deadrecall_jar}" && -f "${fabric_api_jar}" && -f "${server_launcher_jar}" ]] || {
    printf 'DeadRecall, Fabric API and server launcher inputs must be files.\n' >&2
    exit 1
}

for required_command in awk cp grep git mkdir mkfifo rm sleep; do
    require_command "${required_command}"
done
command -v "${java_command}" >/dev/null 2>&1 || {
    printf 'Java command is unavailable: %s\n' "${java_command}" >&2
    exit 1
}
java_major="$("${java_command}" -version 2>&1 | awk -F '"' '/version "/ { split($2, parts, "."); print parts[1]; exit }')"
[[ "${java_major}" == "${REQUIRED_JAVA_MAJOR}" ]] || {
    printf 'Lockstep verification requires Java %s, found Java %s from %s.\n' \
        "${REQUIRED_JAVA_MAJOR}" "${java_major:-unknown}" "${java_command}" >&2
    exit 1
}

declare -A expected_commits=()
declare -A expected_versions=()
current_id=""
current_version=""
current_commit=""
while IFS= read -r line || [[ -n "${line}" ]]; do
    [[ "${line}" == *'"rollback"'* ]] && break
    if [[ "${line}" == *'"id"'* ]]; then
        current_id="$(extract_json_value "${line}" id)"
        current_version=""
        current_commit=""
    elif [[ -n "${current_id}" && "${line}" == *'"version"'* ]]; then
        current_version="$(extract_json_value "${line}" version)"
    elif [[ -n "${current_id}" && "${line}" == *'"sourceCommit"'* ]]; then
        current_commit="$(extract_json_value "${line}" sourceCommit)"
    elif [[ -n "${current_id}" && "${line}" == *'"sha512"'* ]]; then
        [[ -n "${current_version}" && -n "${current_commit}" ]] || {
            printf 'Current manifest module is missing a version or source commit: %s\n' "${current_id}" >&2
            exit 1
        }
        expected_versions["${current_id}"]="${current_version}"
        expected_commits["${current_id}"]="${current_commit}"
        current_id=""
    fi
done < "${MANIFEST}"

[[ ${#expected_versions[@]} -gt 0 ]] || { printf 'Manifest has no current module graph.\n' >&2; exit 1; }

for id in "${!expected_commits[@]}"; do
    [[ -n "${supplied_sources[${id}]+x}" ]] || { printf 'Missing source repository for %s.\n' "${id}" >&2; exit 1; }
    [[ -n "${supplied_artifacts[${id}]+x}" ]] || { printf 'Missing artifact for %s.\n' "${id}" >&2; exit 1; }
    source_root="${supplied_sources[${id}]}"
    git -C "${source_root}" rev-parse --is-inside-work-tree >/dev/null 2>&1 || {
        printf 'Source is not a Git checkout: %s\n' "${source_root}" >&2
        exit 1
    }
    actual_commit="$(git -C "${source_root}" rev-parse HEAD)"
    [[ "${actual_commit}" == "${expected_commits[${id}]}" ]] || {
        printf 'Source commit mismatch for %s: expected %s, found %s.\n' \
            "${id}" "${expected_commits[${id}]}" "${actual_commit}" >&2
        exit 1
    }
done

for id in "${!supplied_sources[@]}"; do
    [[ -n "${expected_commits[${id}]+x}" ]] || { printf 'Source is not in the current graph: %s\n' "${id}" >&2; exit 1; }
done

mkdir -p "${output}/server"
assembly_arguments=(--output "${output}/server/mods")
for id in "${!expected_versions[@]}"; do
    assembly_arguments+=(--artifact "${id}=${supplied_artifacts[${id}]}")
done
bash "${ASSEMBLER}" "${assembly_arguments[@]}"
cp "${deadrecall_jar}" "${fabric_api_jar}" "${output}/server/mods/"
for extra_mod in "${extra_mods[@]}"; do
    cp "${extra_mod}" "${output}/server/mods/"
done
cp "${server_launcher_jar}" "${output}/server/fabric-server-launch.jar"
printf 'eula=true\n' > "${output}/server/eula.txt"
cat > "${output}/server/server.properties" <<PROPERTIES
online-mode=false
server-port=${ISOLATED_SERVER_PORT}
level-name=world
spawn-protection=0
view-distance=4
simulation-distance=4
PROPERTIES

if [[ -n "${world_source}" ]]; then
    cp -a "${world_source}" "${output}/server/world"
fi

surface_arguments=(--jar "${deadrecall_jar}" --source-root "${REPOSITORY_ROOT}")
for id in "${!expected_versions[@]}"; do
    surface_arguments+=(--jar "${supplied_artifacts[${id}]}" --source-root "${supplied_sources[${id}]}")
done
bash "${SURFACE_CHECK}" "${surface_arguments[@]}"

server_log="${output}/server/logs/latest.log"
server_console="${output}/server/console.log"
server_stdin="${output}/server/console.stdin"
mkfifo "${server_stdin}"
runtime_environment_arguments=(env)
for key in "${!runtime_environment[@]}"; do
    runtime_environment_arguments+=("${key}=${runtime_environment[${key}]}")
done
(
    cd "${output}/server"
    exec "${runtime_environment_arguments[@]}" "${java_command}" -Xmx1G -jar fabric-server-launch.jar nogui
) < "${server_stdin}" > "${server_console}" 2>&1 &
server_pid=$!

# Keep stdin open until startup succeeds. Minecraft treats an immediate EOF as
# a stop request, which otherwise makes a non-interactive CI run flaky.
exec 3> "${server_stdin}"
# A fresh Fabric server launcher must download and unpack the Minecraft server
# before it can emit `Done`.  Keep this bounded, but allow a cold CI workspace
# enough time to complete that one-time setup.
deadline=$((SECONDS + 180))
reached_done=false
while (( SECONDS < deadline )); do
    if [[ -f "${server_log}" ]] && grep -Fq 'Done (' "${server_log}"; then
        reached_done=true
        break
    fi
    if ! kill -0 "${server_pid}" 2>/dev/null; then
        break
    fi
    sleep 1
done

if [[ "${reached_done}" == true && -n "${completion_marker}" ]]; then
    completion_deadline=$((SECONDS + 120))
    while (( SECONDS < completion_deadline )); do
        [[ -f "${completion_marker}" ]] && break
        if ! kill -0 "${server_pid}" 2>/dev/null; then
            break
        fi
        sleep 1
    done
    [[ -f "${completion_marker}" ]] || {
        kill -TERM "${server_pid}" 2>/dev/null || true
        printf 'Assembled Dedicated Server did not write completion marker: %s\n' "${completion_marker}" >&2
        cat "${server_console}" >&2
        exit 1
    }
fi

if [[ "${reached_done}" == true ]] && kill -0 "${server_pid}" 2>/dev/null; then
    printf 'stop\n' >&3
elif [[ "${reached_done}" != true ]]; then
    kill -TERM "${server_pid}" 2>/dev/null || true
fi
exec 3>&-

set +e
wait "${server_pid}"
server_status=$?
set -e
rm -f "${server_stdin}"

if [[ "${reached_done}" != true ]]; then
    printf 'Assembled Dedicated Server did not reach Done (exit %s).\n' "${server_status}" >&2
    cat "${server_console}" >&2
    exit 1
fi
[[ ! -d "${output}/server/crash-reports" ]] || {
    printf 'Assembled Dedicated Server produced a crash report.\n' >&2
    find "${output}/server/crash-reports" -maxdepth 1 -type f -print >&2
    exit 1
}

for id in "${!expected_versions[@]}"; do
    match_count="$(grep -F -c -- "- ${id} ${expected_versions[${id}]}" "${server_log}" || true)"
    [[ "${match_count}" == 1 ]] || {
        printf 'Expected exactly one loaded %s %s entry, found %s.\n' \
            "${id}" "${expected_versions[${id}]}" "${match_count}" >&2
        exit 1
    }
done

for initializer in \
    'TotemCore API 1.0 initialized without gameplay registration' \
    'TotemRemnant initialized without Nexus dependency' \
    'TotemDiscordBridge initialized' \
    'TotemAutomata 0.1.1 cutover authority activated without Cognition dependency' \
    'TotemNexus 0.1.1 cutover authority activated'; do
    match_count="$(grep -F -c -- "${initializer}" "${server_log}" || true)"
    [[ "${match_count}" == 1 ]] || {
        printf 'Expected exactly one initializer message, found %s: %s\n' "${match_count}" "${initializer}" >&2
        exit 1
    }
done

grep -Fq 'All dimensions are saved' "${server_log}" || {
    printf 'Assembled Dedicated Server did not save all dimensions after reaching Done.\n' >&2
    exit 1
}
printf 'Pinned compatibility bundle reached Done with one live initializer per Totem module.\n'
