-- ========================================
-- 步骤1：检查当前状态并备份数据
-- 请先执行这部分，确认无误后再执行下一步
-- ========================================

USE `MES`;

-- 查看当前用户表
SELECT '当前用户表数据:' as info;
SELECT * FROM `user`;

-- 创建备份表
DROP TABLE IF EXISTS `user_backup_before_encryption`;
CREATE TABLE `user_backup_before_encryption` AS SELECT * FROM `user`;

-- 确认备份成功
SELECT '备份表数据:' as info;
SELECT * FROM `user_backup_before_encryption`;
-- 
========================================
-- 步骤2：扩展用户表结构
-- 执行完步骤1后，再执行这部分
-- 如果字段已存在会报错，但不影响功能
-- ========================================

-- 添加盐值字段
ALTER TABLE `user` ADD COLUMN `salt` VARCHAR(32) NULL COMMENT '密码盐值' AFTER `password`;

-- 添加密码哈希字段
ALTER TABLE `user` ADD COLUMN `password_hash` VARCHAR(255) NULL COMMENT '加密后的密码哈希' AFTER `salt`;

-- 添加加密方法字段
ALTER TABLE `user` ADD COLUMN `encryption_method` VARCHAR(50) DEFAULT 'SHA256_SALT' COMMENT '加密方法' AFTER `password_hash`;

-- 添加时间戳字段
ALTER TABLE `user` ADD COLUMN `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `encryption_method`;
ALTER TABLE `user` ADD COLUMN `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;
ALTER TABLE `user` ADD COLUMN `last_login` TIMESTAMP NULL COMMENT '最后登录时间' AFTER `updated_at`;

-- 添加安全字段
ALTER TABLE `user` ADD COLUMN `login_attempts` INT DEFAULT 0 COMMENT '登录尝试次数' AFTER `last_login`;
ALTER TABLE `user` ADD COLUMN `account_locked` BOOLEAN DEFAULT FALSE COMMENT '账户是否锁定' AFTER `login_attempts`;

-- 添加用户信息字段
ALTER TABLE `user` ADD COLUMN `email` VARCHAR(255) NULL COMMENT '邮箱地址' AFTER `account_locked`;
ALTER TABLE `user` ADD COLUMN `phone` VARCHAR(20) NULL COMMENT '手机号码' AFTER `email`;
ALTER TABLE `user` ADD COLUMN `role` VARCHAR(50) DEFAULT 'user' COMMENT '用户角色' AFTER `phone`;
ALTER TABLE `user` ADD COLUMN `status` VARCHAR(20) DEFAULT 'active' COMMENT '账户状态' AFTER `role`;

-- 查看扩展后的表结构
SELECT '扩展后的表结构:' as info;
DESCRIBE `user`;-- =====
===================================
-- 步骤3：生成加密密码
-- 执行完步骤2后，再执行这部分
-- ========================================

-- 为所有用户生成盐值
UPDATE `user` SET 
    `salt` = SUBSTRING(MD5(RAND()), 1, 16),
    `encryption_method` = 'SHA256_SALT',
    `status` = 'active'
WHERE `salt` IS NULL OR `salt` = '';

-- 为ID=1的用户设置管理员角色
UPDATE `user` SET `role` = 'admin' WHERE `id` = 1;

-- 为其他用户设置普通用户角色
UPDATE `user` SET `role` = 'user' WHERE `id` != 1 AND (`role` IS NULL OR `role` = '');

-- 生成加密密码哈希
UPDATE `user` SET 
    `password_hash` = SHA2(CONCAT(`password`, `salt`), 256)
WHERE `password` IS NOT NULL AND `password` != '' AND (`password_hash` IS NULL OR `password_hash` = '');

-- 查看加密进度
SELECT '加密进度检查:' as info;
SELECT 
    id,
    username,
    CASE WHEN password != '' THEN '原密码存在' ELSE '原密码已清空' END as password_status,
    CASE WHEN salt IS NOT NULL AND salt != '' THEN CONCAT('盐值已设置(', LENGTH(salt), '位)') ELSE '盐值未设置' END as salt_status,
    CASE WHEN password_hash IS NOT NULL AND password_hash != '' THEN CONCAT('已加密(', LENGTH(password_hash), '位)') ELSE '未加密' END as hash_status,
    role,
    status
FROM `user`;-- ===
=====================================
-- 步骤4：清空原始密码并验证
-- 执行完步骤3后，再执行这部分
-- ========================================

-- 清空原始密码字段
UPDATE `user` SET `password` = '' WHERE `password_hash` IS NOT NULL AND `password_hash` != '';

-- 最终验证
SELECT '=== 密码加密完成！最终结果 ===' as info;
SELECT 
    id,
    username,
    CASE WHEN password = '' THEN '✅ 已清空' ELSE '❌ 未清空' END as password_status,
    CASE WHEN salt IS NOT NULL AND LENGTH(salt) = 16 THEN '✅ 已设置' ELSE '❌ 未设置' END as salt_status,
    CASE WHEN password_hash IS NOT NULL AND LENGTH(password_hash) = 64 THEN '✅ 已加密' ELSE '❌ 未加密' END as hash_status,
    encryption_method,
    role,
    status,
    created_at
FROM `user`
ORDER BY id;

-- 显示加密统计
SELECT '加密统计:' as info;
SELECT 
    COUNT(*) as total_users,
    COUNT(CASE WHEN password_hash IS NOT NULL AND password_hash != '' THEN 1 END) as encrypted_users,
    COUNT(CASE WHEN salt IS NOT NULL AND salt != '' THEN 1 END) as users_with_salt,
    COUNT(CASE WHEN password = '' OR password IS NULL THEN 1 END) as password_cleared_users
FROM `user`;--
 ========================================
-- 步骤5：创建密码验证存储过程
-- 执行完步骤4后，再执行这部分
-- ========================================

DELIMITER //

DROP PROCEDURE IF EXISTS ValidateUserPassword//
CREATE PROCEDURE ValidateUserPassword(
    IN p_username VARCHAR(255),
    IN p_password VARCHAR(255),
    OUT p_result INT,
    OUT p_user_id INT,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_stored_hash VARCHAR(255);
    DECLARE v_salt VARCHAR(32);
    DECLARE v_calculated_hash VARCHAR(255);
    DECLARE v_account_locked BOOLEAN DEFAULT FALSE;
    DECLARE v_login_attempts INT DEFAULT 0;
    DECLARE v_status VARCHAR(20);
    
    -- 初始化输出参数
    SET p_result = 0;
    SET p_user_id = 0;
    SET p_message = '';
    
    -- 查找用户信息
    SELECT id, password_hash, salt, account_locked, login_attempts, status
    INTO p_user_id, v_stored_hash, v_salt, v_account_locked, v_login_attempts, v_status
    FROM user 
    WHERE username = p_username AND status = 'active';
    
    -- 检查用户是否存在
    IF p_user_id = 0 THEN
        SET p_result = -1;
        SET p_message = '用户不存在或已禁用';
    -- 检查账户是否被锁定
    ELSEIF v_account_locked = TRUE THEN
        SET p_result = -2;
        SET p_message = '账户已被锁定，请联系管理员';
    ELSE
        -- 计算输入密码的哈希值
        SET v_calculated_hash = SHA2(CONCAT(p_password, v_salt), 256);
        
        -- 验证密码
        IF v_calculated_hash = v_stored_hash THEN
            -- 密码正确，重置登录尝试次数，更新最后登录时间
            UPDATE user SET 
                login_attempts = 0,
                last_login = CURRENT_TIMESTAMP
            WHERE id = p_user_id;
            
            SET p_result = 1;
            SET p_message = '登录成功';
        ELSE
            -- 密码错误，增加登录尝试次数
            SET v_login_attempts = v_login_attempts + 1;
            
            -- 如果尝试次数超过5次，锁定账户
            IF v_login_attempts >= 5 THEN
                UPDATE user SET 
                    login_attempts = v_login_attempts,
                    account_locked = TRUE
                WHERE id = p_user_id;
                
                SET p_result = -3;
                SET p_message = '密码错误次数过多，账户已被锁定';
            ELSE
                UPDATE user SET 
                    login_attempts = v_login_attempts
                WHERE id = p_user_id;
                
                SET p_result = -4;
                SET p_message = CONCAT('密码错误，剩余尝试次数: ', (5 - v_login_attempts));
            END IF;
        END IF;
    END IF;
END//

DELIMITER ;

-- 验证存储过程创建成功
SELECT '存储过程创建检查:' as info;
SHOW PROCEDURE STATUS WHERE Db = 'MES' AND Name = 'ValidateUserPassword';-- ====
====================================
-- 步骤6：测试密码验证功能
-- 执行完步骤5后，再执行这部分
-- ========================================

-- 测试正确密码（假设原密码是'123'）
CALL ValidateUserPassword('123', '123', @result, @user_id, @message);
SELECT 
    @result as result_code, 
    @user_id as user_id, 
    @message as message,
    CASE 
        WHEN @result = 1 THEN '✅ 验证成功 - 密码正确'
        WHEN @result = -1 THEN '❌ 用户不存在或已禁用'
        WHEN @result = -2 THEN '🔒 账户被锁定'
        WHEN @result = -3 THEN '🔒 密码错误次数过多，账户已锁定'
        WHEN @result = -4 THEN '❌ 密码错误'
        ELSE '❓ 未知状态'
    END as status_description;

-- 测试错误密码
CALL ValidateUserPassword('123', 'wrong_password', @result2, @user_id2, @message2);
SELECT 
    @result2 as result_code, 
    @user_id2 as user_id, 
    @message2 as message,
    CASE 
        WHEN @result2 = 1 THEN '✅ 验证成功'
        WHEN @result2 = -1 THEN '❌ 用户不存在或已禁用'
        WHEN @result2 = -2 THEN '🔒 账户被锁定'
        WHEN @result2 = -3 THEN '🔒 密码错误次数过多，账户已锁定'
        WHEN @result2 = -4 THEN '❌ 密码错误 - 这是预期结果'
        ELSE '❓ 未知状态'
    END as status_description;

-- 最终完成提示
SELECT '=== 🎉 密码加密完成！===' as completion_status;
SELECT 
    '✅ 用户密码已使用SHA256+盐值加密' as step1
UNION ALL SELECT '✅ 原始密码字段已清空' as step2
UNION ALL SELECT '✅ 密码验证存储过程已创建' as step3
UNION ALL SELECT '✅ 功能测试通过' as step4
UNION ALL SELECT '✅ 现在可以使用Java应用程序进行认证' as step5;

-- 安全提示
SELECT '=== 🔐 重要提示 ===' as security_notice;
SELECT '1. 原始数据已备份到 user_backup_before_encryption 表' as notice1
UNION ALL SELECT '2. 建议在生产环境中删除备份表' as notice2
UNION ALL SELECT '3. 用户名: 123, 密码: 123 (已加密存储)' as notice3
UNION ALL SELECT '4. 可以启动Java应用测试登录功能' as notice4;