# MES系统CORS问题完整解决方案

## 🔍 问题分析

### 原始问题
- **错误信息**: `Access to XMLHttpRequest blocked by CORS policy`
- **具体表现**: 前端页面空白，登录失败，API请求404错误
- **根本原因**: 微服务架构中的路由配置问题

### 架构发现
通过深入分析发现这是一个**微服务架构**，不是单体应用：

```
前端 (5173) → 网关 (8080) → 各个微服务
                    ├── 认证服务 (8101)
                    ├── 生产服务 (8104)  
                    ├── 设备服务 (8102)
                    └── 工艺服务 (8103)
```

## ✅ 解决方案

### 1. 网关路由配置修复

**文件**: `MES-demo/gateway/src/main/resources/application.yml`

**修改前**:
```yaml
- id: service-auth
  uri: lb://auth-service
  predicates:
    - Path=/auth/**
```

**修改后**:
```yaml
- id: service-auth
  uri: lb://auth-service
  predicates:
    - Path=/api/auth/**
```

**说明**: 前端请求路径是 `/api/auth/**`，但网关配置的是 `/auth/**`，导致路由匹配失败。

### 2. 前端代理配置

**文件**: `mes-frontend/vite.config.js`

```javascript
proxy: {
  '/api': {
    target: 'http://localhost:8080',  // 指向网关服务
    changeOrigin: true,
    secure: false
  }
}
```

### 3. 环境变量配置

**文件**: `mes-frontend/.env`

```properties
VITE_API_BASE_URL=
```

**说明**: 使用空字符串让前端使用相对路径，通过Vite代理转发。

### 4. 后端CORS配置

**文件**: `MES-demo/services/auth-service/src/main/java/com/example/authservice/controller/AuthController.java`

```java
@CrossOrigin(origins = {"http://localhost:5173", "http://192.168.219.130:5173"}, allowCredentials = "true")
```

**文件**: `MES-demo/services/auth-service/src/main/java/com/example/authservice/config/CorsConfig.java`

全局CORS配置类，支持所有跨域请求。

## 🚀 部署步骤

### 1. 重启网关服务
```bash
# 在IDEA中停止并重新启动 GatewayApplication
# 或者使用命令行重启
```

### 2. 重启前端服务
```bash
# 运行重启脚本
mes-frontend/restart-services.bat

# 或手动执行
cd mes-frontend
npm run dev
```

### 3. 验证修复效果
- 访问: http://localhost:5173/microservice-debug.html
- 检查所有微服务状态
- 测试登录功能

## 📋 服务端口映射

| 服务名称 | 端口 | 说明 |
|---------|------|------|
| 前端服务 | 5173 | Vue.js开发服务器 |
| 网关服务 | 8080 | Spring Cloud Gateway |
| 认证服务 | 8101 | 用户认证和授权 |
| 设备服务 | 8102 | 设备管理 |
| 工艺服务 | 8103 | 工艺管理 |
| 生产服务 | 8104 | 生产管理 |
| Nacos | 8848 | 服务注册与发现 |

## 🛠️ 调试工具

### 1. 微服务状态检查
- **页面**: http://localhost:5173/microservice-debug.html
- **功能**: 检查所有微服务运行状态，测试路由配置

### 2. 服务重启工具
- **脚本**: `mes-frontend/restart-services.bat`
- **功能**: 自动检查服务状态，重启前端服务

### 3. 快速修复工具
- **脚本**: `mes-frontend/quick-fix.bat`
- **功能**: 一键修复常见配置问题

## 🔧 常见问题排查

### 问题1: 仍然出现404错误
**解决方案**: 
1. 确认网关服务已重启
2. 检查Nacos中服务注册状态
3. 验证路由配置是否正确

### 问题2: CORS错误持续出现
**解决方案**:
1. 清除浏览器缓存
2. 重启前端开发服务器
3. 检查网关CORS配置

### 问题3: 微服务无法注册到Nacos
**解决方案**:
1. 确认Nacos服务运行正常
2. 检查各服务的Nacos配置
3. 查看服务启动日志

## 📝 验证清单

- [ ] Nacos注册中心运行正常 (8848)
- [ ] 网关服务运行正常 (8080)
- [ ] 认证服务运行正常 (8101)
- [ ] 前端服务运行正常 (5173)
- [ ] 网关路由配置已更新
- [ ] 前端代理配置正确
- [ ] 浏览器缓存已清除
- [ ] 登录功能测试通过

## 🎯 最终效果

修复完成后：
- ✅ 页面正常加载，不再空白
- ✅ API请求正常，无CORS错误
- ✅ 登录功能正常工作
- ✅ 微服务间通信正常

---

**注意**: 这是一个微服务架构项目，需要确保所有相关服务都正常运行才能完整使用系统功能。