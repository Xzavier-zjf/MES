@echo off
chcp 65001 >nul
echo ========================================
echo 启动MES模拟后端服务
echo ========================================
echo.

echo 检查Node.js环境...
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js环境未安装
    pause
    exit /b 1
)
echo ✅ Node.js环境正常

echo.
echo 检查依赖包...
if not exist node_modules\express (
    echo 正在安装必要依赖...
    npm install express cors
)

echo.
echo 🎨 创建测试图片...
node 创建测试图片.js

echo.
echo 🚀 启动模拟后端服务...
echo 服务将运行在 http://localhost:8080
echo 图片服务: http://localhost:8080/uploads/patterns/
echo.

node mock-server.js

pause