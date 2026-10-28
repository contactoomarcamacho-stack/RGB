#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
build_dir=$(mktemp -d)
trap 'rm -rf "$build_dir"' EXIT
javac -d "$build_dir" app/src/main/java/com/camacho/rgbcontrol/IrCodes.java app/src/main/java/com/camacho/rgbcontrol/RemoteGeometry.java app/src/main/java/com/camacho/rgbcontrol/WheelPalette.java tests/WheelPaletteTest.java app/src/main/java/com/camacho/rgbcontrol/LgCodes.java app/src/main/java/com/camacho/rgbcontrol/LocalTvAddress.java app/src/main/java/com/camacho/rgbcontrol/MotionMath.java app/src/main/java/com/camacho/rgbcontrol/VoiceCommands.java app/src/main/java/com/camacho/rgbcontrol/LgGeometry.java tests/VoiceAndLayoutTest.java tests/LgRemoteTest.java tests/IrCodesTest.java tests/RemoteGeometryTest.java
java -cp "$build_dir" IrCodesTest
java -cp "$build_dir" RemoteGeometryTest
java -cp "$build_dir" WheelPaletteTest
java -cp "$build_dir" LgRemoteTest
java -cp "$build_dir" VoiceAndLayoutTest
