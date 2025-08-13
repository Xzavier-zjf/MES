@echo off
title MES环境检查
color 0A

echo ========================================
echo MES系统环境检查
echo ========================================
echo.

echo 检查当前目录...
echo 当前目录: %CD%
echo.

echo 检查Node.js...
node --version
if %errorlevel% neq 0 (
    echo 错误: Node.js未安装或未添加到PATH
    goto :error
)

echo 检查NPM...
npm --version
if %errorlevel% neq 0 (
    echo 错误: NPM未安装或未添加到PATH
    goto :error
)

echo 检查package.json...
if not exist package.json (
    echo 错误: 当前目录没有package.json文件
    echo 请确保在mes-frontend目录下运行
    goto :error
)

echo 检查node_modules...
if not exist node_modules (
    echo 警告: node_modules不存在，需要安装依赖
    echo 正在安装依赖...
    npm install
)

echo.
echo ========================================
echo 环境检查完成，准备启动服务...
echo ========================================
echo.

echo 启动开发服务器...
npm run dev

goto :end

:error
echo.
echo ========================================
echo 发现问题，请检查以上错误信息
echo ========================================
pause
goto :end

:end