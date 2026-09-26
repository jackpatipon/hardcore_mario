@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ========================================================
echo   Launching Hardcore Mario
echo ========================================================

:: 1. Look for java in PATH
set JAVA_CMD=java
where java >nul 2>nul
if %errorlevel% equ 0 goto CHECK_COMPILE

:: 2. Look for System Java paths
if exist "C:\Program Files\Java\jdk-27\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Java\jdk-27\bin\java.exe"
    goto CHECK_COMPILE
)
if exist "C:\Program Files\Java\latest\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Java\latest\bin\java.exe"
    goto CHECK_COMPILE
)

:: 3. Look for JAVA_HOME
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
        goto CHECK_COMPILE
    )
)

:: 4. Look for VS Code Embedded JDK
set "VSCODE_JAVA=C:\Users\patip\.vscode\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64\bin\java.exe"
if exist "%VSCODE_JAVA%" (
    set "JAVA_CMD=%VSCODE_JAVA%"
    goto CHECK_COMPILE
)

:: 5. Look for older JRE
if exist "C:\Program Files (x86)\Java\jre1.8.0_503\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files (x86)\Java\jre1.8.0_503\bin\java.exe"
    goto CHECK_COMPILE
)

echo [ERROR] Could not find java.exe. Please install Java JRE or JDK.
pause
exit /b 1

:CHECK_COMPILE
if "%~1"=="--skip-compile" goto RUN_GAME

:: Always compile latest code changes before running
echo [INFO] Compiling latest game code...
call compile.bat --no-run
if !errorlevel! neq 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b !errorlevel!
)

:RUN_GAME
echo Running game...
"%JAVA_CMD%" -cp "bin;lib/*" com.hardcoremario.Main
if %errorlevel% neq 0 (
    echo.
    echo [INFO] Game closed.
    pause
)
