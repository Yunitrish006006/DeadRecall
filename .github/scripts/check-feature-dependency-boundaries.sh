#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly REPOSITORY_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
readonly EXCEPTIONS_FILE="${REPOSITORY_ROOT}/openspec/changes/safe-multi-repo-modularization/dependency-boundary-exceptions.txt"

require_command() {
    command -v "$1" >/dev/null 2>&1 || {
        printf 'Required command is unavailable: %s\n' "$1" >&2
        exit 1
    }
}

for required_command in awk find sort; do
    require_command "${required_command}"
done

[[ -f "${EXCEPTIONS_FILE}" ]] || {
    printf 'Missing dependency-boundary exception register: %s\n' "${EXCEPTIONS_FILE}" >&2
    exit 1
}

violations_file="$(mktemp)"
trap 'rm -f -- "${violations_file}"' EXIT

cd "${REPOSITORY_ROOT}"

find src/main/java src/client/java -type f -name '*.java' -print0 |
    xargs -0 awk '
        function owner_for_source(path) {
            if (path ~ /\/DiscordBridge\.java$/ || path ~ /\/discord\//) return "discord-bridge"
            if (path ~ /\/api\/death\// || path ~ /\/death\// || path ~ /\/integration\/trinkets\//) return "remnant"
            if (path ~ /\/item\/copper\//) return "automata"
            if (path ~ /\/space\//) return "nexus"
            return ""
        }

        function owner_for_import(import_name) {
            if (import_name ~ /^com\.adaptor\.deadrecall\.discord\./) return "discord-bridge"
            if (import_name ~ /^com\.adaptor\.deadrecall\.(api\.death|death|integration\.trinkets)\./) return "remnant"
            if (import_name ~ /^com\.adaptor\.deadrecall\.item\.copper\./) return "automata"
            if (import_name ~ /^com\.adaptor\.deadrecall\.space\./) return "nexus"
            return ""
        }

        FNR == 1 {
            source_owner = owner_for_source(FILENAME)
        }

        source_owner != "" && match($0, /^[[:space:]]*import[[:space:]]+(com\.adaptor\.deadrecall\.[A-Za-z0-9_.]+);/, match_parts) {
            import_owner = owner_for_import(match_parts[1])
            if (import_owner != "" && import_owner != source_owner) {
                print FILENAME "|" source_owner "|" import_owner "|" match_parts[1]
            }
        }
    ' > "${violations_file}"

unexpected_violations=0
while IFS='|' read -r source_file source_owner import_owner import_name; do
    [[ -n "${source_file}" ]] || continue

    exception_key="${source_file}|${source_owner}|${import_owner}"
    if ! awk -F '|' -v key="${exception_key}" '
        /^[[:space:]]*(#|$)/ { next }
        $1 "|" $2 "|" $3 == key { found = 1 }
        END { exit !found }
    ' "${EXCEPTIONS_FILE}"; then
        printf 'Forbidden direct feature import: %s imports %s (%s -> %s)\n' \
            "${source_file}" "${import_name}" "${source_owner}" "${import_owner}" >&2
        unexpected_violations=1
    fi
done < <(sort -u "${violations_file}")

if [[ "${unexpected_violations}" -ne 0 ]]; then
    printf '\nUse a versioned Core API, lifecycle event, or optional adapter instead.\n' >&2
    printf 'Temporary exceptions require an explicit OpenSpec task and removal condition.\n' >&2
    exit 1
fi

printf 'Feature dependency boundaries contain no untracked direct imports.\n'
