#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
readonly LOCKSTEP_DIR="${ROOT}/.lockstep/current-bundle"
readonly MODULE_DIR="${ROOT}/standalone-modules"

cd "${ROOT}"

command -v git >/dev/null 2>&1 || { echo 'git is required' >&2; exit 1; }
command -v jq >/dev/null 2>&1 || { echo 'jq is required' >&2; exit 1; }
command -v unzip >/dev/null 2>&1 || { echo 'unzip is required' >&2; exit 1; }

rm -rf "${LOCKSTEP_DIR}" "${MODULE_DIR}"
mkdir -p "${LOCKSTEP_DIR}" "${MODULE_DIR}"

checkout_source() {
    local repo="$1"
    local sha="$2"
    local dir="${LOCKSTEP_DIR}/${repo}"

    git init -q "${dir}"
    git -C "${dir}" remote add origin "https://github.com/Yunitrish006006/${repo}.git"
    git -C "${dir}" fetch --quiet --depth=1 origin "${sha}"
    git -C "${dir}" checkout --quiet --detach FETCH_HEAD
    test "$(git -C "${dir}" rev-parse HEAD)" = "${sha}"
    printf 'Pinned %-20s %s\n' "${repo}" "${sha}"
}

# Source pins are intentionally immutable. A module version change must update
# both DeadRecall's exact dependency graph and the corresponding source pin.
checkout_source TotemCore          c39da3cdc3b4eff16f63798dc3eae4a49e41d105
checkout_source TotemRemnant       b10d40cf00b44e292fdfa3b277fd100364636ea9
checkout_source TotemDiscordBridge 1c2aa559c3e66dc8e23074cd8070be11b4a38ffc
checkout_source TotemAutomata      c424056892c0047ef90615cba2a83bed6beb4d4e
checkout_source TotemAlchemy       3ce2abc9b77a777b717b2708103d54e73c4f769d
checkout_source TotemEnchanting    1bd2a57899376f322a97d560b123607ee4a64ac2
checkout_source TotemExcavation    091dff98833ef284d6d4068101acc55d273fa8d8
checkout_source TotemLocksmith     a7cbca9f81bb66d65c20353fa2961c4fe0ebbabd
checkout_source TotemVanillaTweaks cf43b8a77b471f3f6444e7f4b07045d391a7ea4d
checkout_source TotemNexus         5ad092fedcb7b2c2dec881fc63d8e6cc9fa4826f
checkout_source TotemVillagers     31be402143c27382bb668646445427fd4a64c263

readonly GRADLE="${LOCKSTEP_DIR}/TotemCore/gradlew"
chmod +x "${GRADLE}" gradlew

read_property() {
    local file="$1"
    local key="$2"
    sed -n "s/^${key}=//p" "${file}" | head -n 1
}

release_artifact() {
    local repo="$1"
    local dir="${LOCKSTEP_DIR}/${repo}"
    local version
    local archive
    version="$(read_property "${dir}/gradle.properties" mod_version)"
    archive="$(read_property "${dir}/gradle.properties" archives_base_name)"
    test -n "${version}" && test -n "${archive}"
    printf '%s/build/libs/%s-%s.jar' "${dir}" "${archive}" "${version}"
}

build_module() {
    local repo="$1"
    shift
    local dir="${LOCKSTEP_DIR}/${repo}"
    local artifact

    "${GRADLE}" -p "${dir}" "$@" jar --no-daemon --stacktrace
    artifact="$(release_artifact "${repo}")"
    if [[ ! -f "${artifact}" ]]; then
        if "${GRADLE}" -p "${dir}" "$@" tasks --all --no-daemon --console=plain | grep -q '^remapJar '; then
            "${GRADLE}" -p "${dir}" "$@" remapJar --no-daemon --stacktrace
        fi
    fi
    test -f "${artifact}" || {
        echo "Missing release artifact for ${repo}: ${artifact}" >&2
        find "${dir}/build/libs" -maxdepth 1 -type f -name '*.jar' -print 2>/dev/null || true
        exit 1
    }

    local module_id
    local module_version
    local expected_version
    module_id="$(unzip -p "${artifact}" fabric.mod.json | jq -er '.id')"
    module_version="$(unzip -p "${artifact}" fabric.mod.json | jq -er '.version')"
    expected_version="$(jq -er --arg id "${module_id}" '.depends[$id]' src/main/resources/fabric.mod.json)"
    test "${expected_version}" = "=${module_version}" || {
        printf 'DeadRecall expects %s %s but pinned %s produced %s\n' \
            "${module_id}" "${expected_version}" "${repo}" "${module_version}" >&2
        exit 1
    }

    cp "${artifact}" "${MODULE_DIR}/$(basename "${artifact}")"
    printf 'Built  %-20s %s %s\n' "${repo}" "${module_id}" "${module_version}"
}

# Core is the shared compile-time API for every standalone module.
build_module TotemCore
readonly CORE_JAR="$(release_artifact TotemCore)"
test -f "${CORE_JAR}"

# Build dependency providers before their consumers.
build_module TotemRemnant       -PtotemCoreJar="${CORE_JAR}"
build_module TotemExcavation    -PtotemCoreJar="${CORE_JAR}"
readonly REMNANT_JAR="$(release_artifact TotemRemnant)"
readonly EXCAVATION_JAR="$(release_artifact TotemExcavation)"

build_module TotemDiscordBridge -PtotemCoreJar="${CORE_JAR}"
build_module TotemAutomata      -PtotemCoreJar="${CORE_JAR}" -PtotemExcavationJar="${EXCAVATION_JAR}"
build_module TotemAlchemy       -PtotemCoreJar="${CORE_JAR}"
build_module TotemEnchanting    -PtotemCoreJar="${CORE_JAR}"
build_module TotemLocksmith     -PtotemCoreJar="${CORE_JAR}"
build_module TotemVanillaTweaks -PtotemCoreJar="${CORE_JAR}"
build_module TotemNexus         -PtotemCoreJar="${CORE_JAR}"
build_module TotemVillagers     -PtotemCoreJar="${CORE_JAR}" -PtotemRemnantJar="${REMNANT_JAR}"

mapfile -t module_jars < <(find "${MODULE_DIR}" -maxdepth 1 -type f -name '*.jar' -print | sort)
test "${#module_jars[@]}" -eq 11 || {
    printf 'Expected 11 standalone JARs, found %d\n' "${#module_jars[@]}" >&2
    printf '%s\n' "${module_jars[@]}" >&2
    exit 1
}

./gradlew \
    -PtotemCoreJar="${CORE_JAR}" \
    -PbundleModuleDirectory="${MODULE_DIR}" \
    bundleJar --no-daemon --stacktrace

readonly HOST_VERSION="$(read_property gradle.properties mod_version)"
readonly HOST_ARCHIVE="$(read_property gradle.properties archives_base_name)"
readonly BUNDLE_ARTIFACT="${ROOT}/build/libs/${HOST_ARCHIVE}-${HOST_VERSION}-bundled.jar"
test -f "${BUNDLE_ARTIFACT}"

test "$(unzip -Z1 "${BUNDLE_ARTIFACT}" 'META-INF/jars/*.jar' | wc -l | tr -d ' ')" -eq 11
unzip -p "${BUNDLE_ARTIFACT}" fabric.mod.json > "${ROOT}/build/current-bundle.fabric.mod.json"
jq -e --arg version "${HOST_VERSION}" '
    .id == "deadrecall"
    and .version == $version
    and (.jars | length == 11)
    and ([.jars[].file] | unique | length == 11)
' "${ROOT}/build/current-bundle.fabric.mod.json" >/dev/null

while IFS= read -r module_jar; do
    module_name="$(basename "${module_jar}")"
    jq -e --arg path "META-INF/jars/${module_name}" 'any(.jars[]; .file == $path)' \
        "${ROOT}/build/current-bundle.fabric.mod.json" >/dev/null
    unzip -Z1 "${BUNDLE_ARTIFACT}" "META-INF/jars/${module_name}" >/dev/null
done < <(printf '%s\n' "${module_jars[@]}")

printf 'Built current DeadRecall bundle: %s\n' "${BUNDLE_ARTIFACT}"
printf 'SHA-512: %s\n' "$(sha512sum "${BUNDLE_ARTIFACT}" | awk '{print $1}')"

if [[ -n "${GITHUB_ENV:-}" ]]; then
    printf 'DEADRECALL_BUNDLE_ARTIFACT=%s\n' "${BUNDLE_ARTIFACT}" >> "${GITHUB_ENV}"
    printf 'DEADRECALL_MODULE_DIR=%s\n' "${MODULE_DIR}" >> "${GITHUB_ENV}"
    printf 'DEADRECALL_CORE_JAR=%s\n' "${CORE_JAR}" >> "${GITHUB_ENV}"
fi
