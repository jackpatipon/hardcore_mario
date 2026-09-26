@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ========================================================
echo   Building Release Package for Hardcore Mario
echo ========================================================

:: 1. Look for jar in PATH
set "JAR_CMD=jar"
where jar >nul 2>nul
if %errorlevel% equ 0 goto COMPILE

:: 2. Look for System JDK paths
if exist "C:\Program Files\Java\jdk-27\bin\jar.exe" (
    set "JAR_CMD=C:\Program Files\Java\jdk-27\bin\jar.exe"
    goto COMPILE
)
if exist "C:\Program Files\Java\latest\bin\jar.exe" (
    set "JAR_CMD=C:\Program Files\Java\latest\bin\jar.exe"
    goto COMPILE
)

:: 3. Look for JAVA_HOME
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\jar.exe" (
        set "JAR_CMD=%JAVA_HOME%\bin\jar.exe"
        goto COMPILE
    )
)

:: 4. Look for VS Code Embedded JDK
set "VSCODE_JAR=C:\Users\patip\.vscode\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64\bin\jar.exe"
if exist "%VSCODE_JAR%" (
    set "JAR_CMD=%VSCODE_JAR%"
    goto COMPILE
)

echo [ERROR] Could not find jar.exe.
pause
exit /b 1

:COMPILE
echo [1/4] Compiling latest game source code...
call compile.bat --no-run
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %errorlevel%
)

echo [2/4] Packaging Standalone Runnable JAR...
if exist "build_tmp" rmdir /s /q "build_tmp"
mkdir "build_tmp"

:: Copy compiled game classes
xcopy /s /e /q "bin\com" "build_tmp\com\" >nul

:: Extract library jars into build_tmp to make a Fat JAR
if exist "lib\jlayer-1.0.1.jar" (
    pushd "build_tmp"
    "%JAR_CMD%" -xf "..\lib\jlayer-1.0.1.jar"
    if exist "META-INF" (
        del /f /q "META-INF\*.SF" 2>nul
        del /f /q "META-INF\*.DSA" 2>nul
        del /f /q "META-INF\*.RSA" 2>nul
    )
    popd
)

:: Create Manifest
echo Manifest-Version: 1.0 > "build_tmp\MANIFEST.MF"
echo Main-Class: com.hardcoremario.Main >> "build_tmp\MANIFEST.MF"
echo. >> "build_tmp\MANIFEST.MF"

:: Build Fat JAR
if not exist "release" mkdir "release"
"%JAR_CMD%" -cfm "release\HardcoreMario.jar" "build_tmp\MANIFEST.MF" -C "build_tmp" .
rmdir /s /q "build_tmp"

:: Copy in root directory as well for local double-click
copy /y "release\HardcoreMario.jar" "HardcoreMario.jar" >nul

echo [3/4] Preparing Release Distribution...
if exist "release\assets" rmdir /s /q "release\assets"
xcopy /s /e /q "assets" "release\assets\" >nul

if exist "HOW_TO_PLAY.txt" copy /y "HOW_TO_PLAY.txt" "release\HOW_TO_PLAY.txt" >nul

echo [4/4] Creating ZIP package for GitHub Release...
set "PS_CMD=powershell"
if exist "%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" set "PS_CMD=%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe"
"%PS_CMD%" -NoProfile -Command "if (Test-Path 'HardcoreMario_v1.0.zip') { Remove-Item 'HardcoreMario_v1.0.zip' }; Compress-Archive -Path 'release\*' -DestinationPath 'HardcoreMario_v1.0.zip' -Force"

echo ========================================================
echo [SUCCESS] Build Complete!
echo.
echo 1. Local play: Double-click 'HardcoreMario.jar' or 'run.bat'
echo 2. For GitHub Release: 'HardcoreMario_v1.0.zip'
echo ========================================================
