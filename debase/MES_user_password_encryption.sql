/*
 MES用户密码加密方案
 对user表的password字段进行加密处理
 
 Date: 08/12/2025
 Author: MES System
*/

USE `MES`;

-- ========================================
-- 第一步：备份原始用户数据
-- ========================================

-- 创建用户数据备份表
DROP TABLE IF EXISTS `user_backup`;
CREATE TABLE `user_backup` AS SELECT * FROM `user`;

-- 显示备份数据
SELECT '原始用户数据备份:' as info;
SELECT * FROM `user_backup`;

-- ========================================
-- 第二步：修改user表结构
-- ========================================

-- 添加新的字段用于增强安全性
ALTER TABLE `user` 
ADD COLUMN `salt` VARCHAR(32) NULL COMMENT '密码盐值' AFTER `password`,
ADD COLUMN `password_hash` VARCHAR(255) NULL COMMENT '加密后的密码哈希' AFTER `salt`,
ADD COLUMN `encryption_method` VARCHAR(50) DEFAULT 'SHA256_SALT' COMMENT '加密方法' AFTER `password_hash`,
ADD COLUMN `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `encryption_method`,
ADD COLUMN `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`,
ADD COLUMN `last_login` TIMESTAMP NULL COMMENT '最后登录时间' AFTER `updated_at`,
ADD COLUMN `login_attempts` INT DEFAULT 0 COMMENT '登录尝试次数' AFTER `last_login`,
ADD COLUMN `account_locked` BOOLEAN DEFAULT FALSE COMMENT '账户是否锁定' AFTER `login_attempts`,
ADD COLUMN `email` VARCHAR(255) NULL COMMENT '邮箱地址' AFTER `account_locked`,
ADD COLUMN `phone` VARCHAR(20) NULL COMMENT '手机号码' AFTER `email`,
ADD COLUMN `role` VARCHAR(50) DEFAULT 'user' COMMENT '用户角色' AFTER `phone`,
ADD COLUMN `status` VARCHAR(20) DEFAULT 'active' COMMENT '账户状态' AFTER `role`;

-- ========================================
-- 第三步：生成加密密码
-- ========================================

-- 为现有用户生成盐值和加密密码
-- 注意：这里使用MySQL的内置函数进行加密，实际生产环境建议使用更强的加密算法

-- 用户1: username='123', password='123'
UPDATE `user` SET 
    `salt` = SUBSTRING(MD5(RAND()), 1, 16),
    `encryption_method` = 'SHA256_SALT',
    `role` = 'admin',
    `status` = 'active'
WHERE `id` = 1;

-- 生成加密密码 (SHA256(password + salt))
UPDATE `user` SET 
    `password_hash` = SHA2(CONCAT('123', `salt`), 256)
WHERE `id` = 1;

-- ========================================
-- 第四步：创建密码验证函数
-- ========================================

-- 创建密码验证存储过程
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
-- 第五步：创建用户管理存储过程
-- ========================================

-- 创建新用户的存储过程
DELIMITER //

DROP PROCEDURE IF EXISTS CreateUser//
CREATE PROCEDURE CreateUser(
    IN p_username VARCHAR(255),
    IN p_password VARCHAR(255),
    IN p_email VARCHAR(255),
    IN p_phone VARCHAR(20),
    IN p_role VARCHAR(50),
    OUT p_result INT,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_salt VARCHAR(32);
    DECLARE v_password_hash VARCHAR(255);
    DECLARE v_user_count INT DEFAULT 0;
    
    -- 检查用户名是否已存在
    SELECT COUNT(*) INTO v_user_count FROM user WHERE username = p_username;
    
    IF v_user_count > 0 THEN
        SET p_result = -1;
        SET p_message = '用户名已存在';
    ELSE
        -- 生成盐值
        SET v_salt = SUBSTRING(MD5(RAND()), 1, 16);
        
        -- 生成密码哈希
        SET v_password_hash = SHA2(CONCAT(p_password, v_salt), 256);
        
        -- 插入新用户
        INSERT INTO user (username, password, salt, password_hash, encryption_method, email, phone, role, status)
        VALUES (p_username, '', v_salt, v_password_hash, 'SHA256_SALT', p_email, p_phone, IFNULL(p_role, 'user'), 'active');
        
        SET p_result = 1;
        SET p_message = '用户创建成功';
    END IF;
END//

DELIMITER ;

-- 修改密码的存储过程
DELIMITER //

DROP PROCEDURE IF EXISTS ChangePassword//
CREATE PROCEDURE ChangePassword(
    IN p_user_id INT,
    IN p_old_password VARCHAR(255),
    IN p_new_password VARCHAR(255),
    OUT p_result INT,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_stored_hash VARCHAR(255);
    DECLARE v_salt VARCHAR(32);
    DECLARE v_calculated_hash VARCHAR(255);
    DECLARE v_new_salt VARCHAR(32);
    DECLARE v_new_hash VARCHAR(255);
    
    -- 获取当前密码信息
    SELECT password_hash, salt INTO v_stored_hash, v_salt
    FROM user WHERE id = p_user_id AND status = 'active';
    
    -- 验证旧密码
    SET v_calculated_hash = SHA2(CONCAT(p_old_password, v_salt), 256);
    
    IF v_calculated_hash = v_stored_hash THEN
        -- 生成新的盐值和密码哈希
        SET v_new_salt = SUBSTRING(MD5(RAND()), 1, 16);
        SET v_new_hash = SHA2(CONCAT(p_new_password, v_new_salt), 256);
        
        -- 更新密码
        UPDATE user SET 
            salt = v_new_salt,
            password_hash = v_new_hash,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = p_user_id;
        
        SET p_result = 1;
        SET p_message = '密码修改成功';
    ELSE
        SET p_result = -1;
        SET p_message = '原密码错误';
    END IF;
END//

DELIMITER ;

-- ========================================
-- 第六步：创建管理员功能存储过程
-- ========================================

-- 解锁用户账户
DELIMITER //

DROP PROCEDURE IF EXISTS UnlockUser//
CREATE PROCEDURE UnlockUser(
    IN p_user_id INT,
    OUT p_result INT,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_user_count INT DEFAULT 0;
    
    SELECT COUNT(*) INTO v_user_count FROM user WHERE id = p_user_id;
    
    IF v_user_count = 0 THEN
        SET p_result = -1;
        SET p_message = '用户不存在';
    ELSE
        UPDATE user SET 
            account_locked = FALSE,
            login_attempts = 0,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = p_user_id;
        
        SET p_result = 1;
        SET p_message = '用户账户已解锁';
    END IF;
END//

DELIMITER ;

-- 重置用户密码（管理员功能）
DELIMITER //

DROP PROCEDURE IF EXISTS ResetUserPassword//
CREATE PROCEDURE ResetUserPassword(
    IN p_user_id INT,
    IN p_new_password VARCHAR(255),
    OUT p_result INT,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_new_salt VARCHAR(32);
    DECLARE v_new_hash VARCHAR(255);
    DECLARE v_user_count INT DEFAULT 0;
    
    SELECT COUNT(*) INTO v_user_count FROM user WHERE id = p_user_id;
    
    IF v_user_count = 0 THEN
        SET p_result = -1;
        SET p_message = '用户不存在';
    ELSE
        -- 生成新的盐值和密码哈希
        SET v_new_salt = SUBSTRING(MD5(RAND()), 1, 16);
        SET v_new_hash = SHA2(CONCAT(p_new_password, v_new_salt), 256);
        
        -- 更新密码并解锁账户
        UPDATE user SET 
            salt = v_new_salt,
            password_hash = v_new_hash,
            account_locked = FALSE,
            login_attempts = 0,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = p_user_id;
        
        SET p_result = 1;
        SET p_message = '密码重置成功';
    END IF;
END//

DELIMITER ;

-- ========================================
-- 第七步：创建查询视图
-- ========================================

-- 创建用户信息视图（不包含敏感信息）
CREATE OR REPLACE VIEW user_info AS
SELECT 
    id,
    username,
    email,
    phone,
    role,
    status,
    created_at,
    updated_at,
    last_login,
    login_attempts,
    account_locked
FROM user;

-- ========================================
-- 第八步：测试加密功能
-- ========================================

-- 测试密码验证
SELECT '=== 测试密码验证功能 ===' as test_info;

-- 测试正确密码
CALL ValidateUserPassword('123', '123', @result, @user_id, @message);
SELECT @result as result, @user_id as user_id, @message as message;

-- 测试错误密码
CALL ValidateUserPassword('123', 'wrong_password', @result, @user_id, @message);
SELECT @result as result, @user_id as user_id, @message as message;

-- 测试创建新用户
SELECT '=== 测试创建新用户 ===' as test_info;
CALL CreateUser('admin', 'admin123', 'admin@mes.com', '13800138000', 'admin', @result, @message);
SELECT @result as result, @message as message;

CALL CreateUser('operator', 'op123456', 'operator@mes.com', '13800138001', 'operator', @result, @message);
SELECT @result as result, @message as message;

-- 显示所有用户信息（安全视图）
SELECT '=== 当前用户列表 ===' as info;
SELECT * FROM user_info ORDER BY id;

-- ========================================
-- 第九步：安全建议和清理
-- ========================================

-- 清空原始密码字段（保留字段结构以兼容现有代码）
UPDATE `user` SET `password` = '' WHERE `password` IS NOT NULL;

-- 显示最终的表结构
SELECT '=== 最终用户表结构 ===' as info;
DESCRIBE `user`;

-- 显示加密后的数据示例（仅显示非敏感字段）
SELECT '=== 加密后的用户数据 ===' as info;
SELECT 
    id,
    username,
    CASE WHEN password_hash IS NOT NULL THEN '已加密' ELSE '未加密' END as password_status,
    CASE WHEN salt IS NOT NULL THEN '已设置' ELSE '未设置' END as salt_status,
    encryption_method,
    role,
    status,
    created_at
FROM user;

-- 安全提示
SELECT '=== 安全提示 ===' as security_tips;
SELECT '1. 原始密码已备份到 user_backup 表中' as tip
UNION ALL SELECT '2. 密码字段已清空，现在使用 password_hash 存储加密密码'
UNION ALL SELECT '3. 每个用户都有唯一的盐值，增强安全性'
UNION ALL SELECT '4. 实现了登录尝试限制和账户锁定功能'
UNION ALL SELECT '5. 建议在生产环境中删除 user_backup 表'
UNION ALL SELECT '6. 建议定期更新密码和审计用户账户';