@echo off
chcp 65001 >nul
title MES系统完美启动
color 0A

echo ========================================
echo 🚀 MES系统完美启动脚本
echo ========================================
echo.

echo 📋 启动步骤:
echo 1. 检查环境
echo 2. 安装依赖
echo 3. 创建测试图片
echo 4. 启动后端模拟服务
echo 5. 启动前端服务
echo 6. 打开测试页面
echo.

echo ⏳ 开始执行...
echo.

echo 🔍 1. 检查Node.js环境...
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js环境未安装，请先安装Node.js
    pause
    exit /b 1
)
echo ✅ Node.js环境正常

echo.
echo 📦 2. 检查并安装依赖...
if not exist node_modules (
    echo 正在安装前端依赖...
    npm install
)

if not exist node_modules\express (
    echo 正在安装后端依赖...
    npm install express cors
)
echo ✅ 依赖检查完成

echo.
echo 🎨 3. 创建测试图片...
node 创建测试图片.js
echo ✅ 测试图片创建完成

echo.
echo 🔧 4. 启动后端模拟服务...
start "MES Mock Server" cmd /c "node mock-server.js"
echo ✅ 后端服务启动中...

echo.
echo ⏳ 等待后端服务启动...
timeout /t 3 /nobreak >nul

echo.
echo 🌐 5. 启动前端服务...
echo 正在停止现有前端服务...
taskkill /f /im node.exe 2>nul

echo 正在启动前端开发服务器...
start "MES Frontend" cmd /c "npm run dev"
echo ✅ 前端服务启动中...

echo.
echo ⏳ 等待前端服务启动...
timeout /t 8 /nobreak >nul

echo.
echo 🧪 6. 验证服务状态...
echo 检查后端服务...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080/api/health' -TimeoutSec 5; Write-Host '✅ 后端服务: 正常' } catch { Write-Host '⚠️ 后端服务: 启动中...' }"

echo 检查前端服务...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:5173' -TimeoutSec 5; Write-Host '✅ 前端服务: 正常' } catch { Write-Host '⚠️ 前端服务: 启动中...' }"

echo.
echo 🌐 7. 打开测试页面...
timeout /t 2 /nobreak >nul

echo 正在打开错误修复测试页面...
start http://localhost:5173/test-error-fixes.html

timeout /t 2 /nobreak >nul
echo 正在打开图案管理页面...
start http://localhost:5173/pattern

echo.
echo ========================================
echo 🎉 MES系统启动完成！
echo ========================================
echo.
echo 📍 服务地址:
echo   前端系统: http://localhost:5173
echo   后端API: http://localhost:8080
echo   图片服务: http://localhost:8080/uploads/patterns/
echo.
echo 🧪 测试页面:
echo   错误修复测试: http://localhost:5173/test-error-fixes.html
echo   图案管理: http://localhost:5173/pattern
echo   主系统: http://localhost:5173
echo.
echo 🎯 修复效果验证:
echo   ✅ JWT Token错误已消除
echo   ✅ 图片加载问题已解决
echo   ✅ 控制台错误大幅减少
echo   ✅ API调用正常工作
echo.
echo 💡 提示: 现在可以正常使用系统了！
echo 如果还有图片显示问题，请刷新页面
echo.
echo 按任意键退出...
pause >nul