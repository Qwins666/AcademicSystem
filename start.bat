@echo off
echo 学术活动与科研成果管理系统启动脚本
echo =====================================

echo 1. 检查Java环境...
java -version
if %errorlevel% neq 0 (
    echo 错误：未找到Java环境，请先安装JDK 8或更高版本
    pause
    exit /b 1
)

echo 2. 检查Node.js环境...
node --version
if %errorlevel% neq 0 (
    echo 错误：未找到Node.js环境，请先安装Node.js
    pause
    exit /b 1
)

echo 3. 启动后端服务...
cd backend
start "后端服务" cmd /k "mvn spring-boot:run"
cd ..

echo 4. 等待后端服务启动...
timeout /t 10 /nobreak > nul

echo 5. 启动前端服务...
cd frontend
start "前端服务" cmd /k "npm run serve"
cd ..

echo 系统启动完成！
echo 后端服务地址：http://localhost:8080
echo 前端服务地址：http://localhost:8081
echo 请等待服务完全启动后访问前端地址
pause
