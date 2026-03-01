@echo off
echo ==================================================
echo   Stop Local MariaDB Service (requires Admin)
echo ==================================================
echo.

:: Check if running as admin
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [INFO] Requesting admin privileges...
    powershell -Command "Start-Process '%~f0' -Verb RunAs"
    exit /b
)

:: Stop and disable MariaDB
echo [INFO] Stopping MariaDB service...
net stop MariaDB
if %errorlevel% equ 0 (
    echo [OK]   MariaDB service stopped.
) else (
    echo [WARN] MariaDB service may already be stopped.
)

echo [INFO] Disabling MariaDB auto-start...
sc config MariaDB start= disabled
echo [OK]   MariaDB set to disabled.

echo.
echo ==================================================
echo   Done! Port 3306 is now free for Docker.
echo   To re-enable later: sc config MariaDB start= auto
echo ==================================================
echo.
pause
