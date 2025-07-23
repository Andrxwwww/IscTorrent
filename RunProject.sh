#!/bin/bash

# Define directories
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC_DIR="$PROJECT_DIR/src"
BIN_DIR="$PROJECT_DIR/bin"

# Ensure bin directory exists
mkdir -p "$BIN_DIR"

# Compile all Java files, #TODO: ADD MORE DIRECTORIES SOON
echo "Compiling Java files..."
javac -d "$BIN_DIR" -cp "$SRC_DIR" "$SRC_DIR/Main/"*.java "$SRC_DIR/GUI/"*.java "$SRC_DIR/Core/"*.java "$SRC_DIR/Messages/"*.java "$SRC_DIR/Download/"*.java
if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

# Run each instance with its own ID (converted to port 8080 + ID)
if [ $# -eq 0 ]; then
    echo "Usage: $0 [nodeId1] [nodeId2] [...]"
    exit 1
fi

echo "Starting instances..."
for nodeId in "$@"; do
    echo "Starting node ID $nodeId"
    # Use & to run in background, xterm/gnome-terminal can be used for new windows if desired
    java -cp "$BIN_DIR" Main.IscTorrent