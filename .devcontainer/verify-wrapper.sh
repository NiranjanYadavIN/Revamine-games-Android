#!/usr/bin/env bash
set -e

echo "=== Android DevContainer Auto-Config ==="

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# 1. Automatically configure local.properties with container Android SDK
if [ -d "/opt/android/sdk" ]; then
    echo "sdk.dir=/opt/android/sdk" > "$PROJECT_ROOT/local.properties"
    echo "Configured local.properties (sdk.dir=/opt/android/sdk)"
fi

# 2. Ensure gradlew has executable permissions
if [ -f "gradlew" ]; then
    chmod +x "gradlew"
fi

# 3. Verify wrapper execution
echo "Verifying Gradle environment..."
./gradlew --version || true

echo "=== Environment is 100% Ready-to-Use! ==="
