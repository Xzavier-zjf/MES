# Navicat密码加密执行指南

## 🚀 **快速执行步骤**

### 第一步：连接数据库
1. 打开Navicat
2. 连接到您的MySQL服务器
3. 确保可以看到`MES`数据库

### 第二步：检查当前状态
在Navicat的查询编辑器中执行：
```sql
USE MES;
SELECT * FROM user;
```
您应该看到类似这样的数据：
```
id | username | password
1  | 123      | 123
```

### 第三步：执行密码加密
1. 在Navicat中打开新的查询窗口
2. 复制粘贴 `执行密码加密脚本.sql` 的全部内容
3. 点击"运行"按钮执行脚本
4. 等待执行完成（大约需要几秒钟）

### 第四步：验证加密结果
执行验证脚本：
```sql
-- 复制粘贴 验证密码加密状态.sql 的内容并执行
```

## 📊 **预期结果**

### 执行前（当前状态）：
```sql
SELECT * FROM user;
```
结果：
```
id | username | password
1  | 123      | 123
```

### 执行后（加密状态）：
```sql
SELECT id, username, password, salt, password_hash, encryption_method FROM user;
```
结果：
```
id | username | password | salt             | password_hash                                                    | encryption_method
1  | 123      |          | a1b2c3d4e5f6g7h8 | 9af15b336e6a9619928537df30b2e6a2376569fcf9d7e773eccede65606529a73 | SHA256_SALT
```

## ✅ **验证密码加密是否成功**

### 1. 检查字段变化
- ✅ `password` 字段应该为空
- ✅ `salt` 字段应该有16位随机字符
- ✅ `password_hash` 字段应该有64位哈希值
- ✅ `encryption_method` 应该是 'SHA256_SALT'

### 2. 测试密码验证
执行以下SQL测试：
```sql
-- 测试正确密码
CALL ValidateUserPassword('123', '123', @result, @user_id, @message);
SELECT @result, @user_id, @message;
-- 预期结果：@result = 1 (成功)

-- 测试错误密码
CALL ValidateUserPassword('123', 'wrong', @result, @user_id, @message);
SELECT @result, @user_id, @message;
-- 预期结果：@result = -4 (密码错误)
```

## 🔍 **故障排除**

### 问题1：脚本执行失败
**可能原因**：
- 数据库连接权限不足
- MES数据库不存在
- 用户表结构异常

**解决方法**：
```sql
-- 检查数据库是否存在
SHOW DATABASES LIKE 'MES';

-- 检查用户表是否存在
USE MES;
SHOW TABLES LIKE 'user';

-- 检查当前用户权限
SHOW GRANTS;
```

### 问题2：密码仍然是明文
**检查步骤**：
```sql
-- 检查是否执行了加密
SELECT 
    COUNT(*) as total_users,
    COUNT(CASE WHEN password_hash IS NOT NULL THEN 1 END) as encrypted_users
FROM user;
```

**如果encrypted_users = 0**：
- 重新执行加密脚本
- 检查是否有错误信息

### 问题3：存储过程不存在
**检查**：
```sql
SHOW PROCEDURE STATUS WHERE Db = 'MES';
```

**修复**：
重新执行加密脚本中的存储过程创建部分。

## 🧪 **完整测试流程**

### 1. 执行前测试
```sql
USE MES;
-- 查看原始数据
SELECT * FROM user;
-- 应该看到明文密码
```

### 2. 执行加密脚本
```sql
-- 粘贴并执行 执行密码加密脚本.sql 的全部内容
```

### 3. 执行后验证
```sql
-- 粘贴并执行 验证密码加密状态.sql 的全部内容
```

### 4. 功能测试
```sql
-- 测试密码验证功能
CALL ValidateUserPassword('123', '123', @r, @u, @m);
SELECT @r as result_code, @u as user_id, @m as message;
```

## 📱 **Java应用测试**

加密完成后，您可以测试Java应用：

### 1. 启动auth-service
```bash
cd MES-demo/services/auth-service
mvn spring-boot:run
```

### 2. 测试登录API
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"123","password":"123"}'
```

预期响应：
```json
{
  "success": true,
  "message": "登录成功",
  "userId": 1,
  "username": "123"
}
```

## 🔐 **安全检查清单**

执行完成后，请确认：

- [ ] 原始密码字段已清空
- [ ] 每个用户都有唯一的盐值
- [ ] 密码哈希长度为64位
- [ ] 加密方法设置为SHA256_SALT
- [ ] 存储过程创建成功
- [ ] 密码验证功能正常
- [ ] 备份表已创建
- [ ] Java应用可以正常登录

## 🚨 **重要提醒**

1. **备份数据**：脚本会自动创建备份表，但建议您也手动备份
2. **测试环境**：建议先在测试环境执行，确认无误后再在生产环境执行
3. **权限检查**：确保数据库用户有足够的权限执行DDL和DML操作
4. **删除备份**：在生产环境中，确认加密成功后可以删除备份表

## 📞 **需要帮助？**

如果执行过程中遇到问题：
1. 检查Navicat的错误信息
2. 运行验证脚本查看具体状态
3. 检查数据库连接和权限
4. 确认MES数据库和user表存在

执行成功后，您的用户密码就会从明文'123'变成安全的加密哈希值！