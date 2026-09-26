#!/usr/bin/env bash
set -e

echo "=== Android Keystore Generator ==="

KEYTOOL_BIN=""

# 1. Check in PATH
if command -v keytool >/dev/null 2>&1; then
    KEYTOOL_BIN="$(command -v keytool)"
fi

# 2. Check in JAVA_HOME
if [ -z "$KEYTOOL_BIN" ] && [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/keytool" ]; then
    KEYTOOL_BIN="$JAVA_HOME/bin/keytool"
fi

# 3. Check common JVM installations
if [ -z "$KEYTOOL_BIN" ]; then
    POSSIBLE_KEYTOOL=$(find /usr/lib/jvm /usr/local/sdkman /opt /home -name keytool -type f -executable 2>/dev/null | head -1)
    if [ -n "$POSSIBLE_KEYTOOL" ]; then
        KEYTOOL_BIN="$POSSIBLE_KEYTOOL"
    fi
fi

# 4. If still not found, try installing openjdk-17-jdk
if [ -z "$KEYTOOL_BIN" ]; then
    echo "keytool not found. Installing OpenJDK 17..."
    if command -v sudo >/dev/null 2>&1; then
        sudo apt-get update && sudo apt-get install -y openjdk-17-jdk-headless
    else
        apt-get update && apt-get install -y openjdk-17-jdk-headless
    fi
    KEYTOOL_BIN="$(command -v keytool || true)"
fi

if [ -z "$KEYTOOL_BIN" ]; then
    echo "Error: Could not locate or install 'keytool'. Please ensure JDK is installed."
    exit 1
fi

echo "Using keytool at: $KEYTOOL_BIN"

OUTPUT_KEY="release.jks"
ALIAS="my-release-key"

# Ask password or use argument
STORE_PASS="${1:-}"
if [ -z "$STORE_PASS" ]; then
    read -rsp "Enter password for Keystore & Key (minimum 6 characters): " STORE_PASS
    echo ""
fi

if [ ${#STORE_PASS} -lt 6 ]; then
    echo "Error: Password must be at least 6 characters long."
    exit 1
fi

echo "Generating $OUTPUT_KEY..."
"$KEYTOOL_BIN" -genkeypair -v \
  -keystore "$OUTPUT_KEY" \
  -alias "$ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass "$STORE_PASS" \
  -keypass "$STORE_PASS" \
  -dname "CN=AndroidDeveloper, OU=Dev, O=RevaMine, L=City, ST=State, C=IN"

echo "Creating keystore.properties..."
cat <<EOF > keystore.properties
storeFile=../release.jks
storePassword=$STORE_PASS
keyAlias=$ALIAS
keyPassword=$STORE_PASS
EOF

echo ""
echo "=== SUCCESS! ==="
echo "1. Keystore generated: $OUTPUT_KEY"
echo "2. Configuration saved: keystore.properties"
echo "Now you can run: ./gradlew bundleRelease"
