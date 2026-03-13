# Navicat敏感数据加密查询指南

## 🎯 快速开始

### 1. 查看加密后的敏感数据

在Navicat的查询编辑器中执行以下SQL：

```sql
-- 查看用户数据（密码已加密显示）
SELECT * FROM v_user_encrypted;

-- 查看设备数据（维护时间已加密显示）
SELECT * FROM v_device_encrypted;
```

**效果对比：**
- 原始数据：`password = "123456"`
- 加密显示：`password = "******"`

### 2. 管理敏感字段配置

```sql
-- 查看当前所有敏感字段配置
CALL sp_list_sensitive_fields(NULL);

-- 查看特定表的敏感字段配置
CALL sp_list_sensitive_fields('user');

-- 添加新的敏感字段
CALL sp_add_sensitive_field('employee', 'id_card', 'MASK', '****');

-- 禁用敏感字段
CALL sp_remove_sensitive_field('user', 'password');
```

### 3. 动态创建加密视图

```sql
-- 为指定表创建加密视图
CALL sp_create_encrypted_view('user');

-- 检查加密配置状态
CALL sp_check_encryption_status();
```

## 📋 常用查询模板

### 用户管理
```sql
-- 安全查询用户列表（推荐）
SELECT id, username, password, password_note FROM v_user_encrypted;

-- 危险查询（不推荐，仅管理员使用）
SELECT * FROM user;
```

### 设备管理
```sql
-- 安全查询设备信息（推荐）
SELECT id, device_code, name, status, last_maintenance_time 
FROM v_device_encrypted;

-- 查询非敏感设备信息
SELECT id, device_code, name, status, type, runtime_minutes 
FROM device;
```

### 敏感字段配置管理
```sql
-- 添加常见敏感字段
CALL sp_add_sensitive_field('employee', 'phone', 'MASK', '***-****-****');
CALL sp_add_sensitive_field('employee', 'email', 'MASK', '****@****.***');
CALL sp_add_sensitive_field('customer', 'credit_card', 'MASK', '****-****-****-****');

-- 查看配置统计
CALL sp_check_encryption_status();
```

## 🔒 加密类型说明

### MASK（掩码）- 推荐
- **用途**：完全隐藏敏感信息
- **示例**：`123456` → `******`
- **适用**：密码、身份证号、银行卡号

### HASH（哈希）
- **用途**：显示数据指纹，用于验证
- **示例**：`123456` → `e10adc3949ba59abbe56e057f20f883e`
- **适用**：需要验证数据完整性的场景

### AES（加密）
- **用途**：可逆加密，需要密钥
- **示例**：`123456` → `A1B2C3D4E5F6...`
- **适用**：需要解密还原的场景

## 🛡️ 安全最佳实践

### 1. 日常查询规范
- ✅ 优先使用加密视图：`v_user_encrypted`
- ❌ 避免直接查询原表：`user`
- ✅ 使用存储过程管理配置
- ❌ 避免手动修改配置表

### 2. 权限分离建议
```sql
-- 创建专用查看用户（可选）
CREATE USER 'mes_viewer'@'%' IDENTIFIED BY 'ViewerPass2025!';

-- 只授予加密视图权限
GRANT SELECT ON MES.v_user_encrypted TO 'mes_viewer'@'%';
GRANT SELECT ON MES.v_device_encrypted TO 'mes_viewer'@'%';

-- 授予管理存储过程权限
GRANT EXECUTE ON PROCEDURE MES.sp_list_sensitive_fields TO 'mes_viewer'@'%';
```

### 3. 定期维护
```sql
-- 每月检查加密配置状态
CALL sp_check_encryption_status();

-- 审查敏感字段配置
SELECT table_name, field_name, encryption_type, updated_at 
FROM sensitive_field_config 
WHERE is_active = 1 
ORDER BY updated_at DESC;
```

## 🚀 高级功能

### 1. 批量添加敏感字段
```sql
-- 员工表敏感字段
CALL sp_add_sensitive_field('employee', 'id_card', 'MASK', '****');
CALL sp_add_sensitive_field('employee', 'phone', 'MASK', '***-****-****');
CALL sp_add_sensitive_field('employee', 'salary', 'MASK', '****');

-- 客户表敏感字段
CALL sp_add_sensitive_field('customer', 'phone', 'MASK', '***-****-****');
CALL sp_add_sensitive_field('customer', 'address', 'MASK', '****');

-- 重新创建视图
CALL sp_create_encrypted_view('employee');
CALL sp_create_encrypted_view('customer');
```

### 2. 自定义掩码模式
```sql
-- 手机号掩码：138****1234
CALL sp_add_sensitive_field('contact', 'mobile', 'MASK', '***-****-****');

-- 邮箱掩码：user****@domain.com
CALL sp_add_sensitive_field('contact', 'email', 'MASK', '****@****.***');

-- 身份证掩码：前4位后4位可见
CALL sp_add_sensitive_field('person', 'id_number', 'MASK', '****');
```

## ❓ 常见问题

**Q: 加密显示会影响应用程序吗？**
A: 不会。应用程序仍然可以直接访问原始表获取明文数据。

**Q: 如何添加新的敏感字段？**
A: 使用 `sp_add_sensitive_field` 存储过程，然后调用 `sp_create_encrypted_view` 重新创建视图。

**Q: 可以修改现有字段的加密方式吗？**
A: 可以。重新调用 `sp_add_sensitive_field` 使用新的加密类型即可。

**Q: 如何确保真正的数据安全？**
A: 本方案只是显示层加密。真正的数据安全需要：
- 应用层数据加密存储
- 数据库透明数据加密（TDE）
- 严格的访问权限控制

## 📞 技术支持

如需更多帮助，请查看：
- `敏感数据加密显示使用说明.md` - 详细技术文档
- `MES_encryption_final.sql` - 完整实现代码
- 数据库管理员或系统管理员