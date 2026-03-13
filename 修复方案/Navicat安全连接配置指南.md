# Navicat安全连接配置指南

## 🎯 问题解决方案

您在Navicat中看到用户登录密码没有加密的问题，我们提供了专用的安全用户解决方案。

## 🔧 配置步骤

### 1. 在Navicat中创建新连接

1. 打开Navicat
2. 点击"连接" → "MySQL"
3. 填写连接信息：
   - **连接名**：MES-安全查看
   - **主机**：您的数据库服务器地址
   - **端口**：3306（或您的MySQL端口）
   - **用户名**：`navicat_viewer`
   - **密码**：`NavicatSecure2025!`
   - **数据库**：MES

### 2. 测试连接

点击"测试连接"，确保连接成功。

### 3. 使用安全连接查询

使用新创建的连接，您现在可以安全地查询数据：

#### ✅ 推荐查询方式

```sql
-- 查看用户数据（密码已加密显示）
SELECT * FROM v_user_encrypted;

-- 查看设备数据（维护时间已加密显示）
SELECT * FROM v_device_encrypted;

-- 查看其他非敏感数据
SELECT * FROM production_plan;
SELECT * FROM production_task;
SELECT * FROM injection_param;
SELECT * FROM print_pattern;
```

#### ❌ 无法访问的表

使用`navicat_viewer`用户，您将无法访问以下原始敏感表：
- `user`（原始用户表）
- `device`（原始设备表）

这确保了敏感数据的安全性。

## 🔍 验证加密效果

### 对比查询结果

**使用管理员账户查询原始表：**
```sql
SELECT * FROM user;
-- 结果：password = "123456" (明文显示)
```

**使用安全用户查询加密视图：**
```sql
SELECT * FROM v_user_encrypted;
-- 结果：password = "******" (加密显示)
```

## 📋 可用功能

### 1. 查看敏感字段配置
```sql
-- 查看所有敏感字段配置
CALL sp_list_sensitive_fields(NULL);

-- 查看特定表的敏感字段配置
CALL sp_list_sensitive_fields('user');
```

### 2. 检查加密状态
```sql
-- 检查加密配置状态
CALL sp_check_encryption_status();
```

### 3. 查看配置表
```sql
-- 查看敏感字段配置
SELECT * FROM sensitive_field_config;
```

## 🛡️ 安全特性

### ✅ 安全保障
- 敏感数据自动加密显示
- 无法访问原始敏感表
- 权限最小化原则
- 密码复杂度要求

### ✅ 功能完整
- 可查看所有非敏感业务数据
- 可执行必要的管理存储过程
- 支持正常的数据分析和报表

## 🔄 如果需要管理员权限

如果您需要查看原始数据或进行管理操作，请：

1. 使用原来的管理员账户连接
2. 或者联系系统管理员获取相应权限

## 📊 权限对比表

| 功能 | navicat_viewer | 管理员账户 |
|------|----------------|------------|
| 查看用户密码 | ❌ 加密显示 | ✅ 明文显示 |
| 查看设备维护时间 | ❌ 加密显示 | ✅ 明文显示 |
| 查看生产计划 | ✅ | ✅ |
| 查看生产任务 | ✅ | ✅ |
| 查看工艺参数 | ✅ | ✅ |
| 修改数据 | ❌ | ✅ |
| 管理敏感字段配置 | ❌ 只读 | ✅ |

## 🚀 高级配置

### 如需添加更多用户

```sql
-- 创建其他安全查看用户
CREATE USER 'report_viewer'@'%' IDENTIFIED BY 'ReportSecure2025!';

-- 授予相同权限
GRANT SELECT ON MES.v_user_encrypted TO 'report_viewer'@'%';
GRANT SELECT ON MES.v_device_encrypted TO 'report_viewer'@'%';
-- ... 其他权限

FLUSH PRIVILEGES;
```

### 如需修改密码

```sql
-- 修改navicat_viewer密码
ALTER USER 'navicat_viewer'@'%' IDENTIFIED BY '新密码';
FLUSH PRIVILEGES;
```

## ✅ 总结

现在您可以：
1. 使用`navicat_viewer`账户安全地在Navicat中查询数据
2. 敏感字段自动以加密形式显示
3. 无法意外访问原始敏感数据
4. 保持正常的业务数据查询功能

这个方案完美解决了您在Navicat中看到明文密码的问题，同时确保了数据安全性和可用性的平衡。