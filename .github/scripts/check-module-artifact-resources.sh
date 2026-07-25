#!/usr/bin/env bash

set -euo pipefail

usage() {
    cat <<'USAGE'
Usage: check-module-artifact-resources.sh --source-root <repository-root> --jar <module-jar>

Ensures every DeadRecall compatibility asset or data resource owned by a
module source tree is present byte-for-byte in its publishable JAR. This
catches Loom remap configurations that leave resources in the development JAR
but remove them from the artifact used by the compatibility bundle.
USAGE
}

require_command() {
    command -v "$1" >/dev/null 2>&1 || {
        printf 'Required command is unavailable: %s\n' "$1" >&2
        exit 1
    }
}

source_root=''
module_jar=''
while [[ $# -gt 0 ]]; do
    case "$1" in
        --source-root) source_root="${2:?Missing source root}"; shift 2 ;;
        --jar) module_jar="${2:?Missing module JAR}"; shift 2 ;;
        --help|-h) usage; exit 0 ;;
        *) printf 'Unknown argument: %s\n' "$1" >&2; usage >&2; exit 2 ;;
    esac
done

[[ -d "${source_root}" && -f "${module_jar}" ]] || {
    usage >&2
    exit 2
}

for required_command in cmp find grep jar unzip; do
    require_command "${required_command}"
done

declare -a source_files=()
for resource_root in "${source_root}/src/main/resources" "${source_root}/src/client/resources"; do
    [[ -d "${resource_root}" ]] || continue
    while IFS= read -r -d '' source_file; do
        source_files+=("${source_file}")
    done < <(find "${resource_root}" -type f \( -path '*/assets/deadrecall/*' -o -path '*/data/deadrecall/*' \) -print0)
done

if [[ ${#source_files[@]} -eq 0 ]]; then
    printf 'No owned DeadRecall compatibility resources to verify in %s.\n' "${source_root}"
    exit 0
fi

for source_file in "${source_files[@]}"; do
    resource_path="${source_file#*/resources/}"
    if ! jar tf "${module_jar}" | grep -Fqx "${resource_path}"; then
        printf 'Module artifact is missing owned resource: %s\n' "${resource_path}" >&2
        exit 1
    fi

    if ! cmp -s "${source_file}" <(unzip -p "${module_jar}" "${resource_path}"); then
        printf 'Module artifact resource differs from source: %s\n' "${resource_path}" >&2
        exit 1
    fi
done

printf 'Module artifact retains all %d owned compatibility resources.\n' "${#source_files[@]}"
