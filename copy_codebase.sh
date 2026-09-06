#!/bin/bash

# Define directories to ignore
IGNORE_DIRS=("build" ".gradle" ".idea" ".git" ".cxx" "llama.cpp")

# Define extensions to include
INCLUDE_EXTS=("kt" "java" "xml" "cpp" "h" "kts" "properties" "txt" "md" "sh" "json")

# Construct the extension filter
EXT_FILTER=""
for ext in "${INCLUDE_EXTS[@]}"; do
    if [ -z "$EXT_FILTER" ]; then
        EXT_FILTER="-name \"*.$ext\""
    else
        EXT_FILTER="$EXT_FILTER -o -name \"*.$ext\""
    fi
done

# Construct the directory ignore filter
IGNORE_FILTER=""
for dir in "${IGNORE_DIRS[@]}"; do
    if [ -z "$IGNORE_FILTER" ]; then
        IGNORE_FILTER="-path \"*/$dir/*\""
    else
        IGNORE_FILTER="$IGNORE_FILTER -o -path \"*/$dir/*\""
    fi
done

# Build the final find command
# We look for files matching the extensions, then exclude those in ignored paths
FINAL_CMD="find . -type f \( $EXT_FILTER \) -not \( $IGNORE_FILTER \)"

echo "Collecting source files (excluding llama.cpp and build artifacts)..."

eval "$FINAL_CMD" | while read -r file; do
    echo "========================================"
    echo "FILE: $file"
    echo "========================================"
    cat "$file"
    echo -e "\n"
done | xclip -selection clipboard

echo "Done! Your codebase has been copied to the clipboard using xclip."
