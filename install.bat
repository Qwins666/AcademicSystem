@echo off
echo 学术活动与科研成果管理系统安装脚本
echo =====================================

echo 1. 检查Java环境...
java -version
if %errorlevel% neq 0 (
    echo 错误：未找到Java环境，请先安装JDK 8或更高版本
    echo 下载地址：https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

echo 2. 检查Maven环境...
mvn -version
if %errorlevel% neq 0 (
    echo 错误：未找到Maven环境，请先安装Maven
    echo 下载地址：https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo 3. 检查Node.js环境...
node --version
if %errorlevel% neq 0 (
    echo 错误：未找到Node.js环境，请先安装Node.js
    echo 下载地址：https://nodejs.org/
    pause
    exit /b 1
)

echo 4. 安装后端依赖...
cd backend
mvn clean install
if %errorlevel% neq 0 (
    echo 错误：后端依赖安装失败
    pause
    exit /b 1
)
cd ..

echo 5. 安装前端依赖...
cd frontend
npm install
if %errorlevel% neq 0 (
    echo 错误：前端依赖安装失败
    pause
    exit /b 1
)
cd ..

echo 6. 创建日志目录...
mkdir logs 2>nul

echo 安装完成！
echo 请确保MySQL数据库已启动，并执行database/init.sql脚本初始化数据库
echo 然后运行start.bat启动系统
pause
