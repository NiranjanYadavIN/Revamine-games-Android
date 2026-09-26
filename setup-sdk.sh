#!/usr/bin/env bash
set -e

echo "=== Android SDK Quick Setup for Codespaces ==="

SDK_DIR="$HOME/android-sdk"
mkdir -p "$SDK_DIR/cmdline-tools"

# 1. Download Command Line Tools if not already present
if [ ! -d "$SDK_DIR/cmdline-tools/latest" ]; then
    echo "Downloading Android Command-Line Tools..."
    CMD_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
    curl -fsSL -o /tmp/cmdline-tools.zip "$CMD_TOOLS_URL"
    unzip -q /tmp/cmdline-tools.zip -d "$SDK_DIR/cmdline-tools"
    mv "$SDK_DIR/cmdline-tools/cmdline-tools" "$SDK_DIR/cmdline-tools/latest"
    rm -f /tmp/cmdline-tools.zip
    echo "Command-Line Tools installed."
fi

export ANDROID_HOME="$SDK_DIR"
export ANDROID_SDK_ROOT="$SDK_DIR"
export PATH="$SDK_DIR/cmdline-tools/latest/bin:$SDK_DIR/platform-tools:$PATH"

# 2. Accept licenses and install platforms & build tools
echo "Installing Android Platform 36 and Build-Tools..."
yes | sdkmanager --licenses >/dev/null 2>&1 || true
sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0"

# 3. Create local.properties in project root
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
echo "sdk.dir=$SDK_DIR" > "$PROJECT_ROOT/local.properties"

echo ""
echo "=== SUCCESS! ==="
echo "Android SDK configured at: $SDK_DIR"
echo "Created: $PROJECT_ROOT/local.properties"
echo "Now you can run: ./gradlew bundleRelease"
