@echo off
chcp 65001 >nul
echo ========================================
echo MES智能生产系统 - 完整启动脚本
echo ========================================
echo.

echo 🚀 正在启动MES系统所有服务...
echo.

echo 1️⃣ 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java环境未安装或配置不正确
    echo 请安装Java 8或更高版本
    pause
    exit /b 1
)
echo ✅ Java环境正常

echo.
echo 2️⃣ 检查Node.js环境...
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js环境未安装
    echo 请安装Node.js 16或更高版本
    pause
    exit /b 1
)
echo ✅ Node.js环境正常

echo.
echo 3️⃣ 启动后端服务...

echo 正在启动网关服务...
cd /d "%~dp0\..\MES-demo\gateway"
start "MES Gateway" cmd /c "mvn spring-boot:run -Dspring-boot.run.jvmArguments=-Dserver.port=8080"

echo 等待网关启动...
timeout /t 5 /nobreak >nul

echo 正在启动认证服务...
cd /d "%~dp0\..\MES-demo\services\auth-service"
start "MES Auth Service" cmd /c "mvn spring-boot:run -Dspring-boot.run.jvmArguments=-Dserver.port=8081"

echo 正在启动设备管理服务...
cd /d "%~dp0\..\MES-demo\services\service-equipment"
start "MES Equipment Service" cmd /c "mvn spring-boot:run -Dspring-boot.run.jvmArguments=-Dserver.port=8082"

echo 正在启动工艺管理服务...
cd /d "%~dp0\..\MES-demo\services\service-process"
start "MES Process Service" cmd /c "mvn spring-boot:run -Dspring-boot.run.jvmArguments=-Dserver.port=8083"

echo 正在启动生产管理服务...
cd /d "%~dp0\..\MES-demo\services\service-production"
start "MES Production Service" cmd /c "mvn spring-boot:run -Dspring-boot.run.jvmArguments=-Dserver.port=8084"

echo.
echo 4️⃣ 等待后端服务启动完成...
echo 这可能需要1-2分钟，请耐心等待...
timeout /t 30 /nobreak >nul

echo.
echo 5️⃣ 启动前端服务...
cd /d "%~dp0"

echo 正在停止现有前端服务...
taskkill /f /im node.exe 2>nul

echo 正在启动前端开发服务器...
start "MES Frontend" cmd /c "npm run dev"

echo.
echo 6️⃣ 等待前端服务启动...
timeout /t 10 /nobreak >nul

echo.
echo 7️⃣ 检查服务状态...
echo 正在检查各服务健康状态...

powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080/actuator/health' -TimeoutSec 5; Write-Host '✅ 网关服务: 正常' } catch { Write-Host '❌ 网关服务: 异常' }"
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8081/actuator/health' -TimeoutSec 5; Write-Host '✅ 认证服务: 正常' } catch { Write-Host '❌ 认证服务: 异常' }"
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8082/api/health' -TimeoutSec 5; Write-Host '✅ 设备服务: 正常' } catch { Write-Host '❌ 设备服务: 异常' }"
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8083/api/health' -TimeoutSec 5; Write-Host '✅ 工艺服务: 正常' } catch { Write-Host '❌ 工艺服务: 异常' }"
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8084/api/health' -TimeoutSec 5; Write-Host '✅ 生产服务: 正常' } catch { Write-Host '❌ 生产服务: 异常' }"
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:5173' -TimeoutSec 5; Write-Host '✅ 前端服务: 正常' } catch { Write-Host '❌ 前端服务: 异常' }"

echo.
echo 8️⃣ 打开系统页面...
timeout /t 3 /nobreak >nul

echo 正在打开错误修复测试页面...
start http://localhost:5173/test-error-fixes.html

timeout /t 2 /nobreak >nul
echo 正在打开主系统页面...
start http://localhost:5173

echo.
echo ========================================
echo 🎉 MES系统启动完成！
echo ========================================
echo.
echo 📋 服务地址:
echo   前端系统: http://localhost:5173
echo   API网关: http://localhost:8080
echo   认证服务: http://localhost:8081
echo   设备服务: http://localhost:8082
echo   工艺服务: http://localhost:8083
echo   生产服务: http://localhost:8084
echo.
echo 🔧 测试页面:
echo   错误修复测试: http://localhost:5173/test-error-fixes.html
echo   系统功能测试: http://localhost:5173
echo.
echo 💡 提示:
echo   - 如果某些服务启动失败，请检查端口是否被占用
echo   - 首次启动可能需要下载依赖，请耐心等待
echo   - 可以通过任务管理器查看各服务进程状态
echo.
echo 按任意键退出...
pause >nul