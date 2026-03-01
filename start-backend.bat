@echo off
chcp 65001 >nul
echo ========================================
echo   InsvnterAI - 启动后端服务
echo ========================================
echo.

cd /d "%~dp0insvnter-backend"

:: 检查 Maven 是否可用
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] 未找到 Maven，请先安装 Maven 并配置环境变量。
    echo [INFO]  下载地址: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo [INFO] 启动 Spring Boot 服务...
echo [INFO] 地址: http://localhost:8080
echo [INFO] 健康检查: http://localhost:8080/api/health
echo.
call mvn spring-boot:run
pause
