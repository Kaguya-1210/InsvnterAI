@echo off
setlocal EnableDelayedExpansion
echo ========================================
echo   InsvnterAI - Backend Server
echo ========================================
echo.

cd /d "%~dp0insvnter-backend"

:: Load .env if exists (set env vars for Spring Boot)
if exist ".env" (
    echo [INFO] Loading config from .env ...
    for /f "usebackq tokens=*" %%L in (".env") do (
        set "line=%%L"
        :: Skip comments and blank lines
        if not "!line:~0,1!"=="#" (
            if not "!line!"=="" (
                set "%%L"
            )
        )
    )
    echo [OK]   .env loaded
    echo.
) else (
    echo [WARN] No .env found, using application.yml defaults.
    echo [TIP]  Run config-db.bat to configure databases.
    echo.
)

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
