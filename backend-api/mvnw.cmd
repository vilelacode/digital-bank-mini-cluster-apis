@echo off
REM ----------------------------------------------------------------------------
REM Maven Wrapper Windows script
REM Automatically added by assistant
REM ----------------------------------------------------------------------------

SETLOCAL
set PRG=%~dp0
set WRAPPER_DIR=%PRG%.mvn\wrapper

if defined JAVA_HOME (
  set JAVA_CMD=%JAVA_HOME%\bin\java
) else (
  set JAVA_CMD=java
)

"%JAVA_CMD%" -cp "%WRAPPER_DIR%\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %*

