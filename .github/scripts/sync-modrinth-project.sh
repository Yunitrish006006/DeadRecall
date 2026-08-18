#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
readonly BODY_FILE="${ROOT}/.github/staging/modrinth-project-body.md"
readonly GALLERY_DIR="${ROOT}/.github/staging/modrinth-gallery"
readonly GALLERY_MANIFEST="${GALLERY_DIR}/gallery.json"
readonly ICON_FILE="${ROOT}/src/main/resources/assets/deadrecall/icon.png"
readonly API_BASE="${MODRINTH_API_BASE:-https://api.modrinth.com/v2}"
readonly DRY_RUN="${MODRINTH_PROJECT_DRY_RUN:-false}"
readonly USER_AGENT="Yunitrish006006/DeadRecall-ProjectSync/${GITHUB_RUN_ID:-local}"
readonly PROJECT_DESCRIPTION="One verified Minecraft 26.2 Fabric bundle: backpacks, recovery, Copper Golems, villages, alchemy, travel, storage locks, and server integration."

fail() {
    printf 'error: %s\n' "$*" >&2
    exit 1
}

urlencode() {
    jq -rn --arg value "$1" '$value | @uri'
}

require_status() {
    local status="$1"
    local operation="$2"
    local response_file="${3:-}"
    case "${status}" in
        200|201|204) return 0 ;;
    esac
    printf '%s failed with HTTP %s.\n' "${operation}" "${status}" >&2
    if [[ -n "${response_file}" && -s "${response_file}" ]]; then
        jq . "${response_file}" >&2 2>/dev/null || sed -n '1,80p' "${response_file}" >&2
    fi
    exit 1
}

command -v jq >/dev/null 2>&1 || fail 'jq is required'
[[ -f "${BODY_FILE}" ]] || fail "missing project body: ${BODY_FILE}"
[[ -f "${GALLERY_MANIFEST}" ]] || fail "missing gallery manifest: ${GALLERY_MANIFEST}"
[[ -f "${ICON_FILE}" ]] || fail "missing project icon: ${ICON_FILE}"

case "${DRY_RUN}" in
    true|false) ;;
    *) fail 'MODRINTH_PROJECT_DRY_RUN must be true or false' ;;
esac

awk '
    /^#{1,6}[[:space:]]/ {
        header = $0
        sub(/^#{1,6}[[:space:]]+/, "", header)
        if (length(header) > 24) {
            printf "Header is longer than 24 characters: %s\n", $0 > "/dev/stderr"
            failed = 1
        }
    }
    END { exit failed }
' "${BODY_FILE}" || fail 'Modrinth project body header validation failed'

jq -e '
    .schema_version == 1
    and (.items | length >= 1)
    and (([.items[].file] | unique | length) == (.items | length))
    and (([.items[].title] | unique | length) == (.items | length))
    and (([.items[].ordering] | unique | length) == (.items | length))
    and (([.items[] | select(.featured == true)] | length) == 1)
    and all(.items[];
        (.file | test("^[A-Za-z0-9._-]+[.]png$"))
        and (.title | length > 0)
        and (.description | length > 0)
        and (.featured | type == "boolean")
        and (.ordering | type == "number")
        and (.legacy_titles | type == "array")
    )
' "${GALLERY_MANIFEST}" >/dev/null || fail 'invalid gallery manifest'

icon_size="$(stat -c '%s' "${ICON_FILE}")"
(( icon_size <= 262144 )) || fail "project icon exceeds Modrinth 256 KiB limit: ${icon_size} bytes"

while IFS= read -r file; do
    image="${GALLERY_DIR}/${file}"
    [[ -f "${image}" ]] || fail "missing gallery image: ${image}"
    image_size="$(stat -c '%s' "${image}")"
    (( image_size <= 5242880 )) || fail "gallery image exceeds Modrinth 5 MiB limit: ${file} (${image_size} bytes)"
done < <(jq -r '.items[].file' "${GALLERY_MANIFEST}")

printf 'Validated DeadRecall Modrinth presentation: %s gallery images, icon %s bytes.\n' \
    "$(jq '.items | length' "${GALLERY_MANIFEST}")" "${icon_size}"

if [[ "${DRY_RUN}" == 'true' ]]; then
    jq -r '.items[] | "- [\(.ordering)] \(.title) (featured=\(.featured)) <- \(.file)"' "${GALLERY_MANIFEST}"
    exit 0
fi

command -v curl >/dev/null 2>&1 || fail 'curl is required'
readonly PROJECT_ID="${MODRINTH_PROJECT_ID:-}"
readonly TOKEN="${MODRINTH_TOKEN:-}"
[[ -n "${PROJECT_ID}" ]] || fail 'MODRINTH_PROJECT_ID is required'
[[ -n "${TOKEN}" ]] || fail 'MODRINTH_TOKEN is required'

readonly AUTH_HEADER="Authorization: ${TOKEN}"
readonly UA_HEADER="User-Agent: ${USER_AGENT}"
project_response="$(mktemp)"
metadata_file="$(mktemp)"
response_file="$(mktemp)"
cleanup() {
    rm -f "${project_response}" "${metadata_file}" "${response_file}"
}
trap cleanup EXIT

jq -n \
    --arg title 'DeadRecall' \
    --arg description "${PROJECT_DESCRIPTION}" \
    --rawfile body "${BODY_FILE}" \
    '{
        title: $title,
        description: $description,
        categories: ["adventure", "storage", "utility"],
        additional_categories: ["game-mechanics", "magic", "technology"],
        client_side: "required",
        server_side: "required",
        body: $body,
        issues_url: "https://github.com/Yunitrish006006/DeadRecall/issues",
        source_url: "https://github.com/Yunitrish006006/DeadRecall",
        wiki_url: "https://github.com/Yunitrish006006/DeadRecall/blob/master/docs/README.md",
        license_id: "Apache-2.0",
        license_url: "https://github.com/Yunitrish006006/DeadRecall/blob/master/LICENSE.txt"
    }' > "${metadata_file}"

status="$(curl --silent --show-error \
    --request PATCH \
    --header "${AUTH_HEADER}" \
    --header "${UA_HEADER}" \
    --header 'Content-Type: application/json' \
    --data-binary "@${metadata_file}" \
    --output "${response_file}" \
    --write-out '%{http_code}' \
    "${API_BASE}/project/${PROJECT_ID}")"
require_status "${status}" 'Update Modrinth project metadata' "${response_file}"

status="$(curl --silent --show-error \
    --request PATCH \
    --header "${AUTH_HEADER}" \
    --header "${UA_HEADER}" \
    --header 'Content-Type: image/png' \
    --data-binary "@${ICON_FILE}" \
    --output "${response_file}" \
    --write-out '%{http_code}' \
    "${API_BASE}/project/${PROJECT_ID}/icon?ext=png")"
require_status "${status}" 'Update Modrinth project icon' "${response_file}"

curl --silent --show-error --fail --retry 3 \
    --header "${AUTH_HEADER}" \
    --header "${UA_HEADER}" \
    --output "${project_response}" \
    "${API_BASE}/project/${PROJECT_ID}"

item_count="$(jq '.items | length' "${GALLERY_MANIFEST}")"
for ((index = 0; index < item_count; index++)); do
    file="$(jq -r ".items[${index}].file" "${GALLERY_MANIFEST}")"
    title="$(jq -r ".items[${index}].title" "${GALLERY_MANIFEST}")"
    description="$(jq -r ".items[${index}].description" "${GALLERY_MANIFEST}")"
    featured="$(jq -r ".items[${index}].featured" "${GALLERY_MANIFEST}")"
    ordering="$(jq -r ".items[${index}].ordering" "${GALLERY_MANIFEST}")"
    legacy_titles="$(jq -c ".items[${index}].legacy_titles" "${GALLERY_MANIFEST}")"
    image="${GALLERY_DIR}/${file}"

    image_url="$(jq -r \
        --arg title "${title}" \
        --argjson legacy "${legacy_titles}" '
        first(
            .gallery[]? as $image
            | select($image.title == $title or ($legacy | index($image.title)))
            | $image.url
        ) // empty
    ' "${project_response}")"

    title_q="$(urlencode "${title}")"
    description_q="$(urlencode "${description}")"

    if [[ -n "${image_url}" ]]; then
        image_url_q="$(urlencode "${image_url}")"
        status="$(curl --silent --show-error \
            --request PATCH \
            --header "${AUTH_HEADER}" \
            --header "${UA_HEADER}" \
            --output "${response_file}" \
            --write-out '%{http_code}' \
            "${API_BASE}/project/${PROJECT_ID}/gallery?url=${image_url_q}&featured=${featured}&title=${title_q}&description=${description_q}&ordering=${ordering}")"
        require_status "${status}" "Update gallery image ${title}" "${response_file}"
        printf 'Updated gallery image: %s\n' "${title}"
    else
        status="$(curl --silent --show-error \
            --request POST \
            --header "${AUTH_HEADER}" \
            --header "${UA_HEADER}" \
            --header 'Content-Type: image/png' \
            --data-binary "@${image}" \
            --output "${response_file}" \
            --write-out '%{http_code}' \
            "${API_BASE}/project/${PROJECT_ID}/gallery?ext=png&featured=${featured}&title=${title_q}&description=${description_q}&ordering=${ordering}")"
        require_status "${status}" "Upload gallery image ${title}" "${response_file}"
        printf 'Uploaded gallery image: %s\n' "${title}"
    fi
done

curl --silent --show-error --fail --retry 3 \
    --header "${AUTH_HEADER}" \
    --header "${UA_HEADER}" \
    --output "${project_response}" \
    "${API_BASE}/project/${PROJECT_ID}"

jq -e \
    --arg description "${PROJECT_DESCRIPTION}" \
    --rawfile body "${BODY_FILE}" '
    .title == "DeadRecall"
    and .description == $description
    and .client_side == "required"
    and .server_side == "required"
    and .license.id == "Apache-2.0"
    and ((.body | rtrimstr("\n")) == ($body | rtrimstr("\n")))
' "${project_response}" >/dev/null || fail 'remote Modrinth project metadata verification failed'

for ((index = 0; index < item_count; index++)); do
    title="$(jq -r ".items[${index}].title" "${GALLERY_MANIFEST}")"
    description="$(jq -r ".items[${index}].description" "${GALLERY_MANIFEST}")"
    featured="$(jq -r ".items[${index}].featured" "${GALLERY_MANIFEST}")"
    ordering="$(jq -r ".items[${index}].ordering" "${GALLERY_MANIFEST}")"
    jq -e \
        --arg title "${title}" \
        --arg description "${description}" \
        --argjson featured "${featured}" \
        --argjson ordering "${ordering}" '
        any(.gallery[]?;
            .title == $title
            and .description == $description
            and .featured == $featured
            and .ordering == $ordering
        )
    ' "${project_response}" >/dev/null || fail "remote gallery verification failed: ${title}"
done

printf 'Synchronized and verified DeadRecall Modrinth project presentation.\n'
jq '{id, slug, title, status, requested_status, description, gallery: [.gallery[] | {title, featured, ordering}]}' "${project_response}"
