#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly REPOSITORY_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
readonly API_BASE="${MODRINTH_API_BASE:-https://api.modrinth.com/v2}"
readonly USER_AGENT="Yunitrish006006/DeadRecall-StandaloneSync/${GITHUB_RUN_ID:-local}"
readonly MANIFEST="${REPOSITORY_ROOT}/.github/staging/modrinth-standalone/manifest.json"
readonly BODY_DIR="${REPOSITORY_ROOT}/.github/staging/modrinth-standalone"
readonly BUNDLE="${REPOSITORY_ROOT}/.github/staging/deadrecall-2.4.13-bundled.jar"
readonly TEMP_DIR="$(mktemp -d)"
readonly EXTRACT_DIR="${TEMP_DIR}/jars"
readonly PROJECT_IDS="${TEMP_DIR}/project-ids.json"
readonly VERSION_IDS="${TEMP_DIR}/version-ids.json"

cleanup() {
    rm -rf -- "${TEMP_DIR}"
}
trap cleanup EXIT

die() {
    printf 'error: %s\n' "$*" >&2
    exit 1
}

show_api_error() {
    local response_file="$1"

    if [[ -s "${response_file}" ]]; then
        jq . "${response_file}" >&2 2>/dev/null || sed -n '1,80p' "${response_file}" >&2
    fi
}

json_status() {
    local method="$1"
    local url="$2"
    local output_file="$3"
    local data_file="${4:-}"
    local -a args=(
        --silent
        --show-error
        --retry 3
        --retry-all-errors
        --request "${method}"
        --header "Authorization: ${MODRINTH_TOKEN_VALUE}"
        --header "User-Agent: ${USER_AGENT}"
        --output "${output_file}"
        --write-out '%{http_code}'
    )

    if [[ -n "${data_file}" ]]; then
        args+=(
            --header 'Content-Type: application/json'
            --data-binary "@${data_file}"
        )
    fi

    curl "${args[@]}" "${url}"
}

require_success() {
    local status="$1"
    local response_file="$2"
    local operation="$3"

    if [[ "${status}" != "200" && "${status}" != "201" && "${status}" != "204" ]]; then
        printf '%s failed with HTTP %s.\n' "${operation}" "${status}" >&2
        show_api_error "${response_file}"
        exit 1
    fi
}

set_json_mapping() {
    local file="$1"
    local key="$2"
    local value="$3"
    local next_file="${file}.next"

    jq --arg key "${key}" --arg value "${value}" '. + {($key): $value}' \
        "${file}" > "${next_file}"
    mv "${next_file}" "${file}"
}

project_id_for() {
    local module_id="$1"

    jq -er --arg module_id "${module_id}" '.[$module_id]' "${PROJECT_IDS}"
}

validate_inputs() {
    command -v curl >/dev/null 2>&1 || die "curl is required"
    command -v jq >/dev/null 2>&1 || die "jq is required"
    command -v sha512sum >/dev/null 2>&1 || die "sha512sum is required"
    command -v unzip >/dev/null 2>&1 || die "unzip is required"
    command -v file >/dev/null 2>&1 || die "file is required"

    [[ -f "${MANIFEST}" ]] || die "missing standalone manifest: ${MANIFEST}"
    [[ -f "${BUNDLE}" ]] || die "missing verified bundle: ${BUNDLE}"
    jq -e '
        .schema_version == 1
        and .minecraft_version == "26.2"
        and .loader == "fabric"
        and (.modules | length == 11)
        and (([.modules[].id] | unique | length) == 11)
        and (([.modules[].slug] | unique | length) == 11)
        and ([.modules[] | select(.version_environment == "server_only_client_optional") | .id]
            == ["totem-discord-bridge"])
        and all(.modules[];
            (.id | test("^[a-z0-9_-]+$"))
            and (.slug | test("^[a-z0-9_-]+$"))
            and (.title | length > 0)
            and (.description | length > 0 and length <= 256)
            and (.icon | test("^icons/[a-z0-9_-]+[.]png$"))
            and (.version | length > 0)
            and (.sha512 | test("^[0-9a-f]{128}$"))
            and (.categories | length > 0 and length <= 3)
            and (.additional_categories | length <= 3)
            and (.client_side | IN("required", "optional", "unsupported", "unknown"))
            and (.server_side | IN("required", "optional", "unsupported", "unknown"))
            and (.version_environment | IN(
                "client_and_server",
                "client_only",
                "client_only_server_optional",
                "singleplayer_only",
                "server_only",
                "server_only_client_optional",
                "dedicated_server_only",
                "client_or_server",
                "client_or_server_prefers_both"
            ))
        )
        and ([.modules[].id] as $module_ids
            | all(.modules[];
                all(.dependencies[]?;
                    (has("project_id") and (.project_id | length > 0))
                    or (has("module_id") and (.module_id as $dependency_module
                        | $module_ids | index($dependency_module) != null))
                )
            )
        )
    ' "${MANIFEST}" >/dev/null || die "standalone manifest validation failed"

    mapfile -t body_files < <(jq -r '.modules[].body' "${MANIFEST}")
    for body_file in "${body_files[@]}"; do
        local body_path="${BODY_DIR}/${body_file}"
        [[ -s "${body_path}" ]] || die "missing project body: ${body_path}"
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
        ' "${body_path}" || die "project body header validation failed: ${body_path}"
    done

    mapfile -t icon_files < <(jq -r '.modules[].icon' "${MANIFEST}")
    local icon_hashes="${TEMP_DIR}/icon-hashes.txt"
    : > "${icon_hashes}"
    for icon_file in "${icon_files[@]}"; do
        local icon_path="${BODY_DIR}/${icon_file}"
        local icon_size
        [[ -s "${icon_path}" ]] || die "missing project icon: ${icon_path}"
        icon_size="$(wc -c < "${icon_path}")"
        (( icon_size <= 262144 )) \
            || die "project icon exceeds Modrinth's 256 KiB limit: ${icon_path}"
        file "${icon_path}" | grep -F 'PNG image data, 64 x 64' >/dev/null \
            || die "project icon must be a 64x64 PNG: ${icon_path}"
        sha512sum "${icon_path}" | awk '{print $1}' >> "${icon_hashes}"
    done
    [[ "$(sort -u "${icon_hashes}" | wc -l)" == "11" ]] \
        || die "standalone project icons must be byte-distinct"

    mkdir -p "${EXTRACT_DIR}"
    mapfile -t module_ids < <(jq -r '.modules[].id' "${MANIFEST}")
    for module_id in "${module_ids[@]}"; do
        local jar
        local expected_version
        local expected_sha512
        local artifact
        local actual_id
        local actual_version
        local actual_sha512

        jar="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .jar' "${MANIFEST}")"
        expected_version="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .version' "${MANIFEST}")"
        expected_sha512="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .sha512' "${MANIFEST}")"
        artifact="${EXTRACT_DIR}/${jar}"

        unzip -p "${BUNDLE}" "META-INF/jars/${jar}" > "${artifact}" \
            || die "could not extract ${jar} from the verified DeadRecall bundle"
        [[ -s "${artifact}" ]] || die "extracted artifact is empty: ${jar}"

        actual_id="$(unzip -p "${artifact}" fabric.mod.json | jq -er '.id')"
        actual_version="$(unzip -p "${artifact}" fabric.mod.json | jq -er '.version')"
        actual_sha512="$(sha512sum "${artifact}" | awk '{print $1}')"

        [[ "${actual_id}" == "${module_id}" ]] \
            || die "${jar} has mod ID ${actual_id}, expected ${module_id}"
        [[ "${actual_version}" == "${expected_version}" ]] \
            || die "${jar} has version ${actual_version}, expected ${expected_version}"
        [[ "${actual_sha512}" == "${expected_sha512}" ]] \
            || die "${jar} SHA-512 does not match the audited manifest"
    done

    printf 'Validated 11 descriptions and 11 byte-exact standalone artifacts.\n'
}

check_slug_owner() {
    local slug="$1"
    local allowed_project_id="$2"
    local response_file="${TEMP_DIR}/slug-${slug}.json"
    local status
    local found_id

    status="$(json_status GET "${API_BASE}/project/${slug}" "${response_file}")"
    case "${status}" in
        200)
            found_id="$(jq -er '.id' "${response_file}")"
            [[ -n "${allowed_project_id}" && "${found_id}" == "${allowed_project_id}" ]] \
                || die "Modrinth slug ${slug} belongs to another project (${found_id})"
            ;;
        404) ;;
        *)
            printf 'Could not check Modrinth slug %s (HTTP %s).\n' "${slug}" "${status}" >&2
            show_api_error "${response_file}"
            exit 1
            ;;
    esac
}

validate_remote_tags() {
    local categories_file="${TEMP_DIR}/category-tags.json"
    local game_versions_file="${TEMP_DIR}/game-version-tags.json"
    local loaders_file="${TEMP_DIR}/loader-tags.json"
    local status

    status="$(json_status GET "${API_BASE}/tag/category" "${categories_file}")"
    require_success "${status}" "${categories_file}" "Read Modrinth category tags"
    status="$(json_status GET "${API_BASE}/tag/game_version" "${game_versions_file}")"
    require_success "${status}" "${game_versions_file}" "Read Modrinth game-version tags"
    status="$(json_status GET "${API_BASE}/tag/loader" "${loaders_file}")"
    require_success "${status}" "${loaders_file}" "Read Modrinth loader tags"

    jq -e --slurpfile category_tags "${categories_file}" '
        [.modules[] | (.categories + .additional_categories)[]]
        | unique as $required
        | all($required[]; . as $wanted
            | any($category_tags[0][];
                .project_type == "mod" and .name == $wanted))
    ' "${MANIFEST}" >/dev/null || die "manifest contains an unsupported Modrinth mod category"
    jq -e --arg version "$(jq -er '.minecraft_version' "${MANIFEST}")" \
        'any(.[]; .version == $version)' "${game_versions_file}" >/dev/null \
        || die "Modrinth does not recognize the configured Minecraft version"
    jq -e --arg loader "$(jq -er '.loader' "${MANIFEST}")" \
        'any(.[]; .name == $loader)' "${loaders_file}" >/dev/null \
        || die "Modrinth does not recognize the configured loader"
    printf 'Validated Modrinth category, game-version, and loader tags.\n'
}

create_project() {
    local module_id="$1"
    local create_data="${TEMP_DIR}/create-${module_id}.json"
    local create_response="${TEMP_DIR}/create-${module_id}-response.json"
    local body_file
    local body_path
    local title
    local slug
    local description
    local repository
    local icon_file
    local icon_path
    local categories
    local additional_categories
    local client_side
    local server_side
    local status

    body_file="$(jq -er --arg module_id "${module_id}" \
        '.modules[] | select(.id == $module_id) | .body' "${MANIFEST}")"
    body_path="${BODY_DIR}/${body_file}"
    title="$(jq -er --arg module_id "${module_id}" \
        '.modules[] | select(.id == $module_id) | .title' "${MANIFEST}")"
    slug="$(jq -er --arg module_id "${module_id}" \
        '.modules[] | select(.id == $module_id) | .slug' "${MANIFEST}")"
    description="$(jq -er --arg module_id "${module_id}" \
        '.modules[] | select(.id == $module_id) | .description' "${MANIFEST}")"
    repository="$(jq -r --arg module_id "${module_id}" \
        '.modules[] | select(.id == $module_id) | .repository' "${MANIFEST}")"
    icon_file="$(jq -er --arg module_id "${module_id}" \
        '.modules[] | select(.id == $module_id) | .icon' "${MANIFEST}")"
    icon_path="${BODY_DIR}/${icon_file}"
    categories="$(jq -c --arg module_id "${module_id}" \
        '.modules[] | select(.id == $module_id) | .categories' "${MANIFEST}")"
    additional_categories="$(jq -c --arg module_id "${module_id}" \
        '.modules[] | select(.id == $module_id) | .additional_categories' "${MANIFEST}")"
    client_side="$(jq -er --arg module_id "${module_id}" \
        '.modules[] | select(.id == $module_id) | .client_side' "${MANIFEST}")"
    server_side="$(jq -er --arg module_id "${module_id}" \
        '.modules[] | select(.id == $module_id) | .server_side' "${MANIFEST}")"

    jq -n \
        --arg slug "${slug}" \
        --arg title "${title}" \
        --arg description "${description}" \
        --argjson categories "${categories}" \
        --argjson additional_categories "${additional_categories}" \
        --arg client_side "${client_side}" \
        --arg server_side "${server_side}" \
        --rawfile body "${body_path}" \
        --arg repository "${repository}" \
        '{
            slug: $slug,
            title: $title,
            description: $description,
            categories: $categories,
            additional_categories: $additional_categories,
            client_side: $client_side,
            server_side: $server_side,
            body: $body,
            license_id: "Apache-2.0",
            project_type: "mod",
            initial_versions: [],
            is_draft: true,
            gallery_items: []
        }
        + if $repository == "" then {}
          else {
              source_url: $repository,
              issues_url: ($repository + "/issues")
          }
          end' > "${create_data}"

    status="$(curl --silent --show-error \
        --request POST \
        --header "Authorization: ${MODRINTH_TOKEN_VALUE}" \
        --header "User-Agent: ${USER_AGENT}" \
        --form "data=@${create_data};type=application/json" \
        --form "icon=@${icon_path};type=image/png" \
        --output "${create_response}" \
        --write-out '%{http_code}' \
        "${API_BASE}/project")"
    require_success "${status}" "${create_response}" "Create ${title}"
    jq -er '.id' "${create_response}"
}

resolve_projects() {
    local user_response="${TEMP_DIR}/user.json"
    local projects_response="${TEMP_DIR}/projects.json"
    local status
    local user_id

    status="$(json_status GET "${API_BASE}/user" "${user_response}")"
    require_success "${status}" "${user_response}" "Authenticate Modrinth token"
    user_id="$(jq -er '.id' "${user_response}")"

    status="$(json_status GET "${API_BASE}/user/${user_id}/projects" "${projects_response}")"
    require_success "${status}" "${projects_response}" "List accessible Modrinth projects"

    printf '{}\n' > "${PROJECT_IDS}"
    mapfile -t module_ids < <(jq -r '.modules[].id' "${MANIFEST}")
    for module_id in "${module_ids[@]}"; do
        local explicit_id
        local title
        local slug
        local normalized_title
        local project_id
        local candidate_count
        local project_response="${TEMP_DIR}/resolve-${module_id}.json"

        explicit_id="$(jq -r --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .existing_project_id' "${MANIFEST}")"
        title="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .title' "${MANIFEST}")"
        slug="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .slug' "${MANIFEST}")"
        normalized_title="$(printf '%s' "${title}" | tr '[:upper:]' '[:lower:]' | tr -cd 'a-z0-9')"

        if [[ -n "${explicit_id}" ]]; then
            jq -e --arg project_id "${explicit_id}" \
                'any(.[]; .id == $project_id)' "${projects_response}" >/dev/null \
                || die "configured project ${explicit_id} for ${title} is not accessible to this token"
            status="$(json_status GET "${API_BASE}/project/${explicit_id}" "${project_response}")"
            require_success "${status}" "${project_response}" "Read configured ${title} project"
            project_id="$(jq -er '.id' "${project_response}")"
        else
            candidate_count="$(jq \
                --arg slug "${slug}" \
                --arg normalized_title "${normalized_title}" '
                    [.[] | select(
                        (.slug | ascii_downcase) == $slug
                        or ((.title | ascii_downcase | gsub("[^a-z0-9]"; "")) == $normalized_title)
                    )] | length
                ' "${projects_response}")"
            if [[ "${candidate_count}" == "1" ]]; then
                project_id="$(jq -er \
                    --arg slug "${slug}" \
                    --arg normalized_title "${normalized_title}" '
                        [.[] | select(
                            (.slug | ascii_downcase) == $slug
                            or ((.title | ascii_downcase | gsub("[^a-z0-9]"; "")) == $normalized_title)
                        )][0].id
                    ' "${projects_response}")"
                printf 'Reusing accessible Modrinth project %s for %s.\n' "${project_id}" "${title}"
            elif [[ "${candidate_count}" == "0" ]]; then
                check_slug_owner "${slug}" ""
                project_id="$(create_project "${module_id}")"
                printf 'Created draft Modrinth project %s (%s).\n' "${title}" "${project_id}"
            else
                die "found ${candidate_count} accessible Modrinth projects matching ${title}; refusing to guess"
            fi
        fi

        check_slug_owner "${slug}" "${project_id}"
        set_json_mapping "${PROJECT_IDS}" "${module_id}" "${project_id}"
    done
}

update_projects() {
    mapfile -t module_ids < <(jq -r '.modules[].id' "${MANIFEST}")
    for module_id in "${module_ids[@]}"; do
        local project_id
        local title
        local slug
        local description
        local body_file
        local body_path
        local repository
        local categories
        local additional_categories
        local client_side
        local server_side
        local current_project="${TEMP_DIR}/metadata-${module_id}-current.json"
        local current_slug
        local metadata_file="${TEMP_DIR}/metadata-${module_id}.json"
        local response_file="${TEMP_DIR}/metadata-${module_id}-response.json"
        local status

        project_id="$(project_id_for "${module_id}")"
        title="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .title' "${MANIFEST}")"
        slug="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .slug' "${MANIFEST}")"
        description="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .description' "${MANIFEST}")"
        body_file="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .body' "${MANIFEST}")"
        body_path="${BODY_DIR}/${body_file}"
        repository="$(jq -r --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .repository' "${MANIFEST}")"
        categories="$(jq -c --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .categories' "${MANIFEST}")"
        additional_categories="$(jq -c --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .additional_categories' "${MANIFEST}")"
        client_side="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .client_side' "${MANIFEST}")"
        server_side="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .server_side' "${MANIFEST}")"

        status="$(json_status GET "${API_BASE}/project/${project_id}" "${current_project}")"
        require_success "${status}" "${current_project}" "Read current ${title} metadata"
        current_slug="$(jq -er '.slug' "${current_project}")"

        jq -n \
            --arg slug "${slug}" \
            --arg title "${title}" \
            --arg description "${description}" \
            --argjson categories "${categories}" \
            --argjson additional_categories "${additional_categories}" \
            --arg client_side "${client_side}" \
            --arg server_side "${server_side}" \
            --rawfile body "${body_path}" \
            --arg repository "${repository}" \
            --arg current_slug "${current_slug}" '
            {
                title: $title,
                description: $description,
                categories: $categories,
                additional_categories: $additional_categories,
                client_side: $client_side,
                server_side: $server_side,
                body: $body,
                license_id: "Apache-2.0"
            }
            + if $current_slug == $slug then {} else {slug: $slug} end
            + if $repository == "" then {}
              else {
                  source_url: $repository,
                  issues_url: ($repository + "/issues")
              }
              end
        ' > "${metadata_file}"

        status="$(json_status PATCH "${API_BASE}/project/${project_id}" \
            "${response_file}" "${metadata_file}")"
        require_success "${status}" "${response_file}" "Update ${title} metadata"
        printf 'Updated metadata for %s (%s).\n' "${title}" "${project_id}"
    done
}

update_project_icons() {
    mapfile -t module_ids < <(jq -r '.modules[].id' "${MANIFEST}")
    for module_id in "${module_ids[@]}"; do
        local project_id
        local title
        local icon_file
        local icon_path
        local response_file="${TEMP_DIR}/icon-${module_id}-response.json"
        local status

        project_id="$(project_id_for "${module_id}")"
        title="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .title' "${MANIFEST}")"
        icon_file="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .icon' "${MANIFEST}")"
        icon_path="${BODY_DIR}/${icon_file}"

        status="$(curl --silent --show-error \
            --request PATCH \
            --header "Authorization: ${MODRINTH_TOKEN_VALUE}" \
            --header "User-Agent: ${USER_AGENT}" \
            --header 'Content-Type: image/png' \
            --data-binary "@${icon_path}" \
            --output "${response_file}" \
            --write-out '%{http_code}' \
            "${API_BASE}/project/${project_id}/icon?ext=png")"
        require_success "${status}" "${response_file}" "Update ${title} icon"
        printf 'Updated icon for %s (%s).\n' "${title}" "${project_id}"
    done
}

upload_galleries() {
    mapfile -t module_ids < <(jq -r '.modules[].id' "${MANIFEST}")
    for module_id in "${module_ids[@]}"; do
        local project_id
        local gallery_count
        local project_file="${TEMP_DIR}/gallery-${module_id}-project.json"
        local response_file="${TEMP_DIR}/gallery-${module_id}-response.json"

        project_id="$(project_id_for "${module_id}")"
        gallery_count="$(jq --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .gallery | length' "${MANIFEST}")"
        if [[ "${gallery_count}" == "0" ]]; then
            continue
        fi

        mapfile -t gallery_items < <(jq -c --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .gallery[]' "${MANIFEST}")
        for gallery_item in "${gallery_items[@]}"; do
            local file
            local title
            local description
            local featured
            local ordering
            local status
            local image_url
            local image_url_query
            local title_query
            local description_query
            local local_image_sha512

            file="$(jq -er '.file' <<< "${gallery_item}")"
            title="$(jq -er '.title' <<< "${gallery_item}")"
            description="$(jq -er '.description' <<< "${gallery_item}")"
            featured="$(jq -r '.featured' <<< "${gallery_item}")"
            ordering="$(jq -er '.ordering' <<< "${gallery_item}")"
            [[ -f "${REPOSITORY_ROOT}/${file}" ]] || die "missing gallery image: ${file}"

            status="$(json_status GET "${API_BASE}/project/${project_id}" "${project_file}")"
            require_success "${status}" "${project_file}" "Read gallery for ${module_id}"
            image_url="$(jq -r --arg title "${title}" \
                'first(.gallery[]? | select(.title == $title) | .url) // empty' "${project_file}")"
            title_query="$(jq -rn --arg value "${title}" '$value | @uri')"
            description_query="$(jq -rn --arg value "${description}" '$value | @uri')"

            if [[ -z "${image_url}" ]]; then
                local_image_sha512="$(sha512sum "${REPOSITORY_ROOT}/${file}" | awk '{print $1}')"
                mapfile -t existing_gallery_urls < <(jq -r '.gallery[]?.url' "${project_file}")
                local gallery_index=0
                for existing_gallery_url in "${existing_gallery_urls[@]}"; do
                    local existing_gallery_file="${TEMP_DIR}/gallery-${module_id}-${gallery_index}.img"
                    if curl --silent --show-error --fail --retry 3 \
                        --header "User-Agent: ${USER_AGENT}" \
                        --output "${existing_gallery_file}" \
                        "${existing_gallery_url}" \
                        && [[ "$(sha512sum "${existing_gallery_file}" | awk '{print $1}')" == "${local_image_sha512}" ]]; then
                        image_url="${existing_gallery_url}"
                        printf 'Reusing byte-identical gallery image for %s: %s\n' \
                            "${module_id}" "${title}"
                        break
                    fi
                    gallery_index=$((gallery_index + 1))
                done
            fi

            if [[ -z "${image_url}" ]]; then
                status="$(curl --silent --show-error \
                    --request POST \
                    --header "Authorization: ${MODRINTH_TOKEN_VALUE}" \
                    --header "User-Agent: ${USER_AGENT}" \
                    --header 'Content-Type: image/png' \
                    --data-binary "@${REPOSITORY_ROOT}/${file}" \
                    --output "${response_file}" \
                    --write-out '%{http_code}' \
                    "${API_BASE}/project/${project_id}/gallery?ext=png&featured=${featured}&title=${title_query}&description=${description_query}&ordering=${ordering}")"
                if [[ "${status}" == "200" || "${status}" == "201" || "${status}" == "204" ]]; then
                    status="$(json_status GET "${API_BASE}/project/${project_id}" "${project_file}")"
                    require_success "${status}" "${project_file}" "Refresh gallery for ${module_id}"
                    image_url="$(jq -er --arg title "${title}" \
                        '.gallery[] | select(.title == $title) | .url' "${project_file}")"
                    printf 'Uploaded gallery image for %s: %s\n' "${module_id}" "${title}"
                elif [[ "${status}" == "400" ]] \
                    && jq -e '.description | contains("duplicate gallery images")' \
                        "${response_file}" >/dev/null; then
                    status="$(json_status GET "${API_BASE}/project/${project_id}" "${project_file}")"
                    require_success "${status}" "${project_file}" "Resolve duplicate gallery image for ${module_id}"
                    if [[ "${gallery_count}" == "1" \
                        && "$(jq '.gallery | length' "${project_file}")" == "1" ]]; then
                        image_url="$(jq -er '.gallery[0].url' "${project_file}")"
                        printf 'Reusing the sole existing duplicate gallery image for %s: %s\n' \
                            "${module_id}" "${title}"
                    else
                        die "Modrinth reports a duplicate gallery image for ${module_id}, but it cannot be identified safely"
                    fi
                else
                    require_success "${status}" "${response_file}" "Upload gallery image ${title}"
                fi
            fi

            image_url_query="$(jq -rn --arg value "${image_url}" '$value | @uri')"
            status="$(curl --silent --show-error \
                --request PATCH \
                --header "Authorization: ${MODRINTH_TOKEN_VALUE}" \
                --header "User-Agent: ${USER_AGENT}" \
                --output "${response_file}" \
                --write-out '%{http_code}' \
                "${API_BASE}/project/${project_id}/gallery?url=${image_url_query}&featured=${featured}&title=${title_query}&description=${description_query}&ordering=${ordering}")"
            require_success "${status}" "${response_file}" "Update gallery image ${title}"
        done
    done
}

resolve_dependencies() {
    local module_id="$1"

    jq -c \
        --arg module_id "${module_id}" \
        --slurpfile project_ids "${PROJECT_IDS}" '
        .modules[]
        | select(.id == $module_id)
        | .dependencies
        | map(
            if has("module_id") then {
                project_id: $project_ids[0][.module_id],
                dependency_type: .dependency_type
            } else {
                project_id: .project_id,
                dependency_type: .dependency_type
            } end
        )
    ' "${MANIFEST}"
}

sync_versions() {
    local minecraft_version
    local loader

    minecraft_version="$(jq -er '.minecraft_version' "${MANIFEST}")"
    loader="$(jq -er '.loader' "${MANIFEST}")"
    printf '{}\n' > "${VERSION_IDS}"

    mapfile -t module_ids < <(jq -r '.modules[].id' "${MANIFEST}")
    for module_id in "${module_ids[@]}"; do
        local project_id
        local title
        local version
        local jar
        local artifact
        local changelog
        local environment
        local dependencies
        local version_payload="${TEMP_DIR}/version-${module_id}.json"
        local version_response="${TEMP_DIR}/version-${module_id}-response.json"
        local versions_response="${TEMP_DIR}/versions-${module_id}.json"
        local status
        local existing_count
        local version_id
        local existing_sha512
        local local_sha512
        local patch_payload="${TEMP_DIR}/version-${module_id}-patch.json"

        project_id="$(project_id_for "${module_id}")"
        title="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .title' "${MANIFEST}")"
        version="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .version' "${MANIFEST}")"
        jar="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .jar' "${MANIFEST}")"
        changelog="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .changelog' "${MANIFEST}")"
        environment="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .version_environment' "${MANIFEST}")"
        dependencies="$(resolve_dependencies "${module_id}")"
        jq -e 'all(.[]; .project_id != null and (.project_id | length > 0))' \
            <<< "${dependencies}" >/dev/null || die "unresolved dependency for ${module_id}"

        artifact="${EXTRACT_DIR}/${jar}"
        local_sha512="$(sha512sum "${artifact}" | awk '{print $1}')"

        jq -n \
            --arg name "${title} ${version}" \
            --arg version "${version}" \
            --arg changelog "${changelog}" \
            --argjson dependencies "${dependencies}" \
            --arg minecraft_version "${minecraft_version}" \
            --arg loader "${loader}" \
            --arg project_id "${project_id}" \
            --arg environment "${environment}" '{
                name: $name,
                version_number: $version,
                changelog: $changelog,
                dependencies: $dependencies,
                game_versions: [$minecraft_version],
                version_type: "release",
                loaders: [$loader],
                featured: true,
                status: "listed",
                project_id: $project_id,
                file_parts: ["primary"],
                primary_file: "primary",
                environment: $environment
            }' > "${version_payload}"

        status="$(json_status GET \
            "${API_BASE}/project/${project_id}/version?include_changelog=false" \
            "${versions_response}")"
        require_success "${status}" "${versions_response}" "List ${title} versions"
        existing_count="$(jq --arg version "${version}" \
            '[.[] | select(.version_number == $version)] | length' "${versions_response}")"
        [[ "${existing_count}" -le 1 ]] \
            || die "${title} has more than one Modrinth version numbered ${version}"

        if [[ "${existing_count}" == "1" ]]; then
            version_id="$(jq -er --arg version "${version}" \
                '.[] | select(.version_number == $version) | .id' "${versions_response}")"
            existing_sha512="$(jq -er --arg version "${version}" '
                .[]
                | select(.version_number == $version)
                | (([.files[] | select(.primary == true)][0] // .files[0]) | .hashes.sha512)
            ' "${versions_response}")"
            [[ "${existing_sha512}" == "${local_sha512}" ]] \
                || die "${title} ${version} already exists with a different JAR; refusing to overwrite it"

            jq 'del(.project_id, .file_parts, .primary_file)' \
                "${version_payload}" > "${patch_payload}"
            status="$(json_status PATCH "${API_BASE}/version/${version_id}" \
                "${version_response}" "${patch_payload}")"
            require_success "${status}" "${version_response}" "Update ${title} ${version} metadata"
            printf 'Verified and updated existing version %s %s (%s).\n' \
                "${title}" "${version}" "${version_id}"
        else
            status="$(curl --silent --show-error \
                --request POST \
                --header "Authorization: ${MODRINTH_TOKEN_VALUE}" \
                --header "User-Agent: ${USER_AGENT}" \
                --form "data=@${version_payload};type=application/json" \
                --form "primary=@${artifact};type=application/java-archive" \
                --output "${version_response}" \
                --write-out '%{http_code}' \
                "${API_BASE}/version")"
            require_success "${status}" "${version_response}" "Publish ${title} ${version}"
            version_id="$(jq -er '.id' "${version_response}")"
            printf 'Published %s %s (%s).\n' "${title}" "${version}" "${version_id}"
        fi

        set_json_mapping "${VERSION_IDS}" "${module_id}" "${version_id}"
    done
}

submit_for_review() {
    mapfile -t module_ids < <(jq -r '.modules[].id' "${MANIFEST}")
    for module_id in "${module_ids[@]}"; do
        local project_id
        local project_file="${TEMP_DIR}/review-${module_id}.json"
        local response_file="${TEMP_DIR}/review-${module_id}-response.json"
        local request_file="${TEMP_DIR}/review-${module_id}-request.json"
        local status
        local current_status
        local requested_status

        project_id="$(project_id_for "${module_id}")"
        status="$(json_status GET "${API_BASE}/project/${project_id}" "${project_file}")"
        require_success "${status}" "${project_file}" "Read review state for ${module_id}"
        current_status="$(jq -er '.status' "${project_file}")"
        requested_status="$(jq -r '.requested_status // ""' "${project_file}")"
        if [[ "${current_status}" == "approved" || "${requested_status}" == "approved" ]]; then
            printf '%s is already approved or queued for review.\n' "${module_id}"
            continue
        fi

        printf '{"requested_status":"approved"}\n' > "${request_file}"
        status="$(json_status PATCH "${API_BASE}/project/${project_id}" \
            "${response_file}" "${request_file}")"
        require_success "${status}" "${response_file}" "Submit ${module_id} for review"
        printf 'Submitted %s for Modrinth review.\n' "${module_id}"
    done
}

verify_remote() {
    local summary_file="${GITHUB_STEP_SUMMARY:-}"

    if [[ -n "${summary_file}" ]]; then
        {
            printf '### Standalone Totem Modrinth synchronization\n\n'
            printf '| Module | Project | Version | Status |\n'
            printf '| --- | --- | --- | --- |\n'
        } >> "${summary_file}"
    fi

    mapfile -t module_ids < <(jq -r '.modules[].id' "${MANIFEST}")
    for module_id in "${module_ids[@]}"; do
        local project_id
        local version_id
        local title
        local slug
        local version
        local description
        local client_side
        local server_side
        local body_file
        local body_path
        local expected_sha512
        local expected_jar
        local expected_environment
        local expected_dependencies
        local project_file="${TEMP_DIR}/verify-${module_id}-project.json"
        local version_file="${TEMP_DIR}/verify-${module_id}-version.json"
        local status
        local project_status
        local requested_status

        project_id="$(project_id_for "${module_id}")"
        version_id="$(jq -er --arg module_id "${module_id}" '.[$module_id]' "${VERSION_IDS}")"
        title="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .title' "${MANIFEST}")"
        slug="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .slug' "${MANIFEST}")"
        version="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .version' "${MANIFEST}")"
        description="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .description' "${MANIFEST}")"
        client_side="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .client_side' "${MANIFEST}")"
        server_side="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .server_side' "${MANIFEST}")"
        body_file="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .body' "${MANIFEST}")"
        body_path="${BODY_DIR}/${body_file}"
        expected_sha512="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .sha512' "${MANIFEST}")"
        expected_jar="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .jar' "${MANIFEST}")"
        expected_environment="$(jq -er --arg module_id "${module_id}" \
            '.modules[] | select(.id == $module_id) | .version_environment' "${MANIFEST}")"
        expected_dependencies="$(resolve_dependencies "${module_id}")"

        status="$(json_status GET "${API_BASE}/project/${project_id}" "${project_file}")"
        require_success "${status}" "${project_file}" "Verify ${title} project"
        status="$(json_status GET "${API_BASE}/version/${version_id}" "${version_file}")"
        require_success "${status}" "${version_file}" "Verify ${title} version"

        jq -e \
            --arg project_id "${project_id}" \
            --arg title "${title}" \
            --arg slug "${slug}" \
            --arg description "${description}" \
            --arg client_side "${client_side}" \
            --arg server_side "${server_side}" \
            --rawfile body "${body_path}" '
            .id == $project_id
            and .title == $title
            and .slug == $slug
            and .description == $description
            and .client_side == $client_side
            and .server_side == $server_side
            and .license.id == "Apache-2.0"
            and ((.body | rtrimstr("\n")) == ($body | rtrimstr("\n")))
            and (.status == "approved" or .requested_status == "approved")
            and all(.body | split("\n")[] | select(test("^#{1,6} "));
                (sub("^#{1,6} +"; "") | length) <= 24
            )
        ' "${project_file}" >/dev/null || die "remote project verification failed for ${title}"

        jq -e \
            --arg project_id "${project_id}" \
            --arg version "${version}" \
            --arg sha512 "${expected_sha512}" \
            --arg expected_jar "${expected_jar}" \
            --arg expected_environment "${expected_environment}" \
            --argjson expected_dependencies "${expected_dependencies}" '
            .project_id == $project_id
            and .version_number == $version
            and .version_type == "release"
            and .status == "listed"
            and .environment == $expected_environment
            and .game_versions == ["26.2"]
            and .loaders == ["fabric"]
            and (([.files[] | select(.primary == true)][0] // .files[0]).filename == $expected_jar)
            and (([.files[] | select(.primary == true)][0] // .files[0]).hashes.sha512 == $sha512)
            and (
                [.dependencies[] | {project_id, dependency_type}] | sort_by(.project_id, .dependency_type)
                == ($expected_dependencies | sort_by(.project_id, .dependency_type))
            )
        ' "${version_file}" >/dev/null || die "remote version verification failed for ${title} ${version}"

        project_status="$(jq -er '.status' "${project_file}")"
        requested_status="$(jq -r '.requested_status // ""' "${project_file}")"
        printf '%s\t%s\t%s\t%s\t%s\t%s\n' \
            "${module_id}" "${project_id}" "${slug}" "${version}" "${version_id}" \
            "${project_status}/${requested_status:-none}"
        if [[ -n "${summary_file}" ]]; then
            printf '| %s | [%s](https://modrinth.com/mod/%s) | [%s](https://modrinth.com/mod/%s/version/%s) | %s / %s |\n' \
                "${title}" "${project_id}" "${slug}" "${version}" "${slug}" "${version_id}" \
                "${project_status}" "${requested_status:-none}" >> "${summary_file}"
        fi
    done
}

cd "${REPOSITORY_ROOT}"
validate_inputs

if [[ "${MODRINTH_DRY_RUN:-false}" == "true" ]]; then
    printf 'Standalone Modrinth validation completed without contacting the API.\n'
    exit 0
fi

readonly MODRINTH_TOKEN_VALUE="${MODRINTH_TOKEN:-}"
[[ -n "${MODRINTH_TOKEN_VALUE}" ]] || die "MODRINTH_TOKEN is required"

validate_remote_tags
resolve_projects
update_project_icons
upload_galleries
sync_versions
update_projects
submit_for_review
verify_remote
