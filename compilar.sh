#!/usr/bin/env bash
# Gera BotaoVermelho.apk nesta pasta.
set -euo pipefail
cd "$(dirname "$0")"
export JAVA_HOME="${JAVA_HOME_17:-$HOME/Android/jdk-17.0.21.1+2}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
[ -f local.properties ] || echo "sdk.dir=$ANDROID_HOME" > local.properties
GRADLE="$HOME/Android/gradle-8.11.1/bin/gradle"
[ -x "$GRADLE" ] || GRADLE=./gradlew
"$GRADLE" assembleDebug --console=plain -q
cp app/build/outputs/apk/debug/app-debug.apk BotaoVermelho.apk
echo "Pronto: $(pwd)/BotaoVermelho.apk"
