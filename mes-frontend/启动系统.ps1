# MES系统启动脚本
Write-Host "========================================" -ForegroundColor Green
Write-Host "MES智能生产系统启动" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# 检查Node.js
Write-Host "检查Node.js环境..." -ForegroundColor Yellow
try {
    $nodeVersion = node --version
    Write-Host "✅ Node.js版本: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Node.js未安装或未添加到PATH" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit
}

# 检查NPM
Write-Host "检查NPM环境..." -ForegroundColor Yellow
try {
    $npmVersion = npm --version
    Write-Host "✅ NPM版本: $npmVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ NPM未安装或未添加到PATH" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit
}

# 检查项目文件
Write-Host "检查项目文件..." -ForegroundColor Yellow
if (Test-Path "package.json") {
    Write-Host "✅ package.json存在" -ForegroundColor Green
} else {
    Write-Host "❌ package.json不存在，请确保在正确的目录下运行" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit
}

# 检查依赖
Write-Host "检查项目依赖..." -ForegroundColor Yellow
if (Test-Path "node_modules") {
    Write-Host "✅ 依赖已安装" -ForegroundColor Green
} else {
    Write-Host "⚠️ 依赖未安装，正在安装..." -ForegroundColor Yellow
    npm install
}

Write-Host ""
Write-Host "🚀 启动开发服务器..." -ForegroundColor Green
Write-Host ""

# 停止现有Node进程
Get-Process -Name "node" -ErrorAction SilentlyContinue | Stop-Process -Force

# 启动开发服务器
npm run dev