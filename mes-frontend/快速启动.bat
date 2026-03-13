@echo off
chcp 65001 >nul
echo ========================================
echo 🚀 MES系统快速启动
echo ========================================
echo.

echo 📦 安装依赖...
npm install express cors --silent

echo.
echo 🎨 创建测试图片...
node 创建测试图片.js

echo.
echo 🔧 启动后端服务 (8080端口)...
echo 请保持此窗口打开！
echo 后端服务地址: http://localhost:8080
echo 图片服务地址: http://localhost:8080/uploads/patterns/
echo.
echo 现在请在另一个窗口运行前端服务: npm run dev
echo.

node mock-server.js

pause