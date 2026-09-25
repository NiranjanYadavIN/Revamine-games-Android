#!/usr/bin/env bash
set -e

echo "=== Checking Gradle Wrapper Integrity ==="

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

GRADLE_VERSION="8.13"
WRAPPER_PROPERTIES="gradle/wrapper/gradle-wrapper.properties"
WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"
GRADLEW="gradlew"
GRADLEW_BAT="gradlew.bat"

NEED_REPAIR=false

# 1. Check gradle-wrapper.properties
if [ ! -f "$WRAPPER_PROPERTIES" ]; then
    echo "Warning: $WRAPPER_PROPERTIES is missing."
    NEED_REPAIR=true
else
    # Extract version from distributionUrl if available
    DETECTED_VER=$(grep -o "gradle-[0-9.]\+" "$WRAPPER_PROPERTIES" | head -1 | sed 's/gradle-//')
    if [ -n "$DETECTED_VER" ]; then
        GRADLE_VERSION="$DETECTED_VER"
    fi
fi

# 2. Check gradle-wrapper.jar
if [ ! -f "$WRAPPER_JAR" ] || [ ! -s "$WRAPPER_JAR" ] || [ $(stat -c%s "$WRAPPER_JAR" 2>/dev/null || stat -f%z "$WRAPPER_JAR" 2>/dev/null || echo 0) -lt 10000 ]; then
    echo "Warning: $WRAPPER_JAR is missing or corrupt."
    NEED_REPAIR=true
fi

# 3. Check gradlew script
if [ ! -f "$GRADLEW" ] || [ ! -x "$GRADLEW" ]; then
    echo "Warning: $GRADLEW is missing or not executable."
    NEED_REPAIR=true
fi

# 4. Check gradlew.bat
if [ ! -f "$GRADLEW_BAT" ]; then
    echo "Warning: $GRADLEW_BAT is missing."
    NEED_REPAIR=true
fi

# Repair if needed
if [ "$NEED_REPAIR" = true ]; then
    echo "Repairing Gradle Wrapper for Gradle $GRADLE_VERSION..."
    mkdir -p gradle/wrapper

    # If gradle is installed on system, use it to regenerate wrapper
    if command -v gradle >/dev/null 2>&1; then
        echo "Using system gradle to regenerate wrapper..."
        gradle wrapper --gradle-version "$GRADLE_VERSION" --distribution-type bin
    else
        echo "Downloading official Gradle $GRADLE_VERSION distribution to extract wrapper components..."
        TEMP_DIR=$(mktemp -d)
        ZIP_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
        curl -sSL -o "$TEMP_DIR/gradle.zip" "$ZIP_URL"
        unzip -q "$TEMP_DIR/gradle.zip" -d "$TEMP_DIR"
        GRADLE_EXTRACTED_DIR=$(find "$TEMP_DIR" -maxdepth 1 -name "gradle-${GRADLE_VERSION}*" | head -1)

        if [ -d "$GRADLE_EXTRACTED_DIR" ]; then
            # Use bin/gradle to run wrapper generation
            "$GRADLE_EXTRACTED_DIR/bin/gradle" wrapper --gradle-version "$GRADLE_VERSION" --distribution-type bin
        fi
        rm -rf "$TEMP_DIR"
    fi
fi

# Ensure executable permissions
if [ -f "$GRADLEW" ]; then
    chmod +x "$GRADLEW"
fi

echo "Verifying wrapper execution..."
./gradlew --version

echo "=== Gradle Wrapper Verification Complete ==="
