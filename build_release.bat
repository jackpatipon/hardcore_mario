@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo   Building Release Package for Hardcore Mario
echo ========================================================

:: 1. Look for jar and javac
set "JAR_CMD=jar"
where jar >nul 2>nul
if %errorlevel% equ 0 goto COMPILE

if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\jar.exe" (
        set "JAR_CMD=%JAVA_HOME%\bin\jar.exe"
        goto COMPILE
    )
)

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
call compile.bat
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

:: Create Play instruction
echo ======================================================== > "release\HOW_TO_PLAY.txt"
echo   Hardcore Mario (มาริโอ้เถื่อน) - Standalone Edition >> "release\HOW_TO_PLAY.txt"
echo ======================================================== >> "release\HOW_TO_PLAY.txt"
echo. >> "release\HOW_TO_PLAY.txt"
echo [วิธีเล่น] >> "release\HOW_TO_PLAY.txt"
echo 1. ดับเบิลคลิกที่ไฟล์ 'HardcoreMario.jar' เพื่อเริ่มเกมได้ทันที >> "release\HOW_TO_PLAY.txt"
echo    (เครื่องต้องมี Java Runtime / JRE 8 ขึ้นไป) >> "release\HOW_TO_PLAY.txt"
echo. >> "release\HOW_TO_PLAY.txt"
echo 2. ปุ่มควบคุมในเกม: >> "release\HOW_TO_PLAY.txt"
echo    - W, A, S, D  : เดิน / หมอบคลาน >> "release\HOW_TO_PLAY.txt"
echo    - SPACE       : กระโดด >> "release\HOW_TO_PLAY.txt"
echo    - เมาส์       : เล็งรอบทิศทาง 360 องศา >> "release\HOW_TO_PLAY.txt"
echo    - คลิกซ้าย    : ยิงปืน >> "release\HOW_TO_PLAY.txt"
echo    - R           : รีโหลดกระสุน >> "release\HOW_TO_PLAY.txt"
echo    - C           : สลับสีกระสุน (มีโหมดสำหรับคนตาบอดสี) >> "release\HOW_TO_PLAY.txt"
echo    - ESC / P     : เมนูหยุดชั่วคราว (Pause) >> "release\HOW_TO_PLAY.txt"
echo. >> "release\HOW_TO_PLAY.txt"
echo ผู้พัฒนา: ปฏิพล จันทร์บุญ (6804062612102 ตอน 3) >> "release\HOW_TO_PLAY.txt"

echo [4/4] Creating ZIP package for GitHub Release...
powershell -Command "if (Test-Path 'HardcoreMario_v1.0.zip') { Remove-Item 'HardcoreMario_v1.0.zip' }; Compress-Archive -Path 'release\*' -DestinationPath 'HardcoreMario_v1.0.zip' -Force"

echo ========================================================
echo [SUCCESS] Build Complete!
echo.
echo 1. เล่นบนเครื่องนี้ได้ทันที: ดับเบิลคลิกที่ 'HardcoreMario.jar'
echo 2. ไฟล์สำหรับอัปโหลดขึ้น GitHub Release: 'HardcoreMario_v1.0.zip'
echo ========================================================
