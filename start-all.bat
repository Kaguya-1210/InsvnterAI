@echo off
chcp 65001 >nul
echo ========================================
echo   InsvnterAI - 一键启动前后端
echo ========================================
echo.

:: 启动后端（新窗口）
echo [INFO] 正在启动后端服务...
start "InsvnterAI Backend" cmd /k "cd /d %~dp0 && call start-backend.bat"

:: 等待 2 秒后启动前端
timeout /t 2 /nobreak >nul

:: 启动前端（新窗口）
echo [INFO] 正在启动前端服务...
start "InsvnterAI Frontend" cmd /k "cd /d %~dp0 && call start-frontend.bat"

echo.
echo [INFO] 前后端已在新窗口中启动
echo [INFO] 前端: http://localhost:5173
echo [INFO] 后端: http://localhost:8080/api/health
echo.
pause
