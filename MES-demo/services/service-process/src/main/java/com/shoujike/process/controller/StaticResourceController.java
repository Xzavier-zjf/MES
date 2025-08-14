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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            
            // 检查文件实际内容来确定正确的Content-Type
            if (contentType == null || shouldCheckFileContent(filename)) {
                contentType = detectContentTypeByContent(file, filename);
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
     * 调试端点：显示当前工作目录和文件查找信息
     */
    @GetMapping("/debug/info")
    public ResponseEntity<?> getDebugInfo() {
        try {
            String currentDir = System.getProperty("user.dir");
            Path uploadsPath = Paths.get(currentDir, UPLOAD_BASE_DIR);
            Path patternsPath = Paths.get(currentDir, UPLOAD_BASE_DIR, "patterns");
            
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("currentWorkingDirectory", currentDir);
            debugInfo.put("uploadsPath", uploadsPath.toAbsolutePath().toString());
            debugInfo.put("patternsPath", patternsPath.toAbsolutePath().toString());
            debugInfo.put("uploadsExists", Files.exists(uploadsPath));
            debugInfo.put("patternsExists", Files.exists(patternsPath));
            
            if (Files.exists(patternsPath)) {
                try {
                    List<String> files = Files.list(patternsPath)
                            .map(path -> path.getFileName().toString())
                            .collect(java.util.stream.Collectors.toList());
                    debugInfo.put("patternFiles", files);
                } catch (Exception e) {
                    debugInfo.put("patternFiles", "Error listing files: " + e.getMessage());
                }
            }
            
            return ResponseEntity.ok(debugInfo);
            
        } catch (Exception e) {
            log.error("获取调试信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * 检查是否需要通过文件内容来确定Content-Type
     */
    private boolean shouldCheckFileContent(String filename) {
        // 对于可能包含SVG内容但使用其他扩展名的文件，需要检查内容
        String lowerName = filename.toLowerCase();
        return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || 
               lowerName.endsWith(".png") || lowerName.endsWith(".gif");
    }
    
    /**
     * 通过文件内容检测Content-Type
     */
    private String detectContentTypeByContent(File file, String filename) {
        try {
            // 读取文件开头的几个字节来判断文件类型
            byte[] header = new byte[512];
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                int bytesRead = fis.read(header);
                String headerStr = new String(header, 0, Math.min(bytesRead, 100), "UTF-8");
                
                // 检查是否是SVG内容
                if (headerStr.contains("<svg") || headerStr.contains("<?xml")) {
                    log.info("检测到SVG内容，文件: {}", filename);
                    return "image/svg+xml";
                }
                
                // 检查是否是HTML内容
                if (headerStr.contains("<!DOCTYPE html") || headerStr.contains("<html")) {
                    log.info("检测到HTML内容，文件: {}", filename);
                    return "text/html";
                }
                
                // 检查JPEG文件头
                if (header[0] == (byte)0xFF && header[1] == (byte)0xD8) {
                    return "image/jpeg";
                }
                
                // 检查PNG文件头
                if (header[0] == (byte)0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G') {
                    return "image/png";
                }
                
                // 检查GIF文件头
                if ((header[0] == 'G' && header[1] == 'I' && header[2] == 'F') ||
                    (header[0] == (byte)0x47 && header[1] == (byte)0x49 && header[2] == (byte)0x46)) {
                    return "image/gif";
                }
                
            }
        } catch (Exception e) {
            log.warn("检测文件内容类型失败: {}", filename, e);
        }
        
        // 如果无法检测，根据扩展名返回默认类型
        String fileName = filename.toLowerCase();
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".svg")) {
            return "image/svg+xml";
        } else {
            return "application/octet-stream";
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