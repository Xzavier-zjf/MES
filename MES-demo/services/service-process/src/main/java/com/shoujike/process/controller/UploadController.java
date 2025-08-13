package com.shoujike.process.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
// CORS配置已在WebConfig中全局配置，移除重复配置
public class UploadController {
    
    private static final String UPLOAD_DIR = "uploads/patterns/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    
    /**
     * 上传图片文件
     */
    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            log.info("开始上传图片文件: {}", file.getOriginalFilename());
            
            // 验证文件
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("文件不能为空"));
            }
            
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body(createErrorResponse("文件大小不能超过5MB"));
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(createErrorResponse("只能上传图片文件"));
            }
            
            // 创建上传目录
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("创建上传目录: {}", uploadPath.toAbsolutePath());
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + "_" + originalFilename;
            
            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("文件上传成功: {}", filePath.toAbsolutePath());
            
            // 返回成功响应
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "文件上传成功");
            result.put("url", "/" + UPLOAD_DIR + filename);
            result.put("path", "/" + UPLOAD_DIR + filename);
            result.put("filename", filename);
            result.put("originalFilename", originalFilename);
            result.put("size", file.getSize());
            result.put("contentType", contentType);
            
            return ResponseEntity.ok(result);
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ResponseEntity.status(500).body(createErrorResponse("文件上传失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("文件上传异常", e);
            return ResponseEntity.status(500).body(createErrorResponse("系统错误: " + e.getMessage()));
        }
    }
    
    /**
     * 删除图片文件
     */
    @DeleteMapping("/image")
    public ResponseEntity<?> deleteImage(@RequestParam("path") String imagePath) {
        try {
            log.info("删除图片文件: {}", imagePath);
            
            // 安全检查：确保路径在允许的目录内
            if (!imagePath.startsWith("/" + UPLOAD_DIR)) {
                return ResponseEntity.badRequest().body(createErrorResponse("无效的文件路径"));
            }
            
            // 移除开头的斜杠
            String relativePath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
            Path filePath = Paths.get(relativePath);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("文件删除成功: {}", filePath.toAbsolutePath());
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "文件删除成功");
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("文件不存在"));
            }
            
        } catch (IOException e) {
            log.error("文件删除失败", e);
            return ResponseEntity.status(500).body(createErrorResponse("文件删除失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("文件删除异常", e);
            return ResponseEntity.status(500).body(createErrorResponse("系统错误: " + e.getMessage()));
        }
    }
    
    /**
     * 获取图片信息
     */
    @GetMapping("/image/info")
    public ResponseEntity<?> getImageInfo(@RequestParam("path") String imagePath) {
        try {
            // 安全检查
            if (!imagePath.startsWith("/" + UPLOAD_DIR)) {
                return ResponseEntity.badRequest().body(createErrorResponse("无效的文件路径"));
            }
            
            String relativePath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
            Path filePath = Paths.get(relativePath);
            
            if (Files.exists(filePath)) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("exists", true);
                result.put("path", imagePath);
                result.put("size", Files.size(filePath));
                result.put("lastModified", Files.getLastModifiedTime(filePath).toString());
                
                return ResponseEntity.ok(result);
            } else {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("exists", false);
                result.put("path", imagePath);
                
                return ResponseEntity.ok(result);
            }
            
        } catch (IOException e) {
            log.error("获取文件信息失败", e);
            return ResponseEntity.status(500).body(createErrorResponse("获取文件信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        return error;
    }
}