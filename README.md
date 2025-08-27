# 注塑/印刷生产管理系统 (MES)

## 项目简介
MES (Manufacturing Execution System) 是一个数字化管理注塑和印刷生产线的系统，旨在优化生产计划、设备监控和工艺参数管理的全流程工作。系统采用微服务架构，支持高并发和分布式部署。

## 特性
- **生产计划管理**：支持灵活的生产计划和任务分配。
- **设备监控**：实时获取设备状态，支持 OEE 分析。
- **工艺管理**：注塑参数和印刷图案的设置与版本控制。
- **权限系统**：基于角色的权限管理。
- **前后端分离**：使用 Vue.js 和 Spring Boot 实现。

## 技术栈
- **前端**：Vue.js 3.x, Vite, Element Plus
- **后端**：Spring Boot 3.4.3, Nacos, MySQL 8.0
- **开发语言**：Java, JavaScript, HTML, CSS
- **环境依赖**：
  - JDK 17+
  - Node.js 16+
  - MySQL 8.0+
  - Nacos 2.4.3

## 项目结构
```
MES/
├── MES-demo/               # 后端服务代码
│   ├── auth-service/       # 用户认证服务
│   ├── service-production/ # 生产管理服务
│   ├── service-equipment/  # 设备管理服务
│   ├── service-process/    # 工艺管理服务
│   ├── common-module/      # 公共模块
├── mes-frontend/           # 前端代码
│   ├── src/                # 前端源代码
│   ├── vite.config.js      # Vite 配置文件
│   ├── package.json        # 前端依赖配置
│   ├── index.html          # 前端入口文件
├── debase/                 # 数据库文档
├── 修复方案/               # 错误修复文档
└── README.md               # 项目说明文件
```

## 安装与运行

### 1. 克隆代码
```bash
git clone https://github.com/Xzavier-zjf/MES.git
cd MES
```

### 2. 数据库配置
1. 确保已安装 MySQL 8.0+。
2. 使用 `debase/MES_unified_database.sql` 初始化数据库：
   ```sql
   -- 在 MySQL 执行以下命令：
   CREATE DATABASE MES;
   USE MES;
   SOURCE MES_unified_database.sql;
   ```

### 3. 启动后端服务
按以下顺序启动后端服务：
```bash
# 启动 Nacos
nacos/bin/startup.cmd -m standalone

# 启动后端服务
cd MES-demo/auth-service && mvn spring-boot:run
cd ../service-production && mvn spring-boot:run
# 其他模块类似...
```

### 4. 启动前端服务
```bash
cd mes-frontend
npm install
npm run dev
```

访问前端地址：[http://localhost:5173](http://localhost:5173)

## 系统功能
### 核心模块
1. **认证服务**：用户登录与权限管理。
2. **生产服务**：生产计划与生产任务管理。
3. **设备服务**：设备状态监控与 OEE 分析。
4. **工艺服务**：管理注塑工艺参数和印刷图案。

### 前端功能
- 实时数据可视化（基于 Vue-ECharts）。
- 多语言支持和动态主题切换。

## 开发指南
### 前端开发
1. 安装依赖：
   ```bash
   npm install
   ```
2. 启动开发服务器：
   ```bash
   npm run dev
   ```

### 后端开发
1. 导入 Maven 项目。
2. 修改配置文件中的 MySQL 数据库连接 URL：
   ```
   jdbc:mysql://localhost:3306/MES
   ```
3. 启动服务：
   ```bash
   mvn spring-boot:run
   ```

## 已知问题与修复
详见 [修复方案/错误修复总结.md](./修复方案/错误修复总结.md)。

## 联系方式
如有问题，请联系项目负责人：
- GitHub: [Xzavier-zjf](https://github.com/Xzavier-zjf)
- 邮箱: mes_admin@shoujike.com
