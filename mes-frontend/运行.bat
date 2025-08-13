@echo off
echo 正在启动MES系统...
echo.

echo 1. 停止现有服务...
taskkill /f /im node.exe 2>nul

echo 2. 启动前端服务...
npm run dev

pause