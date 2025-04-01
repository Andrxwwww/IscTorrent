@echo off
setlocal

REM Define directories
set PROJECT_DIR=%~dp0
set SRC_DIR=%PROJECT_DIR%src
set BIN_DIR=%PROJECT_DIR%bin

REM Ensure bin directory exists
if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"

REM Compile all Java files , #TODO: ADD MORE DIRECTORIES SOON
echo Compiling Java files...
javac -d "%BIN_DIR%" -cp "%SRC_DIR%" "%SRC_DIR%\Main\*.java" "%SRC_DIR%\GUI\*.java" "%SRC_DIR%\Core\*.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    exit /b 1
)

REM Run each instance with its own ID (converted to port 8080 + ID)
if "%~1"=="" (
    echo Usage: RunProject.bat [nodeId1] [nodeId2] [...]
    exit /b 1
)

echo Starting instances...
:loop
if "%~1"=="" goto end

echo Starting instance with node ID %1
start "IscTorrent %1" java -cp "%BIN_DIR%" Main.IscTorrent %1

shift
goto loop

:end
endlocal
