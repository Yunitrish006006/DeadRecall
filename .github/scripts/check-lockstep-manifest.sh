#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly MANIFEST="${SCRIPT_DIR}/../../openspec/changes/safe-multi-repo-modularization/lockstep-manifest.json"

[[ -f "${MANIFEST}" ]] || { printf 'Missing lockstep manifest: %s\n' "${MANIFEST}" >&2; exit 1; }

awk '
    BEGIN { module_count = 0; rollback_count = 0; in_rollback = 0; valid = 1 }
    /"rollback"[[:space:]]*:/ { in_rollback = 1 }
    /"id"[[:space:]]*:[[:space:]]*"[a-z0-9-]+"/ {
        if (in_rollback) rollback_count++; else module_count++
    }
    /"version"[[:space:]]*:/ && /SNAPSHOT|latest|\+|\*/ {
        printf "Lockstep manifest contains a non-immutable version: %s\n", $0 > "/dev/stderr"; valid = 0
    }
    /"sha512"[[:space:]]*:/ {
        value = $0
        sub(/^.*"sha512"[[:space:]]*:[[:space:]]*"/, "", value)
        sub(/".*$/, "", value)
        if (value !~ /^[0-9a-f]{128}$/) {
            printf "Lockstep manifest contains an invalid SHA-512: %s\n", value > "/dev/stderr"; valid = 0
        }
    }
    END {
        if (module_count == 0 || rollback_count == 0) {
            print "Lockstep manifest must pin current and rollback module graphs." > "/dev/stderr"; valid = 0
        }
        exit !valid
    }
' "${MANIFEST}"

printf 'Lockstep manifest contains immutable current and rollback module pins.\n'
