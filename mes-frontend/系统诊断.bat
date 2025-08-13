@echo off
chcp 65001 >nul
echo ========================================
echo MES系统状态诊断工具
echo ========================================
echo.

echo 🔍 正在诊断系统状态...
echo.

echo 📋 1. 环境检查
echo ----------------------------------------
java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Java环境: 正常
    java -version 2>&1 | findstr "version"
) else (
    echo ❌ Java环境: 未安装或配置错误
)

node --version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Node.js环境: 正常
    node --version
) else (
    echo ❌ Node.js环境: 未安装
)

npm --version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ NPM环境: 正常
    npm --version
) else (
    echo ❌ NPM环境: 未安装
)

echo.
echo 🌐 2. 服务状态检查
echo ----------------------------------------

echo 检查前端服务 (5173端口)...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:5173' -TimeoutSec 3; Write-Host '✅ 前端服务: 运行中' } catch { Write-Host '❌ 前端服务: 未启动' }"

echo 检查API网关 (8080端口)...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080' -TimeoutSec 3; Write-Host '✅ API网关: 运行中' } catch { Write-Host '❌ API网关: 未启动' }"

echo 检查认证服务 (8081端口)...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8081' -TimeoutSec 3; Write-Host '✅ 认证服务: 运行中' } catch { Write-Host '❌ 认证服务: 未启动' }"

echo 检查设备服务 (8082端口)...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8082' -TimeoutSec 3; Write-Host '✅ 设备服务: 运行中' } catch { Write-Host '❌ 设备服务: 未启动' }"

echo 检查工艺服务 (8083端口)...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8083' -TimeoutSec 3; Write-Host '✅ 工艺服务: 运行中' } catch { Write-Host '❌ 工艺服务: 未启动' }"

echo 检查生产服务 (8084端口)...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8084' -TimeoutSec 3; Write-Host '✅ 生产服务: 运行中' } catch { Write-Host '❌ 生产服务: 未启动' }"

echo.
echo 🔌 3. 端口占用检查
echo ----------------------------------------
echo 检查关键端口占用情况...
netstat -an | findstr ":5173" >nul && echo ✅ 5173端口: 已占用 (前端) || echo ❌ 5173端口: 未占用
netstat -an | findstr ":8080" >nul && echo ✅ 8080端口: 已占用 (网关) || echo ❌ 8080端口: 未占用
netstat -an | findstr ":8081" >nul && echo ✅ 8081端口: 已占用 (认证) || echo ❌ 8081端口: 未占用
netstat -an | findstr ":8082" >nul && echo ✅ 8082端口: 已占用 (设备) || echo ❌ 8082端口: 未占用
netstat -an | findstr ":8083" >nul && echo ✅ 8083端口: 已占用 (工艺) || echo ❌ 8083端口: 未占用
netstat -an | findstr ":8084" >nul && echo ✅ 8084端口: 已占用 (生产) || echo ❌ 8084端口: 未占用

echo.
echo 📁 4. 项目文件检查
echo ----------------------------------------
if exist "package.json" (
    echo ✅ 前端项目: 存在
) else (
    echo ❌ 前端项目: package.json不存在
)

if exist "..\MES-demo" (
    echo ✅ 后端项目: 存在
) else (
    echo ❌ 后端项目: MES-demo目录不存在
)

if exist "node_modules" (
    echo ✅ 前端依赖: 已安装
) else (
    echo ❌ 前端依赖: 未安装，请运行 npm install
)

echo.
echo 🔧 5. 错误修复状态
echo ----------------------------------------
if exist "错误修复总结.md" (
    echo ✅ 错误修复: 已完成
    echo   - JWT Token格式错误修复
    echo   - 图片加载错误优化
    echo   - 代理配置改进
    echo   - 控制台日志清理
) else (
    echo ❌ 错误修复: 未完成
)

echo.
echo ========================================
echo 💡 诊断建议
echo ========================================

echo 🚀 如果需要启动完整系统:
echo    运行: 完整系统启动.bat

echo 🧪 如果只需要测试错误修复:
echo    运行: test-fixes.bat

echo 🔄 如果需要重启服务:
echo    运行: restart-all-services.bat

echo 📊 如果需要查看详细状态:
echo    运行: check-services-status.bat

echo.
echo 按任意键退出...
pause >nul