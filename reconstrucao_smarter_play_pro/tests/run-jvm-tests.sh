#!/data/data/com.termux/files/usr/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
OUT="$ROOT/build/test-classes"
mkdir -p "$OUT"
javac -d "$OUT" "$ROOT/app/src/main/java/org/independent/streambox/MediaEntry.java" "$ROOT/app/src/main/java/org/independent/streambox/M3uParser.java" "$ROOT/tests/M3uParserTest.java"
java -cp "$OUT" M3uParserTest
