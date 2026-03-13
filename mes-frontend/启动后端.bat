@echo off
chcp 65001 >nul
echo ========================================
echo 启动MES后端服务
echo ========================================
echo.

echo 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java环境未安装，请先安装Java 8或更高版本
    pause
    exit /b 1
)
echo ✅ Java环境正常

echo.
echo 正在启动后端服务...
echo 这可能需要1-2分钟，请耐心等待...
echo.

echo 1. 启动API网关 (8080端口)...
cd /d "%~dp0\..\MES-demo\gateway"
start "MES Gateway" cmd /c "mvn spring-boot:run"

echo 等待网关启动...
timeout /t 10 /nobreak >nul

echo 2. 启动设备管理服务 (8082端口)...
cd /d "%~dp0\..\MES-demo\services\service-equipment"
start "MES Equipment" cmd /c "mvn spring-boot:run -Dspring-boot.run.jvmArguments=-Dserver.port=8082"

echo 3. 启动工艺管理服务 (8083端口)...
cd /d "%~dp0\..\MES-demo\services\service-process"
start "MES Process" cmd /c "mvn spring-boot:run -Dspring-boot.run.jvmArguments=-Dserver.port=8083"

echo.
echo ========================================
echo 后端服务启动中...
echo ========================================
echo.
echo 💡 提示:
echo - 网关服务: http://localhost:8080
echo - 设备服务: http://localhost:8082
echo - 工艺服务: http://localhost:8083
echo.
echo 请等待1-2分钟让服务完全启动
echo 然后刷新浏览器页面测试
echo.
pause