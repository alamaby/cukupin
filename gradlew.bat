@echo off
rem Gradle wrapper start-up script (simplified) for Windows.
set APP_HOME=%~dp0
set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar
if not exist "%CLASSPATH%" (
    echo gradle-wrapper.jar not found. Open this project in Android Studio,
    echo or download Gradle 8.7 from https://gradle.org/install/ and run:
    echo   gradle wrapper --gradle-version 8.7
    exit /b 1
)
java -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
