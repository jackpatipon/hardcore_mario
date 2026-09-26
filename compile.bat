@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo   Compiling Hardcore Mario (มาริโอ้เถื่อน)
echo ========================================================

:: 1. Look for javac in PATH
set JAVAC_CMD=javac
where javac >nul 2>nul
if %errorlevel% equ 0 goto COMPILE

:: 2. Look for JAVA_HOME
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\javac.exe" (
        set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
        goto COMPILE
    )
)

:: 3. Look for VS Code Embedded JDK
set "VSCODE_JDK=C:\Users\patip\.vscode\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64\bin\javac.exe"
if exist "%VSCODE_JDK%" (
    set "JAVAC_CMD=%VSCODE_JDK%"
    goto COMPILE
)

echo [ERROR] Could not find javac.exe. Please install Java JDK or set JAVA_HOME.
pause
exit /b 1

:COMPILE
echo Using compiler: "%JAVAC_CMD%"

if not exist bin mkdir bin

:: Find all java files and compile
dir /s /b src\*.java > sources.txt
if exist tools dir /s /b tools\*.java >> sources.txt
"%JAVAC_CMD%" --release 8 -encoding UTF-8 -cp "lib/*;bin" -d bin @sources.txt
set COMPILE_STATUS=%errorlevel%
del sources.txt

if %COMPILE_STATUS% equ 0 (
    echo [SUCCESS] Compilation completed successfully! Output in 'bin/' folder.
) else (
    echo [ERROR] Compilation failed with error code %COMPILE_STATUS%.
    exit /b %COMPILE_STATUS%
)
