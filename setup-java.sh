#!/usr/bin/env bash
set -euo pipefail

find_java_home() {
  local candidates=(
    "${JAVA_HOME:-}"
    "$HOME/.jdks/temurin-25/Contents/Home"
    "/Library/Java/JavaVirtualMachines"
    "/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home"
    "/usr/local/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home"
  )

  for path in "${candidates[@]}"; do
    [[ -z "${path}" ]] && continue
    if [[ -x "${path}/bin/java" ]]; then
      "${path}/bin/java" -version >/dev/null 2>&1 || continue
      echo "${path}"
      return 0
    fi
    if [[ -d "${path}" ]]; then
      while IFS= read -r -d '' java_bin; do
        local home
        home="$(cd "$(dirname "${java_bin}")/.." && pwd)"
        if "${java_bin}" -version 2>&1 | grep -q 'version "25\.'; then
          echo "${home}"
          return 0
        fi
      done < <(find "${path}" -type f -path '*/bin/java' -print0 2>/dev/null)
    fi
  done

  return 1
}

JAVA_HOME_VALUE="$(find_java_home || true)"
if [[ -z "${JAVA_HOME_VALUE}" ]]; then
  echo "找不到 Java 25。請先安裝 Java 25（例如 Temurin 25）。" >&2
  exit 1
fi

export JAVA_HOME="${JAVA_HOME_VALUE}"
export PATH="${JAVA_HOME}/bin:${PATH}"

echo "JAVA_HOME=${JAVA_HOME}"
java -version

GRADLE_PROPERTIES="${HOME}/.gradle/gradle.properties"
mkdir -p "${HOME}/.gradle"
if [[ -f "${GRADLE_PROPERTIES}" ]]; then
  tmp_file="$(mktemp)"
  awk -v java_home="${JAVA_HOME}" '
    BEGIN { updated = 0 }
    /^org\.gradle\.java\.home=/ {
      print "org.gradle.java.home=" java_home
      updated = 1
      next
    }
    { print }
    END {
      if (updated == 0) {
        print "org.gradle.java.home=" java_home
      }
    }
  ' "${GRADLE_PROPERTIES}" > "${tmp_file}"
  mv "${tmp_file}" "${GRADLE_PROPERTIES}"
else
  cat > "${GRADLE_PROPERTIES}" <<EOF
org.gradle.java.home=${JAVA_HOME}
EOF
fi
echo "Gradle Java home set to ${JAVA_HOME}"

if [[ $# -gt 0 ]]; then
  exec "$@"
fi
