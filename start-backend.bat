@echo off
echo ========================================
echo   InsvnterAI - Backend Server
echo ========================================
echo.

cd /d "%~dp0insvnter-backend"

where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Maven not found! Please install Maven first.
    echo [INFO]  Run in PowerShell: winget install Apache.Maven
    echo [INFO]  Then restart your terminal.
    pause
    exit /b 1
)

echo [INFO] Starting Spring Boot...
echo [INFO] URL: http://localhost:8080
echo [INFO] Health: http://localhost:8080/api/health
echo.
call mvn spring-boot:run
pause
