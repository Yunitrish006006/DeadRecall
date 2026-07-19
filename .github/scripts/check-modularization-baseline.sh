#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly REPOSITORY_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
readonly BASELINE_FILE="${REPOSITORY_ROOT}/openspec/changes/safe-multi-repo-modularization/compatibility-surface.txt"

collect_surface() {
    cd "${REPOSITORY_ROOT}"

    {
        find src/main/resources/assets/deadrecall src/main/resources/data/deadrecall \
            -type f \
            | sed 's#^src/main/resources/#resource #'

        (rg -o --no-filename 'deadrecall:[a-z0-9_./-]+' \
            src/main/java src/client/java src/main/resources || true) \
            | sed 's/^/identifier /'

        (rg -o --no-filename \
            'Identifier\.fromNamespaceAndPath\("deadrecall",[[:space:]]*"[a-z0-9_./-]+"\)' \
            src/main/java src/client/java || true) \
            | sed -E 's/.*"deadrecall",[[:space:]]*"([a-z0-9_./-]+)".*/identifier deadrecall:\1/'
    } | LC_ALL=C sort -u
}

if [[ "${1:-}" == "--print" ]]; then
    collect_surface
    exit 0
fi

[[ -f "${BASELINE_FILE}" ]] || {
    printf 'Missing modularization compatibility baseline: %s\n' "${BASELINE_FILE}" >&2
    printf 'Generate candidate content with: %s --print\n' "$0" >&2
    exit 1
}

current_surface="$(mktemp)"
trap 'rm -f -- "${current_surface}"' EXIT
collect_surface > "${current_surface}"

if ! diff -u "${BASELINE_FILE}" "${current_surface}"; then
    printf '\nCompatibility surface changed.\n' >&2
    printf 'Do not update the baseline until the owning module, migration path, and assembled bundle coverage are documented.\n' >&2
    exit 1
fi

printf 'Modularization compatibility surface matches the committed baseline.\n'
