package com.shoujike.process.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 静态资源控制器
 * 专门处理图片等静态资源的访问
 */
@Slf4j
@RestController
@RequestMapping("/api/uploads")
public class StaticResourceController {

    private static final String UPLOAD_BASE_DIR = "uploads";

    /**
     * 获取上传的图片文件
     */
    @GetMapping("/patterns/{filename:.+}")
    public ResponseEntity<Resource> getPatternImage(@PathVariable String filename) {
        try {
            log.info("请求图片文件: {}", filename);
            
            // 构建文件路径
            String currentDir = System.getProperty("user.dir");
            Path filePath = Paths.get(currentDir, UPLOAD_BASE_DIR, "patterns", filename);
            
            log.info("查找文件路径: {}", filePath.toAbsolutePath());
            
            File file = filePath.toFile();
            if (!file.exists() || !file.isFile()) {
                log.warn("文件不存在: {}", filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }
            
            // 检查文件是否在允许的目录内（安全检查）
            String canonicalPath = file.getCanonicalPath();
            String allowedPath = Paths.get(currentDir, UPLOAD_BASE_DIR).toFile().getCanonicalPath();
            if (!canonicalPath.startsWith(allowedPath)) {
                log.warn("非法文件访问尝试: {}", canonicalPath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // 创建资源
            Resource resource = new FileSystemResource(file);
            
            // 确定内容类型
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                // 根据文件扩展名确定内容类型
                String fileName = filename.toLowerCase();
                if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (fileName.endsWith(".png")) {
                    contentType = "image/png";
                } else if (fileName.endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (fileName.endsWith(".webp")) {
                    contentType = "image/webp";
                } else {
                    contentType = "application/octet-stream";
                }
            }
            
            log.info("返回图片文件: {}, 类型: {}, 大小: {} bytes", 
                    filename, contentType, file.length());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("获取图片文件失败: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取任意上传文件
     */
    @GetMapping("/{category}/{filename:.+}")
    public ResponseEntity<Resource> getUploadedFile(
            @PathVariable String category, 
            @PathVariable String filename) {
        try {
            log.info("请求文件: {}/{}", category, filename);
            
            // 构建文件路径
            String currentDir = System.getProperty("user.dir");
            Path filePath = Paths.get(currentDir, UPLOAD_BASE_DIR, category, filename);
            
            log.info("查找文件路径: {}", filePath.toAbsolutePath());
            
            File file = filePath.toFile();
            if (!file.exists() || !file.isFile()) {
                log.warn("文件不存在: {}", filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }
            
            // 安全检查
            String canonicalPath = file.getCanonicalPath();
            String allowedPath = Paths.get(currentDir, UPLOAD_BASE_DIR).toFile().getCanonicalPath();
            if (!canonicalPath.startsWith(allowedPath)) {
                log.warn("非法文件访问尝试: {}", canonicalPath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // 创建资源
            Resource resource = new FileSystemResource(file);
            
            // 确定内容类型
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            log.info("返回文件: {}/{}, 类型: {}, 大小: {} bytes", 
                    category, filename, contentType, file.length());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("获取文件失败: {}/{}", category, filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 检查文件是否存在
     */
    @GetMapping("/check/{category}/{filename:.+}")
    public ResponseEntity<?> checkFile(
            @PathVariable String category, 
            @PathVariable String filename) {
        try {
            String currentDir = System.getProperty("user.dir");
            Path filePath = Paths.get(currentDir, UPLOAD_BASE_DIR, category, filename);
            
            File file = filePath.toFile();
            boolean exists = file.exists() && file.isFile();
            
            return ResponseEntity.ok()
                    .body(new FileCheckResponse(exists, filePath.toAbsolutePath().toString(), 
                            exists ? file.length() : 0));
                            
        } catch (Exception e) {
            log.error("检查文件失败: {}/{}", category, filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new FileCheckResponse(false, "", 0));
        }
    }

    /**
     * 文件检查响应类
     */
    public static class FileCheckResponse {
        public boolean exists;
        public String path;
        public long size;

        public FileCheckResponse(boolean exists, String path, long size) {
            this.exists = exists;
            this.path = path;
            this.size = size;
        }
    }
}