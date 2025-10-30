#!/bin/bash

echo "学术活动与科研成果管理系统启动脚本"
echo "====================================="

# 检查Java环境
echo "1. 检查Java环境..."
if ! command -v java &> /dev/null; then
    echo "错误：未找到Java环境，请先安装JDK 8或更高版本"
    exit 1
fi
java -version

# 检查Node.js环境
echo "2. 检查Node.js环境..."
if ! command -v node &> /dev/null; then
    echo "错误：未找到Node.js环境，请先安装Node.js"
    exit 1
fi
node --version

# 启动后端服务
echo "3. 启动后端服务..."
cd backend
nohup mvn spring-boot:run > ../logs/backend.log 2>&1 &
BACKEND_PID=$!
cd ..

# 等待后端服务启动
echo "4. 等待后端服务启动..."
sleep 10

# 启动前端服务
echo "5. 启动前端服务..."
cd frontend
nohup npm run serve > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!
cd ..

# 创建日志目录
mkdir -p logs

echo "系统启动完成！"
echo "后端服务地址：http://localhost:8080"
echo "前端服务地址：http://localhost:8081"
echo "后端进程ID：$BACKEND_PID"
echo "前端进程ID：$FRONTEND_PID"
echo "日志文件：logs/backend.log, logs/frontend.log"
echo "请等待服务完全启动后访问前端地址"

# 保存进程ID
echo $BACKEND_PID > logs/backend.pid
echo $FRONTEND_PID > logs/frontend.pid
