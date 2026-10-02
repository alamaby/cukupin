#!/bin/sh
# Gradle wrapper start-up script (simplified). If the wrapper jar is missing,
# install Gradle 8.7 manually or open the project in Android Studio which
# will provision the wrapper automatically.

APP_BASE_NAME=${0##*/}
APP_HOME=$(cd "$(dirname "$0")" >/dev/null 2>&1 && pwd -P)

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$CLASSPATH" ]; then
    echo "gradle-wrapper.jar not found. Open this project in Android Studio,"
    echo "or download Gradle 8.7 from https://gradle.org/install/ and run:"
    echo "  gradle wrapper --gradle-version 8.7"
    exit 1
fi

exec java -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
