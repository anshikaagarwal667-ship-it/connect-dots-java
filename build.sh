#!/bin/bash
# Build and run Connect Dots
set -e
echo "==> Compiling Connect Dots..."
mkdir -p out
javac -d out src/connectdots/*.java
echo "==> Compilation successful. Launching game..."
java -cp out connectdots.Main
