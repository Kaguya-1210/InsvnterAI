@echo off
chcp 65001 >nul 2>&1
setlocal EnableDelayedExpansion

echo ==================================================
echo   InsvnterAI - Database Setup
echo   MariaDB 3306 / Redis 6379 / MongoDB 27017
echo ==================================================
echo.

:: ============ Check Docker ============
where docker >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker not found. Install Docker Desktop:
    echo         https://www.docker.com/products/docker-desktop
    echo.
    pause
    exit /b 1
)

docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not running. Please start Docker Desktop.
    echo.
    pause
    exit /b 1
)

echo [OK] Docker is ready.
echo.

:: ============ Start containers ============
echo [INFO] Starting database containers...
echo.

docker compose -f "%~dp0docker-compose.db.yml" up -d

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Failed to start containers.
    pause
    exit /b 1
)

echo.
echo [INFO] Waiting for databases to be ready...
timeout /t 5 /nobreak >nul

:: ============ Init MariaDB ============
echo [INFO] Creating MariaDB database...
docker exec insvnter-mariadb mariadb -uroot -proot -e "CREATE DATABASE IF NOT EXISTS insvnter_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>nul

if %errorlevel% equ 0 (
    echo [OK] Database 'insvnter_ai' created.
) else (
    echo [WARN] MariaDB may still be initializing. The database will be auto-created on next attempt.
)

:: ============ Verify ============
echo.
echo ==================================================
echo   Status Check
echo ==================================================

:: MariaDB
docker exec insvnter-mariadb mariadb -uroot -proot -e "SELECT 1;" >nul 2>&1
if %errorlevel% equ 0 (
    echo   [OK] MariaDB    localhost:3306  user: root / pass: root
) else (
    echo   [..] MariaDB    starting...
)

:: Redis
docker exec insvnter-redis redis-cli ping >nul 2>&1
if %errorlevel% equ 0 (
    echo   [OK] Redis      localhost:6379  no password
) else (
    echo   [..] Redis      starting...
)

:: MongoDB
docker exec insvnter-mongo mongosh --eval "db.runCommand({ping:1})" --quiet >nul 2>&1
if %errorlevel% equ 0 (
    echo   [OK] MongoDB    localhost:27017 no auth
) else (
    echo   [..] MongoDB    starting...
)

echo ==================================================
echo.
echo [DONE] All databases are configured!
echo.
echo   Start backend:  cd insvnter-backend ^& mvn spring-boot:run
echo   Stop databases: docker compose -f docker-compose.db.yml down
echo   Wipe all data:  docker compose -f docker-compose.db.yml down -v
echo.
pause
