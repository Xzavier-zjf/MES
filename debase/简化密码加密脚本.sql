/*
 简化密码加密脚本 - 可直接在Navicat中执行
 请逐步执行，不要一次性全部执行
*/

-- 切换到MES数据库
USE `MES`;

-- ========================================
-- 第一步：检查当前用户表状态
-- ========================================
SELECT '=== 当前用户表状态 ===' as info;
SELECT * FROM `user`;

-- ========================================
-- 第二步：备份原始数据
-- ========================================
SELECT '=== 创建备份表 ===' as info;
DROP TABLE IF EXISTS `user_backup_before_encryption`;
CREATE TABLE `user_backup_before_encryption` AS SELECT * FROM `user`;

SELECT '=== 备份完成，备份数据如下 ===' as info;
SELECT * FROM `user_backup_before_encryption`;

-- ========================================
-- 第三步：扩展用户表结构
-- ========================================
SELECT '=== 扩展用户表结构 ===' as info;

-- 直接添加字段（如果字段已存在会报错，但不影响后续执行）
ALTER TABLE `user` 
ADD COLUMN `salt` VARCHAR(32) NULL COMMENT '密码盐值' AFTER `password`;

ALTER TABLE `user` 
ADD COLUMN `password_hash` VARCHAR(255) NULL COMMENT '加密后的密码哈希' AFTER `salt`;

ALTER TABLE `user` 
ADD COLUMN `encryption_method` VARCHAR(50) DEFAULT 'SHA256_SALT' COMMENT '加密方法' AFTER `password_hash`;

ALTER TABLE `user` 
ADD COLUMN `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `encryption_method`;

ALTER TABLE `user` 
ADD COLUMN `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;

ALTER TABLE `user` 
ADD COLUMN `last_login` TIMESTAMP NULL COMMENT '最后登录时间' AFTER `updated_at`;

ALTER TABLE `user` 
ADD COLUMN `login_attempts` INT DEFAULT 0 COMMENT '登录尝试次数' AFTER `last_login`;

ALTER TABLE `user` 
ADD COLUMN `account_locked` BOOLEAN DEFAULT FALSE COMMENT '账户是否锁定' AFTER `login_attempts`;

ALTER TABLE `user` 
ADD COLUMN `email` VARCHAR(255) NULL COMMENT '邮箱地址' AFTER `account_locked`;

ALTER TABLE `user` 
ADD COLUMN `phone` VARCHAR(20) NULL COMMENT '手机号码' AFTER `email`;

ALTER TABLE `user` 
ADD COLUMN `role` VARCHAR(50) DEFAULT 'user' COMMENT '用户角色' AFTER `phone`;

ALTER TABLE `user` 
ADD COLUMN `status` VARCHAR(20) DEFAULT 'active' COMMENT '账户状态' AFTER `role`;

-- 查看扩展后的表结构
SELECT '=== 表结构扩展完成 ===' as info;
DESCRIBE `user`;

-- ========================================
-- 第四步：为现有用户生成加密密码
-- ========================================
SELECT '=== 开始加密现有用户密码 ===' as info;

-- 为用户ID=1生成盐值和加密密码
UPDATE `user` SET 
    `salt` = SUBSTRING(MD5(RAND()), 1, 16),
    `encryption_method` = 'SHA256_SALT',
    `role` = 'admin',
    `status` = 'active'
WHERE `id` = 1;

-- 生成加密密码哈希
UPDATE `user` SET 
    `password_hash` = SHA2(CONCAT(`password`, `salt`), 256)
WHERE `id` = 1 AND `password` IS NOT NULL AND `password` != '';

-- 处理其他用户（如果存在）
UPDATE `user` SET 
    `salt` = SUBSTRING(MD5(RAND()), 1, 16),
    `encryption_method` = 'SHA256_SALT',
    `role` = COALESCE(`role`, 'user'),
    `status` = COALESCE(`status`, 'active')
WHERE `id` != 1;

UPDATE `user` SET 
    `password_hash` = SHA2(CONCAT(`password`, `salt`), 256)
WHERE `id` != 1 AND `password` IS NOT NULL AND `password` != '';

-- ========================================
-- 第五步：清空原始密码字段
-- ========================================
SELECT '=== 清空原始密码字段 ===' as info;
UPDATE `user` SET `password` = '' WHERE `password_hash` IS NOT NULL;

-- ========================================
-- 第六步：验证加密结果
-- ========================================
SELECT '=== 加密完成！查看结果 ===' as info;

SELECT 
    id,
    username,
    CASE WHEN password = '' THEN '已清空' ELSE '未清空' END as password_status,
    CASE WHEN salt IS NOT NULL AND salt != '' THEN '已设置' ELSE '未设置' END as salt_status,
    CASE WHEN password_hash IS NOT NULL AND password_hash != '' THEN '已加密' ELSE '未加密' END as hash_status,
    encryption_method,
    role,
    status,
    created_at
FROM `user`
ORDER BY id;

-- ========================================
-- 第七步：创建密码验证存储过程
-- ========================================
SELECT '=== 创建密码验证存储过程 ===' as info;

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

-- ========================================
-- 第八步：测试密码验证
-- ========================================
SELECT '=== 测试密码验证功能 ===' as info;

-- 测试正确密码（假设原密码是'123'）
CALL ValidateUserPassword('123', '123', @result, @user_id, @message);
SELECT @result as result_code, @user_id as user_id, @message as message, 
       CASE 
           WHEN @result = 1 THEN '✅ 验证成功'
           WHEN @result = -1 THEN '❌ 用户不存在'
           WHEN @result = -2 THEN '🔒 账户被锁定'
           WHEN @result = -3 THEN '🔒 账户已锁定'
           WHEN @result = -4 THEN '❌ 密码错误'
           ELSE '❓ 未知状态'
       END as status_description;

-- 测试错误密码
CALL ValidateUserPassword('123', 'wrong_password', @result, @user_id, @message);
SELECT @result as result_code, @user_id as user_id, @message as message,
       CASE 
           WHEN @result = 1 THEN '✅ 验证成功'
           WHEN @result = -1 THEN '❌ 用户不存在'
           WHEN @result = -2 THEN '🔒 账户被锁定'
           WHEN @result = -3 THEN '🔒 账户已锁定'
           WHEN @result = -4 THEN '❌ 密码错误'
           ELSE '❓ 未知状态'
       END as status_description;

-- ========================================
-- 第九步：显示最终状态
-- ========================================
SELECT '=== 密码加密完成！最终状态 ===' as info;

SELECT 
    '数据库' as item,
    'MES' as name,
    '✅ 已完成' as status
UNION ALL
SELECT 
    '用户表',
    'user',
    '✅ 已扩展'
UNION ALL
SELECT 
    '密码加密',
    CONCAT(COUNT(*), ' 个用户'),
    '✅ 已完成'
FROM user WHERE password_hash IS NOT NULL
UNION ALL
SELECT 
    '存储过程',
    'ValidateUserPassword',
    '✅ 已创建'
UNION ALL
SELECT 
    '备份表',
    'user_backup_before_encryption',
    '✅ 已创建';

-- 安全提示
SELECT '=== 🔐 安全提示 ===' as security_tips;
SELECT '1. 原始密码已备份到 user_backup_before_encryption 表' as tip
UNION ALL SELECT '2. 用户密码已使用 SHA256 + 盐值加密'
UNION ALL SELECT '3. 原始 password 字段已清空'
UNION ALL SELECT '4. 可以使用 ValidateUserPassword 存储过程验证密码'
UNION ALL SELECT '5. 建议在生产环境中删除备份表'
UNION ALL SELECT '6. 现在可以使用 Java 应用程序进行用户认证';