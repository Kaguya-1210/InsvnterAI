@echo off
echo ========================================
echo   InsvnterAI - Frontend Dev Server
echo ========================================
echo.

cd /d "%~dp0insvnter-frontend"

if not exist "node_modules" (
    echo [INFO] First run, installing dependencies...
    call npm install
    echo.
)

echo [INFO] Starting Vue 3 dev server...
echo [INFO] URL: http://localhost:5173
echo.
call npm run dev
pause
