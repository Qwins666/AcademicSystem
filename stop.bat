@echo off
echo 停止学术活动与科研成果管理系统
echo =====================================

echo 1. 停止前端服务...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8081') do (
    taskkill /f /pid %%a >nul 2>&1
)

echo 2. 停止后端服务...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080') do (
    taskkill /f /pid %%a >nul 2>&1
)

echo 系统已停止！
pause
