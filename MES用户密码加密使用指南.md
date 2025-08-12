# MES用户密码加密使用指南

## 📋 **概述**

本指南详细说明了MES系统用户密码加密方案的实施和使用方法。该方案提供了完整的密码安全解决方案，包括密码加密存储、验证、管理等功能。

## 🔐 **安全特性**

### 1. **密码加密**
- **算法**: SHA-256 + 盐值 (Salt)
- **盐值**: 每个用户独有的16位随机盐值
- **存储**: 只存储加密后的哈希值，不存储明文密码

### 2. **安全防护**
- **登录尝试限制**: 5次失败后自动锁定账户
- **账户锁定**: 防止暴力破解攻击
- **密码强度验证**: 至少8位，包含字母和数字
- **会话管理**: 安全的用户会话处理

### 3. **审计功能**
- **登录记录**: 记录最后登录时间
- **操作日志**: 记录密码修改、账户解锁等操作
- **失败追踪**: 记录登录失败次数

## 🚀 **部署步骤**

### 第一步：执行数据库脚本

```sql
-- 在MySQL中执行
source MES_user_password_encryption.sql;
```

**脚本功能**：
- ✅ 备份原始用户数据
- ✅ 扩展user表结构，添加安全字段
- ✅ 为现有用户生成加密密码
- ✅ 创建密码管理存储过程
- ✅ 创建安全视图和测试数据

### 第二步：集成Java服务

将以下文件添加到您的Spring Boot项目中：

1. **`UserPasswordService.java`** - 密码服务类
2. **`AuthController.java`** - 认证控制器

### 第三步：配置依赖

在`pom.xml`中确保包含以下依赖：

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
</dependencies>
```

## 📚 **API使用说明**

### 1. **用户登录**

```http
POST /api/auth/login
Content-Type: application/json

{
    "username": "123",
    "password": "123"
}
```

**响应示例**：
```json
{
    "success": true,
    "message": "登录成功",
    "userId": 1,
    "username": "123"
}
```

### 2. **用户注册**

```http
POST /api/auth/register
Content-Type: application/json

{
    "username": "newuser",
    "password": "password123",
    "email": "user@example.com",
    "phone": "13800138000",
    "role": "user"
}
```

### 3. **修改密码**

```http
POST /api/auth/change-password
Content-Type: application/json

{
    "oldPassword": "oldpass123",
    "newPassword": "newpass123"
}
```

### 4. **管理员功能**

#### 解锁用户
```http
POST /api/auth/admin/unlock-user/2
```

#### 重置密码
```http
POST /api/auth/admin/reset-password
Content-Type: application/json

{
    "userId": 2,
    "newPassword": "newpass123"
}
```

## 🔧 **数据库结构说明**

### 扩展后的user表结构：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `id` | INT | 用户ID（主键） |
| `username` | VARCHAR(255) | 用户名 |
| `password` | VARCHAR(255) | 原密码字段（已清空） |
| `salt` | VARCHAR(32) | 密码盐值 |
| `password_hash` | VARCHAR(255) | 加密后的密码哈希 |
| `encryption_method` | VARCHAR(50) | 加密方法 |
| `created_at` | TIMESTAMP | 创建时间 |
| `updated_at` | TIMESTAMP | 更新时间 |
| `last_login` | TIMESTAMP | 最后登录时间 |
| `login_attempts` | INT | 登录尝试次数 |
| `account_locked` | BOOLEAN | 账户是否锁定 |
| `email` | VARCHAR(255) | 邮箱地址 |
| `phone` | VARCHAR(20) | 手机号码 |
| `role` | VARCHAR(50) | 用户角色 |
| `status` | VARCHAR(20) | 账户状态 |

### 存储过程说明：

1. **`ValidateUserPassword`** - 验证用户密码
2. **`CreateUser`** - 创建新用户
3. **`ChangePassword`** - 修改密码
4. **`UnlockUser`** - 解锁用户账户
5. **`ResetUserPassword`** - 重置用户密码

## 🛡️ **安全最佳实践**

### 1. **密码策略**
- ✅ 最小长度：8位
- ✅ 必须包含：字母和数字
- ✅ 建议包含：特殊字符
- ✅ 定期更换：建议90天更换一次

### 2. **账户安全**
- ✅ 登录失败5次后自动锁定
- ✅ 管理员可以解锁账户
- ✅ 记录所有登录尝试
- ✅ 定期审计用户账户

### 3. **系统安全**
- ✅ 使用HTTPS传输
- ✅ 实施会话超时
- ✅ 定期备份用户数据
- ✅ 监控异常登录行为

## 📊 **测试验证**

### 1. **功能测试**

执行SQL脚本后，系统会自动进行测试：

```sql
-- 测试正确密码
CALL ValidateUserPassword('123', '123', @result, @user_id, @message);
-- 预期结果：@result = 1 (成功)

-- 测试错误密码
CALL ValidateUserPassword('123', 'wrong', @result, @user_id, @message);
-- 预期结果：@result = -4 (密码错误)
```

### 2. **API测试**

使用Postman或curl测试API接口：

```bash
# 测试登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"123","password":"123"}'

# 测试注册
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123","email":"test@test.com"}'
```

## 🔄 **迁移现有系统**

### 1. **数据迁移**
- 原始密码已自动备份到`user_backup`表
- 现有用户密码已自动加密
- 原`password`字段已清空但保留（兼容性考虑）

### 2. **代码迁移**
- 替换原有的密码验证逻辑
- 使用新的`UserPasswordService`
- 更新前端登录表单

### 3. **测试迁移**
- 验证现有用户可以正常登录
- 测试新用户注册功能
- 验证密码修改功能

## 🚨 **故障排除**

### 常见问题：

1. **登录失败**
   - 检查用户名是否正确
   - 确认账户未被锁定
   - 验证密码是否正确

2. **账户锁定**
   - 使用管理员账户解锁
   - 检查登录尝试次数
   - 重置密码后自动解锁

3. **系统错误**
   - 检查数据库连接
   - 验证存储过程是否正确创建
   - 查看应用程序日志

### 日志查看：

```sql
-- 查看用户状态
SELECT * FROM user_info WHERE username = '123';

-- 查看登录尝试
SELECT username, login_attempts, account_locked, last_login 
FROM user WHERE username = '123';
```

## 📈 **性能优化**

### 1. **数据库优化**
- 在`username`字段上创建索引
- 定期清理过期的会话数据
- 优化存储过程性能

### 2. **应用优化**
- 使用连接池管理数据库连接
- 实施缓存机制减少数据库查询
- 异步处理日志记录

## 🔮 **未来扩展**

### 1. **增强安全功能**
- 双因素认证（2FA）
- 密码复杂度策略配置
- IP白名单/黑名单
- 设备指纹识别

### 2. **管理功能**
- 用户权限管理
- 角色基础访问控制（RBAC）
- 审计日志查询
- 批量用户操作

### 3. **集成功能**
- LDAP/AD集成
- SSO单点登录
- OAuth2支持
- API密钥管理

## 📝 **总结**

通过实施这个密码加密方案，MES系统的用户认证安全性得到了显著提升：

- ✅ **密码安全**：使用SHA-256+盐值加密，无法逆向破解
- ✅ **防护机制**：登录尝试限制和账户锁定功能
- ✅ **管理功能**：完整的用户和密码管理功能
- ✅ **审计追踪**：详细的操作日志和登录记录
- ✅ **易于使用**：简单的API接口和管理功能

这个方案不仅解决了当前的密码安全问题，还为未来的安全功能扩展奠定了坚实的基础。