#!/bin/sh
# Compiles the application sources into the out/ directory (Linux/macOS).
cd "$(dirname "$0")"
mkdir -p out
find src -name '*.java' > sources.txt
javac -cp "lib/*" -d out @sources.txt || { rm -f sources.txt; echo "COMPILATION FAILED."; exit 1; }
rm -f sources.txt
echo "Compilation successful. Run the application with ./run.sh"
