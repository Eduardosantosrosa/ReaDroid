#!/data/data/com.termux/files/usr/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT HUP INT TERM
SRC="$ROOT/app/src/main/java/br/com/readroid/catalogodemo"
TEST="$ROOT/tests/CatalogBehaviorTest.java"
javac -d "$TMP" \
  "$SRC/CatalogItem.java" \
  "$SRC/CatalogRepository.java" \
  "$SRC/CatalogNavigator.java" \
  "$TEST"
java -cp "$TMP" br.com.readroid.catalogodemo.CatalogBehaviorTest
