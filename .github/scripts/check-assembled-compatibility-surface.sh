#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly REPOSITORY_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
readonly BASELINE_FILE="${REPOSITORY_ROOT}/openspec/changes/safe-multi-repo-modularization/compatibility-surface.txt"
readonly DELEGATED_SURFACE_FILE="${REPOSITORY_ROOT}/openspec/changes/safe-multi-repo-modularization/delegated-compatibility-surface.txt"

usage() {
    cat <<'USAGE'
Usage: check-assembled-compatibility-surface.sh \
  --jar <assembled-module-jar> [--jar <assembled-module-jar> ...] \
  --source-root <repository-root> [--source-root <repository-root> ...] \
  [--allow-delegated-surface]

Checks an assembled compatibility graph against the committed DeadRecall
compatibility surface.  Resources are read from the supplied JARs, so missing
or duplicate assets/data paths fail the check.  Identifiers are collected from
the supplied source roots until every extracted module has a generated,
machine-readable registry inventory.

Use --allow-delegated-surface only when inspecting DeadRecall's root JAR by
itself. Entries listed in delegated-compatibility-surface.txt are then owned
by a separately verified exact compatibility bundle. Do not use this option
for a complete assembled graph.
USAGE
}

require_command() {
    command -v "$1" >/dev/null 2>&1 || {
        printf 'Required command is unavailable: %s\n' "$1" >&2
        exit 1
    }
}

declare -a jars=()
declare -a source_roots=()
allow_delegated_surface=false
while [[ $# -gt 0 ]]; do
    case "$1" in
        --jar) jars+=("${2:?Missing JAR path}"); shift 2 ;;
        --source-root) source_roots+=("${2:?Missing source root}"); shift 2 ;;
        --allow-delegated-surface) allow_delegated_surface=true; shift ;;
        --help|-h) usage; exit 0 ;;
        *) printf 'Unknown argument: %s\n' "$1" >&2; usage >&2; exit 2 ;;
    esac
done

[[ ${#jars[@]} -gt 0 && ${#source_roots[@]} -gt 0 ]] || {
    usage >&2
    exit 2
}
[[ -f "${BASELINE_FILE}" ]] || { printf 'Missing compatibility baseline: %s\n' "${BASELINE_FILE}" >&2; exit 1; }
if [[ "${allow_delegated_surface}" == true ]]; then
    [[ -f "${DELEGATED_SURFACE_FILE}" ]] || {
        printf 'Missing delegated compatibility surface: %s\n' "${DELEGATED_SURFACE_FILE}" >&2
        exit 1
    }
fi

for required_command in awk comm find jar jq mktemp sort uniq unzip xargs; do
    require_command "${required_command}"
done

temporary_directory="$(mktemp -d)"
trap 'rm -rf -- "${temporary_directory}"' EXIT
bundle_resources_all="${temporary_directory}/bundle-resources-all"
bundle_identifiers="${temporary_directory}/bundle-identifiers"

for module_jar in "${jars[@]}"; do
    [[ -f "${module_jar}" ]] || { printf 'Assembled module JAR is missing: %s\n' "${module_jar}" >&2; exit 1; }
    jar tf "${module_jar}" | awk '
        (/^assets\/deadrecall\// || /^data\/deadrecall\//) && $0 !~ /\/$/ { print "resource " $0 }
    ' >> "${bundle_resources_all}"
done

duplicate_resources="${temporary_directory}/duplicate-resources"
LC_ALL=C sort "${bundle_resources_all}" | uniq -d > "${duplicate_resources}"

# Locale resources need to stay packaged with the standalone Nexus visual
# GameTest while the compatibility bundle still owns the complete legacy
# locale files for fallback.  The three shared files are safe only when every
# overlapping translation key has exactly the same JSON value.  This permits
# no resource-owner exception: malformed JSON or a conflicting translation
# remains a hard bundle failure.
is_shared_locale_resource() {
    case "$1" in
        assets/deadrecall/lang/en_us.json|assets/deadrecall/lang/zh_cn.json|assets/deadrecall/lang/zh_tw.json) return 0 ;;
        *) return 1 ;;
    esac
}

invalid_duplicates="${temporary_directory}/invalid-duplicates"
shared_locale_duplicates="${temporary_directory}/shared-locale-duplicates"
: > "${shared_locale_duplicates}"
while IFS= read -r duplicate_resource; do
    [[ -n "${duplicate_resource}" ]] || continue
    resource_path="${duplicate_resource#resource }"
    if is_shared_locale_resource "${resource_path}"; then
        printf '%s\n' "${resource_path}" >> "${shared_locale_duplicates}"
    else
        printf '%s\n' "${duplicate_resource}" >> "${invalid_duplicates}"
    fi
done < "${duplicate_resources}"

if [[ -s "${invalid_duplicates}" ]]; then
    printf 'The assembled bundle has duplicate compatibility resources:\n' >&2
    cat "${invalid_duplicates}" >&2
    exit 1
fi

shared_locale_conflicts="${temporary_directory}/shared-locale-conflicts"
while IFS= read -r locale_path; do
    [[ -n "${locale_path}" ]] || continue
    locale_entries="${temporary_directory}/$(basename "${locale_path}").entries"
    : > "${locale_entries}"

    for module_jar in "${jars[@]}"; do
        jar tf "${module_jar}" | grep -Fqx "${locale_path}" || continue
        locale_json="${temporary_directory}/$(basename "${module_jar}").$(basename "${locale_path}").json"
        unzip -p "${module_jar}" "${locale_path}" > "${locale_json}"
        jq -e -c -S 'if type == "object" then to_entries[] else error("locale must be a JSON object") end' \
            "${locale_json}" >> "${locale_entries}"
    done

    jq -r -s '
        sort_by(.key)
        | group_by(.key)[]
        | select(length > 1 and ([.[].value] | unique | length > 1))
        | .[0].key
    ' "${locale_entries}" | while IFS= read -r key; do
        printf '%s: %s\n' "${locale_path}" "${key}" >> "${shared_locale_conflicts}"
    done
done < "${shared_locale_duplicates}"

if [[ -s "${shared_locale_conflicts}" ]]; then
    printf 'The assembled bundle has conflicting shared locale translations:\n' >&2
    cat "${shared_locale_conflicts}" >&2
    exit 1
fi

for source_root in "${source_roots[@]}"; do
    [[ -d "${source_root}" ]] || { printf 'Source root is missing: %s\n' "${source_root}" >&2; exit 1; }
    source_paths=()
    for candidate in "${source_root}/src/main/java" "${source_root}/src/client/java" "${source_root}/src/main/resources"; do
        [[ -d "${candidate}" ]] && source_paths+=("${candidate}")
    done
    [[ ${#source_paths[@]} -gt 0 ]] || {
        printf 'Source root has no production sources or resources: %s\n' "${source_root}" >&2
        exit 1
    }
    find "${source_paths[@]}" \
        -type f \
        \( -name '*.java' -o -name '*.json' -o -name '*.json5' -o -name '*.mcmeta' -o -name '*.properties' -o -name '*.txt' \) \
        -print0 \
        | xargs -0 -r awk '
            {
                remaining = $0
                while (match(remaining, /deadrecall:[a-z0-9_\.\/-]+/)) {
                    print "identifier " substr(remaining, RSTART, RLENGTH)
                    remaining = substr(remaining, RSTART + RLENGTH)
                }
                remaining = $0
                factory = "Identifier\\.fromNamespaceAndPath\\(\"deadrecall\",[[:space:]]*\"[a-z0-9_./-]+\"\\)"
                while (match(remaining, factory)) {
                    identifier = substr(remaining, RSTART, RLENGTH)
                    sub(/^.*"deadrecall",[[:space:]]*"/, "", identifier)
                    sub(/".*$/, "", identifier)
                    print "identifier deadrecall:" identifier
                    remaining = substr(remaining, RSTART + RLENGTH)
                }
            }
        ' >> "${bundle_identifiers}"
done

{
    cat "${bundle_resources_all}"
    cat "${bundle_identifiers}"
} | LC_ALL=C sort -u > "${temporary_directory}/bundle-surface"

required_surface="${temporary_directory}/required-surface"
LC_ALL=C sort -u "${BASELINE_FILE}" > "${required_surface}"
if [[ "${allow_delegated_surface}" == true ]]; then
    delegated_surface="${temporary_directory}/delegated-surface"
    LC_ALL=C sort -u "${DELEGATED_SURFACE_FILE}" > "${delegated_surface}"
    comm -23 "${required_surface}" "${delegated_surface}" > "${required_surface}.root-only"
    mv "${required_surface}.root-only" "${required_surface}"
fi

missing_surface="${temporary_directory}/missing-surface"
comm -23 "${required_surface}" "${temporary_directory}/bundle-surface" > "${missing_surface}"
if [[ -s "${missing_surface}" ]]; then
    printf 'The assembled bundle is missing committed compatibility entries:\n' >&2
    cat "${missing_surface}" >&2
    printf '\nKeep the baseline unchanged until the owning module, migration, rollback and bundle evidence are approved.\n' >&2
    exit 1
fi

printf 'Assembled compatibility bundle matches the committed surface with no duplicate resources.\n'
