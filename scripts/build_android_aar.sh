#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
ROOT_DIR=$(cd "$SCRIPT_DIR/.." && pwd)
ANDROID_NATIVE_DIR="$ROOT_DIR/android/native"
ANDROID_PLUGIN_DIR="$ROOT_DIR/android"
EXAMPLE_ANDROID_DIR="$ROOT_DIR/example/android"

log() {
  echo "[build_android_aar] $*"
}

fail() {
  echo "[build_android_aar][ERRO] $*" >&2
  exit 1
}

escape_property_path() {
  printf '%s' "$1" | sed 's/\\/\\\\/g; s/:/\\:/g; s/ /\\ /g'
}

detect_flutter_sdk() {
  if [[ -n "${FLUTTER_SDK:-}" ]]; then
    echo "$FLUTTER_SDK"
    return
  fi

  local flutter_bin
  flutter_bin=$(command -v flutter || true)
  if [[ -z "$flutter_bin" ]]; then
    fail "Flutter não encontrado no PATH. Exporte FLUTTER_SDK ou adicione flutter ao PATH."
  fi

  dirname "$(dirname "$flutter_bin")"
}

detect_java_home() {
  if [[ -n "${JAVA_HOME:-}" ]]; then
    echo "$JAVA_HOME"
    return
  fi

  if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    local detected
    detected=$(/usr/libexec/java_home 2>/dev/null || true)
    if [[ -n "$detected" ]]; then
      echo "$detected"
      return
    fi
  fi

  echo ""
}

detect_sdk_dir() {
  if [[ -n "${ANDROID_SDK_ROOT:-}" ]]; then
    echo "$ANDROID_SDK_ROOT"
    return
  fi

  if [[ -n "${ANDROID_HOME:-}" ]]; then
    echo "$ANDROID_HOME"
    return
  fi

  local candidates=(
    "$ANDROID_PLUGIN_DIR/local.properties"
    "$EXAMPLE_ANDROID_DIR/local.properties"
  )

  for props in "${candidates[@]}"; do
    if [[ -f "$props" ]]; then
      local sdk
      sdk=$(grep '^sdk.dir=' "$props" | head -n1 | cut -d'=' -f2-)
      if [[ -n "$sdk" ]]; then
        echo "$sdk"
        return
      fi
    fi
  done

  if [[ -d "$HOME/Library/Android/sdk" ]]; then
    echo "$HOME/Library/Android/sdk"
    return
  fi

  if [[ -d "$HOME/Android/Sdk" ]]; then
    echo "$HOME/Android/Sdk"
    return
  fi

  echo ""
}

main() {
  log "Preparando ambiente..."

  local flutter_sdk
  flutter_sdk=$(detect_flutter_sdk)
  [[ -n "$flutter_sdk" ]] || fail "Não foi possível determinar o Flutter SDK."

  local java_home
  java_home=$(detect_java_home)
  if [[ -z "$java_home" || ! -d "$java_home" ]]; then
    [[ -n "$java_home" ]] && log "JAVA_HOME detectado, mas diretório inexistente: $java_home"
    log "JAVA_HOME não identificado automaticamente. O Gradle tentará detectar uma JVM compatível."
    unset JAVA_HOME || true
    java_home=""
  else
    export JAVA_HOME="$java_home"
  fi

  local sdk_dir
  sdk_dir=$(detect_sdk_dir)
  [[ -n "$sdk_dir" ]] || fail "Não foi possível localizar o Android SDK. Defina ANDROID_SDK_ROOT/ANDROID_HOME ou configure local.properties."

  log "Flutter SDK: $flutter_sdk"
  log "Android SDK: $sdk_dir"
  [[ -n "$java_home" ]] && log "JAVA_HOME: $java_home"

  log "Executando flutter precache --android"
  "$flutter_sdk/bin/flutter" precache --android

  log "Atualizando local.properties do módulo nativo"
  mkdir -p "$ANDROID_NATIVE_DIR"
  local escaped_sdk escaped_flutter escaped_java escaped_plugin_libs
  escaped_sdk=$(escape_property_path "$sdk_dir")
  escaped_flutter=$(escape_property_path "$flutter_sdk")
  escaped_java=$(escape_property_path "${java_home:-}")
  escaped_plugin_libs=$(escape_property_path "$ANDROID_PLUGIN_DIR/libs")

  {
    echo "sdk.dir=$escaped_sdk"
    echo "flutter.sdk=$escaped_flutter"
    [[ -n "$java_home" ]] && echo "java.home=$escaped_java"
    echo "plugin.libs.dir=$escaped_plugin_libs"
  } > "$ANDROID_NATIVE_DIR/local.properties"

  log "Compilando AAR do módulo nativo"
  local gradlew
  gradlew="$EXAMPLE_ANDROID_DIR/gradlew"
  [[ -x "$gradlew" ]] || fail "Gradle wrapper do projeto de exemplo não encontrado em $gradlew"

  # GRADLE_ARGS permite flags extras, ex.: GRADLE_ARGS="--no-daemon -Dorg.gradle.jvmargs=-Xmx1g"
  # shellcheck disable=SC2086
  (cd "$EXAMPLE_ANDROID_DIR" && ./gradlew ${GRADLE_ARGS:-} -p "$ANDROID_NATIVE_DIR" clean assembleRelease)

  local aar_dir="$ANDROID_NATIVE_DIR/build/outputs/aar"
  [[ -d "$aar_dir" ]] || fail "Diretório de saída do AAR não encontrado em $aar_dir"

  local aar_path
  aar_path=$(find "$aar_dir" -maxdepth 1 -name "*-release.aar" | head -n1 || true)
  [[ -n "$aar_path" && -f "$aar_path" ]] || fail "AAR de release não localizado em $aar_dir"

  log "Copiando artefatos para android/libs"
  mkdir -p "$ANDROID_PLUGIN_DIR/libs"
  cp "$aar_path" "$ANDROID_PLUGIN_DIR/libs/rede_payment_tech-native.aar"

  local temp_dir
  temp_dir=$(mktemp -d)
  unzip -qq "$aar_path" classes.jar -d "$temp_dir"
  mv "$temp_dir/classes.jar" "$ANDROID_PLUGIN_DIR/libs/rede_payment_tech-native.jar"
  rm -rf "$temp_dir"

  log "Artefatos gerados com sucesso em android/libs"
}

main "$@"

