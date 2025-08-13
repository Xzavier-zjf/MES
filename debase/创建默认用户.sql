-- ========================================
-- MES系统默认用户创建脚本
-- ========================================

USE MES;

-- 检查是否存在admin用户
SELECT 'admin用户检查:' as info;
SELECT id, username, status, account_locked, login_attempts, created_at 
FROM user 
WHERE username = 'admin';

-- 如果admin用户被锁定，解锁它
UPDATE user 
SET account_locked = FALSE, 
    login_attempts = 0 
WHERE username = 'admin' AND account_locked = TRUE;

-- 如果admin用户不存在，创建它
INSERT IGNORE INTO user (username, password_hash, salt, email, phone, role, status, account_locked, login_attempts, created_at)
SELECT 
    'admin' as username,
    SHA2(CONCAT('123456', 'admin_salt'), 256) as password_hash,
    'admin_salt' as salt,
    'admin@mes.com' as email,
    '13800138000' as phone,
    'admin' as role,
    'active' as status,
    FALSE as account_locked,
    0 as login_attempts,
    CURRENT_TIMESTAMP as created_at
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'admin');

-- 创建测试用户
INSERT IGNORE INTO user (username, password_hash, salt, email, phone, role, status, account_locked, login_attempts, created_at)
SELECT 
    'testuser' as username,
    SHA2(CONCAT('Test123456', 'test_salt'), 256) as password_hash,
    'test_salt' as salt,
    'test@mes.com' as email,
    '13800138001' as phone,
    'user' as role,
    'active' as status,
    FALSE as account_locked,
    0 as login_attempts,
    CURRENT_TIMESTAMP as created_at
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'testuser');

-- 验证用户创建结果
SELECT '用户创建结果:' as info;
SELECT id, username, email, role, status, account_locked, login_attempts, created_at 
FROM user 
WHERE username IN ('admin', 'testuser')
ORDER BY username;

-- 测试admin用户登录
SELECT '测试admin用户登录:' as info;
CALL ValidateUserPassword('admin', '123456', @result, @user_id, @message);
SELECT 
    @result as result_code, 
    @user_id as user_id, 
    @message as message,
    CASE 
        WHEN @result = 1 THEN '✅ 登录成功'
        WHEN @result = -1 THEN '❌ 用户不存在'
        WHEN @result = -2 THEN '🔒 账户被锁定'
        WHEN @result = -3 THEN '🔒 账户因密码错误过多被锁定'
        WHEN @result = -4 THEN '❌ 密码错误'
        ELSE '❓ 未知状态'
    END as status_description;

-- 测试testuser用户登录
SELECT '测试testuser用户登录:' as info;
CALL ValidateUserPassword('testuser', 'Test123456', @result2, @user_id2, @message2);
SELECT 
    @result2 as result_code, 
    @user_id2 as user_id, 
    @message2 as message,
    CASE 
        WHEN @result2 = 1 THEN '✅ 登录成功'
        WHEN @result2 = -1 THEN '❌ 用户不存在'
        WHEN @result2 = -2 THEN '🔒 账户被锁定'
        WHEN @result2 = -3 THEN '🔒 账户因密码错误过多被锁定'
        WHEN @result2 = -4 THEN '❌ 密码错误'
        ELSE '❓ 未知状态'
    END as status_description;

-- 显示完成信息
SELECT 
    '========================================' as info
UNION ALL SELECT '默认用户创建完成'
UNION ALL SELECT '用户名: admin, 密码: 123456'
UNION ALL SELECT '用户名: testuser, 密码: Test123456'
UNION ALL SELECT '如果仍有问题，请检查数据库连接和表结构'
UNION ALL SELECT '========================================';