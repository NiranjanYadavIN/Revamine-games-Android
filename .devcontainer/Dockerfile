FROM mcr.microsoft.com/devcontainers/java:1-17-bookworm

# Build arguments and environment variables
ARG ANDROID_CMD_TOOLS_VERSION="11076708"
ENV ANDROID_HOME="/opt/android/sdk"
ENV ANDROID_SDK_ROOT="/opt/android/sdk"
ENV PATH="${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/build-tools/36.0.0:${PATH}"

# Install required system tools
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    unzip \
    git \
    libncurses6 \
    openjdk-17-jdk-headless \
    && rm -rf /var/lib/apt/lists/*

# Download and install Android SDK Command-Line Tools
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools \
    && curl -fsSL -o /tmp/cmdline-tools.zip "https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_CMD_TOOLS_VERSION}_latest.zip" \
    && unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_HOME}/cmdline-tools \
    && mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
    && rm -f /tmp/cmdline-tools.zip

# Accept licenses and install required SDK packages (matching project compileSdk 36 & build-tools 36.0.0)
RUN yes | sdkmanager --licenses \
    && sdkmanager --install \
        "platform-tools" \
        "platforms;android-36" \
        "build-tools;36.0.0" \
    && chown -R vscode:vscode ${ANDROID_HOME}

USER vscode
