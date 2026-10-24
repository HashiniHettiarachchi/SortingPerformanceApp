#!/bin/sh
# Launches the Sorting Algorithm Performance Evaluator (Linux/macOS).
cd "$(dirname "$0")"
if [ ! -f out/Main.class ]; then
    echo "Application is not compiled yet. Running compile.sh first..."
    sh compile.sh || exit 1
fi
java -cp "out:lib/*" Main
