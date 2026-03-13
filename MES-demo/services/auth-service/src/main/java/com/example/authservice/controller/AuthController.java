package com.example.authservice.controller;

import com.example.authservice.service.UserPasswordService;
import com.example.authservice.service.UserPasswordService.PasswordValidationResult;
import com.example.authservice.service.UserPasswordService.UserOperationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录、注册、密码管理等功能
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
// CORS配置已在网关层统一处理，移除重复配置
public class AuthController {

    @Autowired
    private UserPasswordService userPasswordService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        try {
            log.info("用户登录尝试: {}", request.getUsername());

            PasswordValidationResult result = userPasswordService.validatePassword(
                    request.getUsername(),
                    request.getPassword());

            Map<String, Object> response = new HashMap<>();

            if (result.isSuccess()) {
                // 登录成功，设置会话
                session.setAttribute("userId", result.getUserId());
                session.setAttribute("username", request.getUsername());

                response.put("success", true);
                response.put("message", "登录成功");
                response.put("userId", result.getUserId());
                response.put("username", request.getUsername());

                log.info("用户登录成功: {} (ID: {})", request.getUsername(), result.getUserId());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.getMessage());
                response.put("resultCode", result.getResultCode());

                HttpStatus status = HttpStatus.UNAUTHORIZED;
                if (result.isAccountLocked()) {
                    status = HttpStatus.LOCKED;
                } else if (result.isUserNotFound()) {
                    status = HttpStatus.NOT_FOUND;
                }

                log.warn("用户登录失败: {} - {}", request.getUsername(), result.getMessage());
                return ResponseEntity.status(status).body(response);
            }
        } catch (Exception e) {
            log.error("登录处理异常", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "系统错误，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            log.info("用户注册尝试: {}", request.getUsername());

            UserOperationResult result = userPasswordService.createUser(
                    request.getUsername(),
                    request.getPassword(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getRole());

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());

            if (result.isSuccess()) {
                log.info("用户注册成功: {}", request.getUsername());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                log.warn("用户注册失败: {} - {}", request.getUsername(), result.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("注册处理异常", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "系统错误，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            log.info("用户修改密码: {}", userId);

            UserOperationResult result = userPasswordService.changePassword(
                    userId,
                    request.getOldPassword(),
                    request.getNewPassword());

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());

            if (result.isSuccess()) {
                log.info("用户密码修改成功: {}", userId);
                return ResponseEntity.ok(response);
            } else {
                log.warn("用户密码修改失败: {} - {}", userId, result.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("密码修改处理异常", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "系统错误，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        try {
            String username = (String) session.getAttribute("username");
            session.invalidate();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登出成功");

            log.info("用户登出: {}", username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("登出处理异常", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "系统错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            String username = (String) session.getAttribute("username");

            if (userId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("username", username);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取用户信息异常", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "系统错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 解锁用户（管理员功能）
     */
    @PostMapping("/admin/unlock-user/{userId}")
    public ResponseEntity<?> unlockUser(@PathVariable int userId, HttpSession session) {
        try {
            // 检查管理员权限（这里简化处理，实际应该检查角色）
            Integer currentUserId = (Integer) session.getAttribute("userId");
            if (currentUserId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            log.info("管理员解锁用户: {} by {}", userId, currentUserId);

            UserOperationResult result = userPasswordService.unlockUser(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());

            if (result.isSuccess()) {
                log.info("用户解锁成功: {}", userId);
                return ResponseEntity.ok(response);
            } else {
                log.warn("用户解锁失败: {} - {}", userId, result.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("用户解锁处理异常", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "系统错误，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 重置用户密码（管理员功能）
     */
    @PostMapping("/admin/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request, HttpSession session) {
        try {
            // 检查管理员权限
            Integer currentUserId = (Integer) session.getAttribute("userId");
            if (currentUserId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            log.info("管理员重置密码: {} by {}", request.getUserId(), currentUserId);

            UserOperationResult result = userPasswordService.resetUserPassword(
                    request.getUserId(),
                    request.getNewPassword());

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());

            if (result.isSuccess()) {
                log.info("密码重置成功: {}", request.getUserId());
                return ResponseEntity.ok(response);
            } else {
                log.warn("密码重置失败: {} - {}", request.getUserId(), result.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("密码重置处理异常", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "系统错误，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // DTO类定义
    public static class LoginRequest {
        private String username;
        private String password;

        // Getters and Setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
        private String phone;
        private String role;

        // Getters and Setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;

        // Getters and Setters
        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    public static class ResetPasswordRequest {
        private int userId;
        private String newPassword;

        // Getters and Setters
        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}