-- ========================================
-- 印刷图案预览图数据修复脚本
-- ========================================

USE MES;

-- 1. 检查当前图案数据状态
SELECT '=== 当前图案数据检查 ===' as info;

SELECT 
    id,
    pattern_code,
    pattern_name,
    image_url,
    CASE 
        WHEN image_url IS NULL THEN '❌ 无图片'
        WHEN image_url = '' THEN '❌ 图片路径为空'
        WHEN image_url LIKE 'http%' THEN '✅ 完整URL'
        WHEN image_url LIKE '/uploads/%' THEN '✅ 相对路径'
        ELSE '⚠️ 其他格式'
    END as image_status
FROM print_pattern
ORDER BY id;

-- 2. 统计图片状态
SELECT '=== 图片状态统计 ===' as info;

SELECT 
    '总图案数' as status_type,
    COUNT(*) as count
FROM print_pattern
UNION ALL
SELECT 
    '有图片的图案',
    COUNT(*)
FROM print_pattern 
WHERE image_url IS NOT NULL AND image_url != ''
UNION ALL
SELECT 
    '无图片的图案',
    COUNT(*)
FROM print_pattern 
WHERE image_url IS NULL OR image_url = ''
UNION ALL
SELECT 
    '相对路径格式',
    COUNT(*)
FROM print_pattern 
WHERE image_url LIKE '/uploads/%'
UNION ALL
SELECT 
    '完整URL格式',
    COUNT(*)
FROM print_pattern 
WHERE image_url LIKE 'http%';

-- 3. 修复图片路径格式（如果需要）
SELECT '=== 开始修复图片路径格式 ===' as info;

-- 备份原始数据
CREATE TABLE IF NOT EXISTS print_pattern_backup AS 
SELECT * FROM print_pattern WHERE 1=0;

INSERT INTO print_pattern_backup 
SELECT * FROM print_pattern 
WHERE image_url IS NOT NULL AND image_url != '';

SELECT '已备份现有图片数据到 print_pattern_backup 表' as info;

-- 修复路径格式：确保所有路径都以 /uploads/patterns/ 开头
UPDATE print_pattern 
SET image_url = CONCAT('/uploads/patterns/', SUBSTRING_INDEX(image_url, '/', -1))
WHERE image_url IS NOT NULL 
AND image_url != ''
AND image_url NOT LIKE '/uploads/patterns/%'
AND image_url NOT LIKE 'http%';

SELECT '已修复图片路径格式' as info;

-- 4. 添加测试图案数据（如果需要）
SELECT '=== 添加测试图案数据 ===' as info;

-- 检查是否需要添加测试数据
INSERT IGNORE INTO print_pattern (
    plan_id, task_id, device_id, quantity, 
    pattern_code, pattern_name, machine_model, 
    image_url, default_print_speed, default_pressure
) VALUES 
('TEST-PLAN-001', 'TEST-TASK-001', 'TEST-DEVICE-001', 100, 
 'PAT-TEST-001', '测试图案1', 'A', 
 '/uploads/patterns/test-pattern-1.jpg', 1000, 2.5),
('TEST-PLAN-002', 'TEST-TASK-002', 'TEST-DEVICE-002', 150, 
 'PAT-TEST-002', '测试图案2', 'B', 
 '/uploads/patterns/test-pattern-2.jpg', 1200, 3.0);

-- 5. 验证修复结果
SELECT '=== 修复结果验证 ===' as info;

SELECT 
    id,
    pattern_code,
    pattern_name,
    image_url,
    CASE 
        WHEN image_url IS NULL THEN '❌ 无图片'
        WHEN image_url = '' THEN '❌ 图片路径为空'
        WHEN image_url LIKE 'http%' THEN '✅ 完整URL'
        WHEN image_url LIKE '/uploads/patterns/%' THEN '✅ 标准相对路径'
        ELSE '⚠️ 需要检查'
    END as image_status
FROM print_pattern
ORDER BY id;

-- 6. 创建图片验证存储过程
DELIMITER //

DROP PROCEDURE IF EXISTS CheckPatternImages//
CREATE PROCEDURE CheckPatternImages()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE pattern_id INT;
    DECLARE pattern_code VARCHAR(50);
    DECLARE image_url VARCHAR(255);
    
    DECLARE cur CURSOR FOR 
        SELECT id, pattern_code, image_url 
        FROM print_pattern 
        WHERE image_url IS NOT NULL AND image_url != '';
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    -- 创建临时结果表
    DROP TEMPORARY TABLE IF EXISTS temp_image_check;
    CREATE TEMPORARY TABLE temp_image_check (
        id INT,
        pattern_code VARCHAR(50),
        image_url VARCHAR(255),
        status VARCHAR(50),
        recommendation TEXT
    );
    
    OPEN cur;
    
    read_loop: LOOP
        FETCH cur INTO pattern_id, pattern_code, image_url;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- 检查图片URL格式
        IF image_url LIKE '/uploads/patterns/%' THEN
            INSERT INTO temp_image_check VALUES (
                pattern_id, pattern_code, image_url, 
                '✅ 路径格式正确', '前端应该能正常显示'
            );
        ELSEIF image_url LIKE 'http%' THEN
            INSERT INTO temp_image_check VALUES (
                pattern_id, pattern_code, image_url, 
                '✅ 完整URL', '外部链接，需要验证可访问性'
            );
        ELSE
            INSERT INTO temp_image_check VALUES (
                pattern_id, pattern_code, image_url, 
                '⚠️ 格式异常', '需要手动检查和修复'
            );
        END IF;
    END LOOP;
    
    CLOSE cur;
    
    -- 显示检查结果
    SELECT * FROM temp_image_check ORDER BY id;
    
    -- 显示统计信息
    SELECT 
        status,
        COUNT(*) as count
    FROM temp_image_check 
    GROUP BY status;
    
END//

DELIMITER ;

-- 执行图片检查
CALL CheckPatternImages();

-- 7. 显示完成信息
SELECT 
    '========================================' as info
UNION ALL SELECT '图案预览图数据修复完成'
UNION ALL SELECT '1. 已备份原始数据到 print_pattern_backup'
UNION ALL SELECT '2. 已修复图片路径格式'
UNION ALL SELECT '3. 已添加测试数据（如果需要）'
UNION ALL SELECT '4. 前端图片URL已修复为正确端口'
UNION ALL SELECT '5. 后端已添加静态资源配置'
UNION ALL SELECT '6. 请重启后端服务以应用配置'
UNION ALL SELECT '========================================';