@echo off
rem ------------------------------------------------------------
rem Compiles the application sources into the out\ directory.
rem Finds a JDK via JAVA_HOME, PATH, or a portable JDK in C:\Zcode\jdk-*.
rem ------------------------------------------------------------
setlocal
cd /d "%~dp0"

set "JAVA_CMD=javac"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "JAVA_CMD=%JAVA_HOME%\bin\javac.exe"
if not exist "%JAVA_CMD%" (
    for /d %%D in ("C:\Zcode\jdk-*") do if exist "%%D\bin\javac.exe" set "JAVA_CMD=%%D\bin\javac.exe"
)

if not exist out mkdir out

echo Compiling sources using %JAVA_CMD%...
dir /s /b src\*.java > sources.txt
"%JAVA_CMD%" -cp "lib\*" -d out @sources.txt
if errorlevel 1 (
    echo.
    echo COMPILATION FAILED.
    del sources.txt
    exit /b 1
)
del sources.txt
echo.
echo Compilation successful. Run the application with run.bat
endlocal
