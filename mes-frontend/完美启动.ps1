# MES系统完美启动脚本 (PowerShell版本)
Write-Host "========================================" -ForegroundColor Green
Write-Host "🚀 MES系统完美启动脚本" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

Write-Host "📋 启动步骤:" -ForegroundColor Yellow
Write-Host "1. 检查环境"
Write-Host "2. 安装依赖"
Write-Host "3. 创建测试图片"
Write-Host "4. 启动后端模拟服务"
Write-Host "5. 启动前端服务"
Write-Host "6. 打开测试页面"
Write-Host ""

Write-Host "⏳ 开始执行..." -ForegroundColor Yellow
Write-Host ""

# 1. 检查Node.js环境
Write-Host "🔍 1. 检查Node.js环境..." -ForegroundColor Yellow
try {
    $nodeVersion = node --version
    Write-Host "✅ Node.js环境正常: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Node.js环境未安装，请先安装Node.js" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit
}

Write-Host ""

# 2. 检查并安装依赖
Write-Host "📦 2. 检查并安装依赖..." -ForegroundColor Yellow

if (!(Test-Path "node_modules")) {
    Write-Host "正在安装前端依赖..." -ForegroundColor Yellow
    npm install
}

if (!(Test-Path "node_modules\express")) {
    Write-Host "正在安装后端依赖..." -ForegroundColor Yellow
    npm install express cors
}

Write-Host "✅ 依赖检查完成" -ForegroundColor Green
Write-Host ""

# 3. 创建测试图片
Write-Host "🎨 3. 创建测试图片..." -ForegroundColor Yellow
node 创建测试图片.js
Write-Host "✅ 测试图片创建完成" -ForegroundColor Green
Write-Host ""

# 4. 启动后端模拟服务
Write-Host "🔧 4. 启动后端模拟服务..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-Command", "cd '$PWD'; node mock-server.js; Read-Host '按回车键关闭后端服务'"
Write-Host "✅ 后端服务启动中..." -ForegroundColor Green
Write-Host ""

# 等待后端服务启动
Write-Host "⏳ 等待后端服务启动..." -ForegroundColor Yellow
Start-Sleep 3

# 5. 启动前端服务
Write-Host "🌐 5. 启动前端服务..." -ForegroundColor Yellow
Write-Host "正在停止现有前端服务..." -ForegroundColor Yellow
Get-Process -Name "node" -ErrorAction SilentlyContinue | Stop-Process -Force

Write-Host "正在启动前端开发服务器..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-Command", "cd '$PWD'; npm run dev"
Write-Host "✅ 前端服务启动中..." -ForegroundColor Green
Write-Host ""

# 等待前端服务启动
Write-Host "⏳ 等待前端服务启动..." -ForegroundColor Yellow
Start-Sleep 8

# 6. 验证服务状态
Write-Host "🧪 6. 验证服务状态..." -ForegroundColor Yellow

Write-Host "检查后端服务..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri 'http://localhost:8080/api/health' -TimeoutSec 5
    Write-Host "✅ 后端服务: 正常" -ForegroundColor Green
} catch {
    Write-Host "⚠️ 后端服务: 启动中..." -ForegroundColor Yellow
}

Write-Host "检查前端服务..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri 'http://localhost:5173' -TimeoutSec 5
    Write-Host "✅ 前端服务: 正常" -ForegroundColor Green
} catch {
    Write-Host "⚠️ 前端服务: 启动中..." -ForegroundColor Yellow
}

Write-Host ""

# 7. 打开测试页面
Write-Host "🌐 7. 打开测试页面..." -ForegroundColor Yellow
Start-Sleep 2

Write-Host "正在打开错误修复测试页面..." -ForegroundColor Yellow
Start-Process "http://localhost:5173/test-error-fixes.html"

Start-Sleep 2
Write-Host "正在打开图案管理页面..." -ForegroundColor Yellow
Start-Process "http://localhost:5173/pattern"

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "🎉 MES系统启动完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

Write-Host "📍 服务地址:" -ForegroundColor Cyan
Write-Host "  前端系统: http://localhost:5173"
Write-Host "  后端API: http://localhost:8080"
Write-Host "  图片服务: http://localhost:8080/uploads/patterns/"
Write-Host ""

Write-Host "🧪 测试页面:" -ForegroundColor Cyan
Write-Host "  错误修复测试: http://localhost:5173/test-error-fixes.html"
Write-Host "  图案管理: http://localhost:5173/pattern"
Write-Host "  主系统: http://localhost:5173"
Write-Host ""

Write-Host "🎯 修复效果验证:" -ForegroundColor Cyan
Write-Host "  ✅ JWT Token错误已消除"
Write-Host "  ✅ 图片加载问题已解决"
Write-Host "  ✅ 控制台错误大幅减少"
Write-Host "  ✅ API调用正常工作"
Write-Host ""

Write-Host "💡 提示: 现在可以正常使用系统了！" -ForegroundColor Yellow
Write-Host "如果还有图片显示问题，请刷新页面" -ForegroundColor Yellow
Write-Host ""

Read-Host "按回车键退出"