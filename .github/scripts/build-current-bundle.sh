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

# 2.4.22 transition graph. Villagers is intentionally omitted.
checkout_source TotemCore          a8cedca207f9d4444ae4a52fd384ebd830fbd026
checkout_source TotemRemnant       8a6d4c291eb2f5ecf857abc1fcb718ae82b28b46
checkout_source TotemDiscordBridge aa845935867c110fa0206eab759982549e0ee3f8
checkout_source TotemAutomata      2103ba4057e069196eb6f54ddd99387aef2766eb
checkout_source TotemAlchemy       19def3fe576c0deabe5f6eaf286564da317b35ce
checkout_source TotemEnchanting    5cc5f319eae38c2d64de1053349e4418d247d56c
checkout_source TotemExcavation    b3f784d8e2faa542ef44e60a961a34bca19d1f91
checkout_source TotemLocksmith     70fe7f691d6caac50ec525665b9b5643c5ede86d
checkout_source TotemVanillaTweaks 4ce0732896f74ad9a79ef52d377dd31c868ba3cc
checkout_source TotemNexus         61bc2a25dc78d7b164e903fbc2b56a6d25db214c

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
    local version archive
    version="$(read_property "${dir}/gradle.properties" mod_version)"
    archive="$(read_property "${dir}/gradle.properties" archives_base_name)"
    printf '%s/build/libs/%s-%s.jar' "${dir}" "${archive}" "${version}"
}

build_module() {
    local repo="$1"
    shift
    local dir="${LOCKSTEP_DIR}/${repo}"
    local artifact module_id module_version expected_version

    "${GRADLE}" -p "${dir}" "$@" jar --no-daemon --stacktrace
    artifact="$(release_artifact "${repo}")"
    if [[ ! -f "${artifact}" ]]; then
        if "${GRADLE}" -p "${dir}" "$@" tasks --all --no-daemon --console=plain | grep -q '^remapJar '; then
            "${GRADLE}" -p "${dir}" "$@" remapJar --no-daemon --stacktrace
        fi
    fi
    test -f "${artifact}" || { echo "Missing release artifact for ${repo}: ${artifact}" >&2; exit 1; }

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

build_module TotemCore
readonly CORE_JAR="$(release_artifact TotemCore)"
test -f "${CORE_JAR}"

build_module TotemRemnant       -PtotemCoreJar="${CORE_JAR}"
build_module TotemExcavation    -PtotemCoreJar="${CORE_JAR}"
readonly EXCAVATION_JAR="$(release_artifact TotemExcavation)"

build_module TotemDiscordBridge -PtotemCoreJar="${CORE_JAR}"
build_module TotemAutomata      -PtotemCoreJar="${CORE_JAR}" -PtotemExcavationJar="${EXCAVATION_JAR}"
build_module TotemAlchemy       -PtotemCoreJar="${CORE_JAR}"
build_module TotemEnchanting    -PtotemCoreJar="${CORE_JAR}"
build_module TotemLocksmith     -PtotemCoreJar="${CORE_JAR}"
build_module TotemVanillaTweaks -PtotemCoreJar="${CORE_JAR}"
build_module TotemNexus         -PtotemCoreJar="${CORE_JAR}"

mapfile -t module_jars < <(find "${MODULE_DIR}" -maxdepth 1 -type f -name '*.jar' -print | sort)
test "${#module_jars[@]}" -eq 10 || {
    printf 'Expected 10 standalone JARs, found %d\n' "${#module_jars[@]}" >&2
    exit 1
}

if find "${MODULE_DIR}" -maxdepth 1 -type f -name 'totem-villagers-*.jar' | grep -q .; then
    echo 'TotemVillagers must not be present in the 2.4.22 transition bundle.' >&2
    exit 1
fi

# Core must have exactly one runtime authority. Reject feature artifacts that
# accidentally shade Core classes or nest another TotemCore JAR.
for module_jar in "${module_jars[@]}"; do
    module_id="$(unzip -p "${module_jar}" fabric.mod.json | jq -er '.id')"
    if [[ "${module_id}" == 'totem-core' ]]; then
        continue
    fi
    if unzip -Z1 "${module_jar}" | grep -q '^dev/totem/core/'; then
        printf 'Feature module %s illegally contains TotemCore classes.\n' "${module_jar}" >&2
        exit 1
    fi
    if unzip -Z1 "${module_jar}" | grep -Eq '^META-INF/jars/totem-core-[^/]+\.jar$'; then
        printf 'Feature module %s illegally nests another TotemCore JAR.\n' "${module_jar}" >&2
        exit 1
    fi
done

./gradlew \
    -PtotemCoreJar="${CORE_JAR}" \
    -PbundleModuleDirectory="${MODULE_DIR}" \
    bundleJar --no-daemon --stacktrace

readonly HOST_VERSION="$(read_property gradle.properties mod_version)"
readonly HOST_ARCHIVE="$(read_property gradle.properties archives_base_name)"
readonly BUNDLE_ARTIFACT="${ROOT}/build/libs/${HOST_ARCHIVE}-${HOST_VERSION}-bundled.jar"
test -f "${BUNDLE_ARTIFACT}"

test "$(unzip -Z1 "${BUNDLE_ARTIFACT}" 'META-INF/jars/*.jar' | wc -l | tr -d ' ')" -eq 10
unzip -p "${BUNDLE_ARTIFACT}" fabric.mod.json > "${ROOT}/build/current-bundle.fabric.mod.json"
jq -e --arg version "${HOST_VERSION}" '
    .id == "deadrecall"
    and .version == $version
    and (.jars | length == 10)
    and ([.jars[].file] | unique | length == 10)
    and (.depends["totem-core"] == "=0.7.2")
    and (.depends | has("totem-villagers") | not)
' "${ROOT}/build/current-bundle.fabric.mod.json" >/dev/null

while IFS= read -r module_jar; do
    module_name="$(basename "${module_jar}")"
    jq -e --arg path "META-INF/jars/${module_name}" 'any(.jars[]; .file == $path)' \
        "${ROOT}/build/current-bundle.fabric.mod.json" >/dev/null
    unzip -Z1 "${BUNDLE_ARTIFACT}" "META-INF/jars/${module_name}" >/dev/null
done < <(printf '%s\n' "${module_jars[@]}")

printf 'Built DeadRecall 2.4.22 transition bundle: %s\n' "${BUNDLE_ARTIFACT}"
printf 'SHA-512: %s\n' "$(sha512sum "${BUNDLE_ARTIFACT}" | awk '{print $1}')"

if [[ -n "${GITHUB_ENV:-}" ]]; then
    printf 'DEADRECALL_BUNDLE_ARTIFACT=%s\n' "${BUNDLE_ARTIFACT}" >> "${GITHUB_ENV}"
    printf 'DEADRECALL_MODULE_DIR=%s\n' "${MODULE_DIR}" >> "${GITHUB_ENV}"
    printf 'DEADRECALL_CORE_JAR=%s\n' "${CORE_JAR}" >> "${GITHUB_ENV}"
fi
