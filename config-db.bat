@echo off
chcp 65001 >nul 2>&1
setlocal EnableDelayedExpansion

echo ==================================================
echo   InsvnterAI - Database Configuration
echo ==================================================
echo.
echo   Leave blank to use default value shown in [brackets]
echo.

:: ============ MariaDB ============
echo --- MariaDB ---
set /p "DB_HOST=  Host [localhost]: "
if "!DB_HOST!"=="" set "DB_HOST=localhost"

set /p "DB_PORT=  Port [3306]: "
if "!DB_PORT!"=="" set "DB_PORT=3306"

set /p "DB_NAME=  Database [insvnter_ai]: "
if "!DB_NAME!"=="" set "DB_NAME=insvnter_ai"

set /p "DB_USER=  Username [root]: "
if "!DB_USER!"=="" set "DB_USER=root"

set /p "DB_PASS=  Password [root]: "
if "!DB_PASS!"=="" set "DB_PASS=root"

echo.

:: ============ Redis ============
echo --- Redis ---
set /p "REDIS_HOST=  Host [localhost]: "
if "!REDIS_HOST!"=="" set "REDIS_HOST=localhost"

set /p "REDIS_PORT=  Port [6379]: "
if "!REDIS_PORT!"=="" set "REDIS_PORT=6379"

set /p "REDIS_PASS=  Password [empty]: "
if "!REDIS_PASS!"=="" set "REDIS_PASS="

set /p "REDIS_DB=  Database index [0]: "
if "!REDIS_DB!"=="" set "REDIS_DB=0"

echo.

:: ============ MongoDB ============
echo --- MongoDB ---
set /p "MONGO_HOST=  Host [localhost]: "
if "!MONGO_HOST!"=="" set "MONGO_HOST=localhost"

set /p "MONGO_PORT=  Port [27017]: "
if "!MONGO_PORT!"=="" set "MONGO_PORT=27017"

set /p "MONGO_DB=  Database [insvnter_ai]: "
if "!MONGO_DB!"=="" set "MONGO_DB=insvnter_ai"

echo.

:: ============ JWT ============
echo --- JWT ---
set /p "JWT_SECRET=  Secret [auto-generate]: "
if "!JWT_SECRET!"=="" (
    powershell -NoProfile -Command "[guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N')" > "%TEMP%\jwt_tmp.txt"
    set /p JWT_SECRET=<"%TEMP%\jwt_tmp.txt"
    del "%TEMP%\jwt_tmp.txt" 2>nul
    echo   Generated: !JWT_SECRET!
)

set /p "JWT_EXP=  Expiration ms [86400000 = 24h]: "
if "!JWT_EXP!"=="" set "JWT_EXP=86400000"

echo.

:: ============ Server ============
echo --- Server ---
set /p "SERVER_PORT=  Port [8080]: "
if "!SERVER_PORT!"=="" set "SERVER_PORT=8080"

echo.

:: ============ Write .env ============
set "ENV_FILE=%~dp0insvnter-backend\.env"

(
    echo # InsvnterAI Backend Environment Config
    echo # Generated at %date% %time%
    echo.
    echo # MariaDB
    echo DB_HOST=!DB_HOST!
    echo DB_PORT=!DB_PORT!
    echo DB_NAME=!DB_NAME!
    echo DB_USERNAME=!DB_USER!
    echo DB_PASSWORD=!DB_PASS!
    echo.
    echo # Redis
    echo REDIS_HOST=!REDIS_HOST!
    echo REDIS_PORT=!REDIS_PORT!
    echo REDIS_PASSWORD=!REDIS_PASS!
    echo REDIS_DB=!REDIS_DB!
    echo.
    echo # MongoDB
    echo MONGO_URI=mongodb://!MONGO_HOST!:!MONGO_PORT!/!MONGO_DB!
    echo.
    echo # JWT
    echo JWT_SECRET=!JWT_SECRET!
    echo JWT_EXPIRATION=!JWT_EXP!
    echo.
    echo # Server
    echo SERVER_PORT=!SERVER_PORT!
) > "!ENV_FILE!"

echo ==================================================
echo   Config saved to: insvnter-backend\.env
echo ==================================================
echo.
echo   MariaDB:  !DB_USER!@!DB_HOST!:!DB_PORT!/!DB_NAME!
echo   Redis:    !REDIS_HOST!:!REDIS_PORT!/!REDIS_DB!
echo   MongoDB:  !MONGO_HOST!:!MONGO_PORT!/!MONGO_DB!
echo   Server:   localhost:!SERVER_PORT!
echo.
echo   Start backend with: start-backend.bat
echo   The .env file will be auto-loaded.
echo.
pause
