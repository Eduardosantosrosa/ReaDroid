#!/data/data/com.termux/files/usr/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
OUT="$ROOT/build/local"
APP="$ROOT/app/src/main"
mkdir -p "$OUT/classes" "$ROOT/app/build/outputs/apk/debug"
ANDROID_JAR="${ANDROID_JAR:-$PREFIX/share/java/android.jar}"
FRAMEWORK_RES="${FRAMEWORK_RES:-/system/framework/framework-res.apk}"
javac --release 8 -cp "$ANDROID_JAR" -d "$OUT/classes" "$APP/java/org/independent/streambox/MediaEntry.java" "$APP/java/org/independent/streambox/M3uParser.java" "$APP/java/org/independent/streambox/MainActivity.java"
dx --dex --min-sdk-version=26 --output="$OUT/classes.dex" "$OUT/classes"
aapt2 link -o "$OUT/unsigned.apk" --manifest "$APP/AndroidManifest.xml" -I "$FRAMEWORK_RES" --min-sdk-version 26 --target-sdk-version 29 --version-code 1 --version-name 1.0.0
(cd "$OUT" && zip -q -u unsigned.apk classes.dex)
zipalign -f 4 "$OUT/unsigned.apk" "$OUT/aligned.apk"
KEY="$ROOT/build/debug.keystore"
if [ ! -f "$KEY" ]; then
  keytool -genkeypair -keystore "$KEY" -storepass android -keypass android -alias androiddebugkey -dname 'CN=Android Debug,O=Independent,C=BR' -keyalg RSA -keysize 2048 -validity 10000 >/dev/null 2>&1
fi
apksigner sign --ks "$KEY" --ks-pass pass:android --key-pass pass:android --out "$ROOT/app/build/outputs/apk/debug/app-debug.apk" "$OUT/aligned.apk"
