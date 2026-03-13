@echo off
chcp 65001 >nul
echo ========================================
echo 🌐 启动MES前端服务
echo ========================================
echo.

echo 停止现有Node进程...
taskkill /f /im node.exe 2>nul

echo.
echo 启动前端开发服务器...
echo 前端地址: http://localhost:5173
echo.

npm run dev

pause