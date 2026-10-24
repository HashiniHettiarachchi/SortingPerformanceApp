@echo off
rem ------------------------------------------------------------
rem Launches the Sorting Algorithm Performance Evaluator.
rem Finds a JDK via JAVA_HOME, PATH, or a portable JDK in C:\Zcode\jdk-*.
rem ------------------------------------------------------------
setlocal
cd /d "%~dp0"

if not exist out\Main.class (
    echo Application is not compiled yet. Running compile.bat first...
    call compile.bat
    if errorlevel 1 exit /b 1
)

set "JAVA_CMD=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
if not exist "%JAVA_CMD%" (
    for /d %%D in ("C:\Zcode\jdk-*") do if exist "%%D\bin\java.exe" set "JAVA_CMD=%%D\bin\java.exe"
)

"%JAVA_CMD%" -cp "out;lib\*" Main
endlocal
