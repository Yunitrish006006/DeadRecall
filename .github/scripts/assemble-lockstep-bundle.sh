#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly REPOSITORY_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
readonly MANIFEST="${REPOSITORY_ROOT}/openspec/changes/safe-multi-repo-modularization/lockstep-manifest.json"

usage() {
    cat <<'USAGE'
Usage: assemble-lockstep-bundle.sh [--rollback] --output <mods-directory> --artifact <id=jar> [--artifact <id=jar> ...]

Copies exactly the module artifacts pinned in lockstep-manifest.json into the
given mods directory. Every supplied artifact must match its pinned SHA-512;
missing, duplicate, unknown, or unpinned artifacts fail the assembly.
USAGE
}

rollback=false
output=""
declare -A supplied=()

while [[ $# -gt 0 ]]; do
    case "$1" in
        --rollback) rollback=true; shift ;;
        --output) output="${2:-}"; shift 2 ;;
        --artifact)
            spec="${2:-}"
            [[ "${spec}" == *=* ]] || { printf 'Artifact must use id=jar form: %s\n' "${spec}" >&2; exit 2; }
            id="${spec%%=*}"
            path="${spec#*=}"
            [[ -n "${id}" && -n "${path}" && -z "${supplied[${id}]+x}" ]] || {
                printf 'Artifact id is missing or duplicated: %s\n' "${id}" >&2; exit 2;
            }
            supplied["${id}"]="${path}"
            shift 2
            ;;
        --help|-h) usage; exit 0 ;;
        *) printf 'Unknown argument: %s\n' "$1" >&2; usage >&2; exit 2 ;;
    esac
done

[[ -n "${output}" ]] || { printf 'Missing --output.\n' >&2; usage >&2; exit 2; }
[[ -f "${MANIFEST}" ]] || { printf 'Missing lockstep manifest: %s\n' "${MANIFEST}" >&2; exit 1; }

mode="current"
if [[ "${rollback}" == true ]]; then mode="rollback"; fi

declare -A expected=()
while IFS='|' read -r graph id checksum; do
    [[ "${graph}" == "${mode}" ]] || continue
    expected["${id}"]="${checksum}"
done < <(
    awk '
        /"rollback"[[:space:]]*:/ { graph = "rollback" }
        /"id"[[:space:]]*:/ {
            value = $0
            sub(/^.*"id"[[:space:]]*:[[:space:]]*"/, "", value)
            sub(/".*$/, "", value)
            id = value
        }
        /"sha512"[[:space:]]*:/ {
            value = $0
            sub(/^.*"sha512"[[:space:]]*:[[:space:]]*"/, "", value)
            sub(/".*$/, "", value)
            print (graph == "rollback" ? "rollback" : "current") "|" id "|" value
        }
    ' "${MANIFEST}"
)

[[ ${#expected[@]} -gt 0 ]] || { printf 'Manifest has no %s artifact graph.\n' "${mode}" >&2; exit 1; }

for id in "${!expected[@]}"; do
    [[ -n "${supplied[${id}]+x}" ]] || { printf 'Missing pinned artifact: %s\n' "${id}" >&2; exit 1; }
done
for id in "${!supplied[@]}"; do
    [[ -n "${expected[${id}]+x}" ]] || { printf 'Artifact is not pinned in the %s graph: %s\n' "${mode}" "${id}" >&2; exit 1; }
done

mkdir -p "${output}"
for id in "${!expected[@]}"; do
    jar="${supplied[${id}]}"
    [[ -f "${jar}" ]] || { printf 'Artifact does not exist: %s\n' "${jar}" >&2; exit 1; }
    actual="$(sha512sum "${jar}" | awk '{print $1}')"
    [[ "${actual}" == "${expected[${id}]}" ]] || {
        printf 'SHA-512 mismatch for %s.\n' "${id}" >&2; exit 1;
    }
    cp "${jar}" "${output}/$(basename "${jar}")"
done
cp "${MANIFEST}" "${output}/lockstep-manifest.json"
printf 'Assembled the %s lockstep graph in %s.\n' "${mode}" "${output}"
