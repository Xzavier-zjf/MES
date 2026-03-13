/*
 MES敏感数据加密显示方案
 
 功能说明：
 1. 对敏感字段（如密码）在Navicat等工具中以加密形式显示
 2. 支持字段级加密，非敏感字段保持明文
 3. 通过视图和函数实现，不影响应用程序正常使用
 4. 支持灵活的加密字段配置
 
 Date: 2025-08-12
*/

USE `MES`;

-- ========================================
-- 1. 创建敏感字段配置表
-- ========================================

DROP TABLE IF EXISTS `sensitive_field_config`;
CREATE TABLE `sensitive_field_config` (
  `id` int NOT NULL AUTO_INCREMENT,
  `table_name` varchar(64) NOT NULL COMMENT '表名',
  `field_name` varchar(64) NOT NULL COMMENT '字段名',
  `encryption_type` varchar(20) NOT NULL DEFAULT 'MASK' COMMENT 'Encryption type: MASK, HASH, AES',
  `mask_pattern` varchar(50) DEFAULT '****' COMMENT 'Mask pattern',
  `is_active` tinyint(1) DEFAULT 1 COMMENT 'Is active',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_table_field` (`table_name`, `field_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Sensitive field configuration table';

-- 插入敏感字段配置
INSERT INTO `sensitive_field_config` (`table_name`, `field_name`, `encryption_type`, `mask_pattern`) VALUES
('user', 'password', 'MASK', '******'),
('device', 'last_maintenance_time', 'MASK', '****-**-** **:**:**');

-- ========================================
-- 2. 创建加密显示函数
-- ========================================

-- 掩码函数
DELIMITER $$
DROP FUNCTION IF EXISTS `mask_sensitive_data`$$
CREATE FUNCTION `mask_sensitive_data`(
    original_value TEXT,
    mask_pattern VARCHAR(50)
) RETURNS TEXT
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE result TEXT;
    
    -- 如果原始值为空，返回空
    IF original_value IS NULL OR original_value = '' THEN
        RETURN original_value;
    END IF;
    
    -- 根据掩码模式处理
    CASE mask_pattern
        WHEN '******' THEN
            SET result = '******';
        WHEN '****-**-** **:**:**' THEN
            SET result = '****-**-** **:**:**';
        ELSE
            -- 默认掩码：保留前2位和后2位，中间用*替代
            IF LENGTH(original_value) <= 4 THEN
                SET result = REPEAT('*', LENGTH(original_value));
            ELSE
                SET result = CONCAT(
                    LEFT(original_value, 2),
                    REPEAT('*', LENGTH(original_value) - 4),
                    RIGHT(original_value, 2)
                );
            END IF;
    END CASE;
    
    RETURN result;
END$$

-- MD5哈希函数（用于显示数据指纹）
DROP FUNCTION IF EXISTS `hash_sensitive_data`$$
CREATE FUNCTION `hash_sensitive_data`(
    original_value TEXT
) RETURNS VARCHAR(32)
READS SQL DATA
DETERMINISTIC
BEGIN
    IF original_value IS NULL OR original_value = '' THEN
        RETURN original_value;
    END IF;
    
    RETURN MD5(original_value);
END$$

-- AES加密函数（需要密钥）
DROP FUNCTION IF EXISTS `aes_encrypt_display`$$
CREATE FUNCTION `aes_encrypt_display`(
    original_value TEXT,
    encryption_key VARCHAR(128)
) RETURNS TEXT
READS SQL DATA
DETERMINISTIC
BEGIN
    IF original_value IS NULL OR original_value = '' THEN
        RETURN original_value;
    END IF;
    
    RETURN HEX(AES_ENCRYPT(original_value, encryption_key));
END$$

DELIMITER ;

-- ========================================
-- 3. 创建加密显示视图
-- ========================================

-- 用户表加密视图
DROP VIEW IF EXISTS `v_user_encrypted`;
CREATE VIEW `v_user_encrypted` AS
SELECT 
    id,
    username,
    mask_sensitive_data(password, '******') as password,
    'Password encrypted for display' as password_note
FROM user;

-- 设备表加密视图（示例：假设维护时间也是敏感信息）
DROP VIEW IF EXISTS `v_device_encrypted`;
CREATE VIEW `v_device_encrypted` AS
SELECT 
    id,
    device_code,
    name,
    status,
    type,
    runtime_minutes,
    open_close_times,
    mask_sensitive_data(CAST(last_maintenance_time AS CHAR), '****-**-** **:**:**') as last_maintenance_time,
    created_at,
    updated_at,
    injection_time,
    injection_pressure,
    printing_speed,
    printing_pressure,
    'Maintenance time encrypted for display' as maintenance_note
FROM device;

-- ========================================
-- 4. 创建通用加密查询存储过程
-- ========================================

DELIMITER $$
DROP PROCEDURE IF EXISTS `sp_query_with_encryption`$$
CREATE PROCEDURE `sp_query_with_encryption`(
    IN table_name VARCHAR(64)
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE field_name VARCHAR(64);
    DECLARE encryption_type VARCHAR(20);
    DECLARE mask_pattern VARCHAR(50);
    DECLARE select_clause TEXT DEFAULT '';
    DECLARE final_query TEXT;
    
    -- 声明游标
    DECLARE field_cursor CURSOR FOR
        SELECT field_name, encryption_type, mask_pattern
        FROM sensitive_field_config
        WHERE table_name = table_name AND is_active = 1;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    -- 构建基础查询
    SET select_clause = 'SELECT ';
    
    -- 获取表的所有列
    SET @sql = CONCAT('SELECT GROUP_CONCAT(COLUMN_NAME) INTO @all_columns FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ''', table_name, '''');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    
    SET select_clause = CONCAT(select_clause, @all_columns);
    
    -- 处理敏感字段
    OPEN field_cursor;
    read_loop: LOOP
        FETCH field_cursor INTO field_name, encryption_type, mask_pattern;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- 替换敏感字段为加密显示
        CASE encryption_type
            WHEN 'MASK' THEN
                SET select_clause = REPLACE(select_clause, field_name, 
                    CONCAT('mask_sensitive_data(CAST(', field_name, ' AS CHAR), ''', mask_pattern, ''') as ', field_name));
            WHEN 'HASH' THEN
                SET select_clause = REPLACE(select_clause, field_name,
                    CONCAT('hash_sensitive_data(CAST(', field_name, ' AS CHAR)) as ', field_name));
            WHEN 'AES' THEN
                SET select_clause = REPLACE(select_clause, field_name,
                    CONCAT('aes_encrypt_display(CAST(', field_name, ' AS CHAR), ''MES_SECRET_KEY_2025'') as ', field_name));
            ELSE
                -- 默认情况，保持原字段不变
                SET select_clause = select_clause;
        END CASE;
        
    END LOOP;
    CLOSE field_cursor;
    
    -- 构建完整查询
    SET final_query = CONCAT(select_clause, ' FROM ', table_name);
    
    -- 执行查询
    SET @final_sql = final_query;
    PREPARE stmt FROM @final_sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    
END$$

DELIMITER ;

-- ========================================
-- 5. 创建权限控制用户（可选）
-- ========================================

-- 创建只能查看加密视图的用户
-- CREATE USER 'mes_viewer'@'%' IDENTIFIED BY 'ViewerPass2025!';

-- 只授予加密视图的查询权限
-- GRANT SELECT ON MES.v_user_encrypted TO 'mes_viewer'@'%';
-- GRANT SELECT ON MES.v_device_encrypted TO 'mes_viewer'@'%';
-- GRANT SELECT ON MES.production_plan TO 'mes_viewer'@'%';
-- GRANT SELECT ON MES.production_task TO 'mes_viewer'@'%';
-- GRANT SELECT ON MES.injection_param TO 'mes_viewer'@'%';
-- GRANT SELECT ON MES.print_pattern TO 'mes_viewer'@'%';

-- 授予存储过程执行权限
-- GRANT EXECUTE ON PROCEDURE MES.sp_query_with_encryption TO 'mes_viewer'@'%';

-- FLUSH PRIVILEGES;

-- ========================================
-- 6. 使用示例和测试查询
-- ========================================

-- 测试加密显示效果
SELECT '=== 原始用户表数据 ===' as info;
SELECT * FROM user;

SELECT '=== 加密显示的用户表数据 ===' as info;
SELECT * FROM v_user_encrypted;

SELECT '=== 原始设备表数据 ===' as info;
SELECT id, device_code, name, last_maintenance_time FROM device LIMIT 3;

SELECT '=== 加密显示的设备表数据 ===' as info;
SELECT id, device_code, name, last_maintenance_time, maintenance_note FROM v_device_encrypted LIMIT 3;

-- 测试存储过程
SELECT '=== 使用存储过程查询用户表 ===' as info;
CALL sp_query_with_encryption('user');