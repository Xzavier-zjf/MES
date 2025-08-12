package com.example.authservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Types;
import java.util.Map;

/**
 * 用户密码服务类
 * 提供密码加密、验证、修改等功能
 */
@Slf4j
@Service
public class UserPasswordService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 验证用户密码
     * @param username 用户名
     * @param password 密码
     * @return 验证结果对象
     */
    public PasswordValidationResult validatePassword(String username, String password) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("ValidateUserPassword")
                    .declareParameters(
                            new SqlParameter("p_username", Types.VARCHAR),
                            new SqlParameter("p_password", Types.VARCHAR),
                            new SqlOutParameter("p_result", Types.INTEGER),
                            new SqlOutParameter("p_user_id", Types.INTEGER),
                            new SqlOutParameter("p_message", Types.VARCHAR)
                    );

            Map<String, Object> result = jdbcCall.execute(username, password);
            
            int resultCode = (Integer) result.get("p_result");
            int userId = (Integer) result.get("p_user_id");
            String message = (String) result.get("p_message");

            log.info("密码验证结果 - 用户: {}, 结果码: {}, 消息: {}", username, resultCode, message);

            return new PasswordValidationResult(resultCode, userId, message);
        } catch (Exception e) {
            log.error("密码验证失败", e);
            return new PasswordValidationResult(-999, 0, "系统错误");
        }
    }

    /**
     * 创建新用户
     * @param username 用户名
     * @param password 密码
     * @param email 邮箱
     * @param phone 手机号
     * @param role 角色
     * @return 创建结果
     */
    public UserOperationResult createUser(String username, String password, String email, String phone, String role) {
        try {
            // 密码强度验证
            if (!isPasswordStrong(password)) {
                return new UserOperationResult(-1, "密码强度不足，请使用至少8位包含字母和数字的密码");
            }

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("CreateUser")
                    .declareParameters(
                            new SqlParameter("p_username", Types.VARCHAR),
                            new SqlParameter("p_password", Types.VARCHAR),
                            new SqlParameter("p_email", Types.VARCHAR),
                            new SqlParameter("p_phone", Types.VARCHAR),
                            new SqlParameter("p_role", Types.VARCHAR),
                            new SqlOutParameter("p_result", Types.INTEGER),
                            new SqlOutParameter("p_message", Types.VARCHAR)
                    );

            Map<String, Object> result = jdbcCall.execute(username, password, email, phone, role);
            
            int resultCode = (Integer) result.get("p_result");
            String message = (String) result.get("p_message");

            log.info("用户创建结果 - 用户: {}, 结果码: {}, 消息: {}", username, resultCode, message);

            return new UserOperationResult(resultCode, message);
        } catch (Exception e) {
            log.error("用户创建失败", e);
            return new UserOperationResult(-999, "系统错误");
        }
    }

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    public UserOperationResult changePassword(int userId, String oldPassword, String newPassword) {
        try {
            // 密码强度验证
            if (!isPasswordStrong(newPassword)) {
                return new UserOperationResult(-1, "新密码强度不足，请使用至少8位包含字母和数字的密码");
            }

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("ChangePassword")
                    .declareParameters(
                            new SqlParameter("p_user_id", Types.INTEGER),
                            new SqlParameter("p_old_password", Types.VARCHAR),
                            new SqlParameter("p_new_password", Types.VARCHAR),
                            new SqlOutParameter("p_result", Types.INTEGER),
                            new SqlOutParameter("p_message", Types.VARCHAR)
                    );

            Map<String, Object> result = jdbcCall.execute(userId, oldPassword, newPassword);
            
            int resultCode = (Integer) result.get("p_result");
            String message = (String) result.get("p_message");

            log.info("密码修改结果 - 用户ID: {}, 结果码: {}, 消息: {}", userId, resultCode, message);

            return new UserOperationResult(resultCode, message);
        } catch (Exception e) {
            log.error("密码修改失败", e);
            return new UserOperationResult(-999, "系统错误");
        }
    }

    /**
     * 解锁用户账户（管理员功能）
     * @param userId 用户ID
     * @return 操作结果
     */
    public UserOperationResult unlockUser(int userId) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("UnlockUser")
                    .declareParameters(
                            new SqlParameter("p_user_id", Types.INTEGER),
                            new SqlOutParameter("p_result", Types.INTEGER),
                            new SqlOutParameter("p_message", Types.VARCHAR)
                    );

            Map<String, Object> result = jdbcCall.execute(userId);
            
            int resultCode = (Integer) result.get("p_result");
            String message = (String) result.get("p_message");

            log.info("用户解锁结果 - 用户ID: {}, 结果码: {}, 消息: {}", userId, resultCode, message);

            return new UserOperationResult(resultCode, message);
        } catch (Exception e) {
            log.error("用户解锁失败", e);
            return new UserOperationResult(-999, "系统错误");
        }
    }

    /**
     * 重置用户密码（管理员功能）
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 操作结果
     */
    public UserOperationResult resetUserPassword(int userId, String newPassword) {
        try {
            // 密码强度验证
            if (!isPasswordStrong(newPassword)) {
                return new UserOperationResult(-1, "新密码强度不足，请使用至少8位包含字母和数字的密码");
            }

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("ResetUserPassword")
                    .declareParameters(
                            new SqlParameter("p_user_id", Types.INTEGER),
                            new SqlParameter("p_new_password", Types.VARCHAR),
                            new SqlOutParameter("p_result", Types.INTEGER),
                            new SqlOutParameter("p_message", Types.VARCHAR)
                    );

            Map<String, Object> result = jdbcCall.execute(userId, newPassword);
            
            int resultCode = (Integer) result.get("p_result");
            String message = (String) result.get("p_message");

            log.info("密码重置结果 - 用户ID: {}, 结果码: {}, 消息: {}", userId, resultCode, message);

            return new UserOperationResult(resultCode, message);
        } catch (Exception e) {
            log.error("密码重置失败", e);
            return new UserOperationResult(-999, "系统错误");
        }
    }

    /**
     * 客户端密码加密（用于前端预加密）
     * @param password 原始密码
     * @param salt 盐值
     * @return 加密后的密码
     */
    public String encryptPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedPassword = password + salt;
            byte[] hash = digest.digest(saltedPassword.getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("密码加密失败", e);
            throw new RuntimeException("密码加密失败", e);
        }
    }

    /**
     * 生成随机盐值
     * @return 盐值
     */
    public String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : salt) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString().substring(0, 16);
    }

    /**
     * 验证密码强度
     * @param password 密码
     * @return 是否符合强度要求
     */
    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasLetter = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        
        return hasLetter && hasDigit;
    }

    /**
     * 密码验证结果类
     */
    public static class PasswordValidationResult {
        private final int resultCode;
        private final int userId;
        private final String message;

        public PasswordValidationResult(int resultCode, int userId, String message) {
            this.resultCode = resultCode;
            this.userId = userId;
            this.message = message;
        }

        public boolean isSuccess() {
            return resultCode == 1;
        }

        public boolean isUserNotFound() {
            return resultCode == -1;
        }

        public boolean isAccountLocked() {
            return resultCode == -2 || resultCode == -3;
        }

        public boolean isPasswordWrong() {
            return resultCode == -4;
        }

        // Getters
        public int getResultCode() { return resultCode; }
        public int getUserId() { return userId; }
        public String getMessage() { return message; }
    }

    /**
     * 用户操作结果类
     */
    public static class UserOperationResult {
        private final int resultCode;
        private final String message;

        public UserOperationResult(int resultCode, String message) {
            this.resultCode = resultCode;
            this.message = message;
        }

        public boolean isSuccess() {
            return resultCode == 1;
        }

        // Getters
        public int getResultCode() { return resultCode; }
        public String getMessage() { return message; }
    }
}