@echo off
chcp 65001 >nul
echo ========================================
echo   InsvnterAI - 启动前端开发服务器
echo ========================================
echo.

cd /d "%~dp0insvnter-frontend"

:: 检查 node_modules 是否存在
if not exist "node_modules" (
    echo [INFO] 首次启动，正在安装依赖...
    call npm install
    echo.
)

echo [INFO] 启动 Vue 3 开发服务器...
echo [INFO] 地址: http://localhost:5173
echo.
call npm run dev
pause
