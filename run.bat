@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo   Launching Hardcore Mario (มาริโอ้เถื่อน)
echo ========================================================

:: 1. Look for java in PATH
set JAVA_CMD=java
where java >nul 2>nul
if %errorlevel% equ 0 goto CHECK_BIN

:: 2. Look for JAVA_HOME
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
        goto CHECK_BIN
    )
)

:: 3. Look for VS Code Embedded JDK
set "VSCODE_JAVA=C:\Users\patip\.vscode\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64\bin\java.exe"
if exist "%VSCODE_JAVA%" (
    set "JAVA_CMD=%VSCODE_JAVA%"
    goto CHECK_BIN
)

echo [ERROR] Could not find java.exe.
pause
exit /b 1

:CHECK_BIN
if not exist "bin\com\hardcoremario\Main.class" (
    echo [INFO] Binaries not found. Compiling first...
    call compile.bat
    if %errorlevel% neq 0 exit /b %errorlevel%
)

echo Running game...
"%JAVA_CMD%" -cp bin com.hardcoremario.Main
