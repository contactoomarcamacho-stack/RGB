#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
build_dir=$(mktemp -d)
trap 'rm -rf "$build_dir"' EXIT
javac -d "$build_dir" app/src/main/java/com/camacho/rgbcontrol/IrCodes.java tests/IrCodesTest.java
java -cp "$build_dir" IrCodesTest
