# MES 项目

## 项目简介
MES（制造执行系统，Manufacturing Execution System）项目旨在为制造企业提供实时生产数据监控、生产流程管理和分析功能。通过该系统，用户可以有效地优化生产效率，跟踪制造过程，并提高整体运营效率。

## 特性
- **实时监控**：实时跟踪生产设备的状态和产量。
- **数据收集和分析**：收集生产数据，生成详细的统计报告。
- **用户友好界面**：提供直观的操作界面，降低使用门槛。
- **系统集成**：支持与现有的企业资源规划（ERP）系统无缝集成。

## 项目结构
该项目主要使用以下技术栈：
- **前端**：Vue.js, HTML, CSS
- **后端**：Java, JavaScript
- **脚本**：Batchfile, DIGITAL Command Language

## 安装指南
以下是安装和运行 MES 项目的步骤：

1. **克隆仓库**：
   ```bash
   git clone https://github.com/Xzavier-zjf/MES.git
   cd MES
   ```

2. **安装依赖**：
   根据项目需求安装前端和后端依赖：
   ```bash
   # 前端依赖安装
   cd frontend
   npm install

   # 后端依赖安装
   cd ../backend
   mvn install
   ```

3. **运行项目**：
   ```bash
   # 启动后端服务
   cd backend
   java -jar target/backend.jar

   # 启动前端服务
   cd frontend
   npm run serve
   ```

4. **访问项目**：
   在浏览器中访问 `http://localhost:8080`。

## 使用说明
用户可以通过以下模块管理制造流程：
- **仪表盘**：查看实时数据和关键性能指标。
- **生产计划**：创建和管理生产任务。
- **数据分析**：生成并导出生产报告。

## 贡献指南
欢迎社区开发者参与到 MES 项目的建设中：
1. 提交问题（Issues）或功能建议。
2. 创建分支开发新功能。
3. 提交合并请求（Pull Requests）。

贡献步骤：
```bash
# 创建新分支
git checkout -b feature-branch

# 提交修改
git commit -m "描述修改内容"

# 推送到远程仓库
git push origin feature-branch

# 创建 Pull Request
```

## 许可证
该项目采用 MIT 许可证。详情请参考 [LICENSE](./LICENSE) 文件。

## 联系方式
如有任何问题或建议，请联系项目维护者：
- GitHub: [Xzavier-zjf](https://github.com/Xzavier-zjf)
- 邮箱: 2114086570@qq.com
