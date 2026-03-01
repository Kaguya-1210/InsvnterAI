@echo off
echo ========================================
echo   InsvnterAI - Start All Services
echo ========================================
echo.

echo [INFO] Starting backend...
start "InsvnterAI-Backend" cmd /c "cd /d "%~dp0" & call start-backend.bat"

timeout /t 2 /nobreak >nul

echo [INFO] Starting frontend...
start "InsvnterAI-Frontend" cmd /c "cd /d "%~dp0" & call start-frontend.bat"

echo.
echo [INFO] Frontend: http://localhost:5173
echo [INFO] Backend:  http://localhost:8080/api/health
echo.
pause
