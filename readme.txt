# 注塑/印刷生产管理系统(MES)

## 项目概述
数字化管理注塑和印刷生产线，实现生产计划跟踪、设备监控和工艺管理的全流程系统。

## 技术栈
- 核心框架: Spring Boot 3.4.3
- 服务发现: Nacos 2.4.3
- 数据库: MySQL 8.0
- JDK版本: 17
- 前端技术: Vue.js 3.x

## 系统架构
```
[网关层] → [认证服务] → [业务服务]
    ↑           ↑            ↑
[前端应用]    [Nacos]    [MySQL集群]
```

## 核心模块
1. **gateway**: API网关，路由转发和跨域处理
2. **auth-service**: 用户认证和权限管理
3. **service-production**: 生产计划与任务管理
4. **service-equipment**: 设备监控与OEE分析  
5. **service-process**: 工艺参数与模板管理
6. **common-module**: 公共组件和DTO定义

## 快速启动
1. 安装依赖环境：
   - JDK 17+
   - MySQL 8.0+
   - Nacos 2.4.3

2. 启动顺序：
```bash
# 1. 启动Nacos
startup.cmd -m standalone

# 2. 启动各服务模块
cd gateway && mvn spring-boot:run
cd auth-service && mvn spring-boot:run
# 其他模块类似...
```

3，前端启动
先安装：npm i axios
后启动：npm run dev

## 接口文档
访问 `http://localhost:8080/swagger-ui.html` 查看各模块API文档

## 版本历史
- v1.0.0 (2024-03-15): 初始版本，实现基础功能
- v1.1.0 (2024-04-10): 增加OEE计算功能

