#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
build_dir=$(mktemp -d)
trap 'rm -rf "$build_dir"' EXIT
javac -d "$build_dir" app/src/main/java/com/camacho/rgbcontrol/IrCodes.java app/src/main/java/com/camacho/rgbcontrol/RemoteGeometry.java tests/IrCodesTest.java tests/RemoteGeometryTest.java
java -cp "$build_dir" IrCodesTest
java -cp "$build_dir" RemoteGeometryTest
