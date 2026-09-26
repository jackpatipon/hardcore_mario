@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ========================================================
echo   Launching Hardcore Mario
echo ========================================================

:: 1. Look for java in PATH
set JAVA_CMD=java
where java >nul 2>nul
if %errorlevel% equ 0 goto RUN_GAME

:: 2. Look for System Java paths
if exist "C:\Program Files\Java\jdk-27\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Java\jdk-27\bin\java.exe"
    goto RUN_GAME
)
if exist "C:\Program Files\Java\latest\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Java\latest\bin\java.exe"
    goto RUN_GAME
)

:: 3. Look for JAVA_HOME
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
        goto RUN_GAME
    )
)

:: 4. Look for VS Code Embedded JDK
set "VSCODE_JAVA=C:\Users\patip\.vscode\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64\bin\java.exe"
if exist "%VSCODE_JAVA%" (
    set "JAVA_CMD=%VSCODE_JAVA%"
    goto RUN_GAME
)

:: 5. Look for older JRE
if exist "C:\Program Files (x86)\Java\jre1.8.0_503\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files (x86)\Java\jre1.8.0_503\bin\java.exe"
    goto RUN_GAME
)

echo [ERROR] Could not find java.exe. Please install Java JRE or JDK.
pause
exit /b 1

:RUN_GAME
:: Compile if bin is missing
if not exist "bin\com\hardcoremario\Main.class" (
    echo [INFO] Compiling game classes...
    call compile.bat
    if !errorlevel! neq 0 (
        echo [ERROR] Compilation failed.
        pause
        exit /b !errorlevel!
    )
)

echo Running game...
"%JAVA_CMD%" -cp "bin;lib/*" com.hardcoremario.Main
if %errorlevel% neq 0 (
    echo.
    echo [INFO] Game closed.
    pause
)
