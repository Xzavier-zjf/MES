package com.shoujike.process.controller;

import com.shoujike.common.config.FileStorageConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8};
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 0x50, 0x4E, 0x47};
    private static final byte[] GIF_MAGIC = {0x47, 0x49, 0x46};
    private static final byte[] WEBP_RIFF = {0x52, 0x49, 0x46, 0x46};
    private static final byte[] WEBP_WEBP = {0x57, 0x45, 0x42, 0x50};

    private final FileStorageConfig fileStorageConfig;
    
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
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
                return ResponseEntity.badRequest().body(createErrorResponse("只能上传图片文件"));
            }

            if (!isSupportedImage(file)) {
                return ResponseEntity.badRequest().body(createErrorResponse("图片内容校验失败，仅支持 JPG/PNG/GIF/WEBP"));
            }

            Path uploadPath = getUploadPath();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("创建上传目录: {}", uploadPath.toAbsolutePath());
            }

            String originalFilename = file.getOriginalFilename();
            String filename = UUID.randomUUID().toString() + "_" + originalFilename;

            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("文件上传成功: {}", filePath.toAbsolutePath());
            
            // 返回成功响应
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "文件上传成功");
            result.put("url", "/uploads/patterns/" + filename);
            result.put("path", "/uploads/patterns/" + filename);
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
            
            if (!imagePath.startsWith("/uploads/patterns/")) {
                return ResponseEntity.badRequest().body(createErrorResponse("无效的文件路径"));
            }

            String fileName = imagePath.substring("/uploads/patterns/".length());
            Path filePath = getUploadPath().resolve(fileName).normalize();
            if (!filePath.startsWith(getUploadPath())) {
                return ResponseEntity.badRequest().body(createErrorResponse("无效的文件路径"));
            }
            
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
            if (!imagePath.startsWith("/uploads/patterns/")) {
                return ResponseEntity.badRequest().body(createErrorResponse("无效的文件路径"));
            }

            String fileName = imagePath.substring("/uploads/patterns/".length());
            Path filePath = getUploadPath().resolve(fileName).normalize();
            if (!filePath.startsWith(getUploadPath())) {
                return ResponseEntity.badRequest().body(createErrorResponse("无效的文件路径"));
            }
            
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

    private Path getUploadPath() {
        return Paths.get(fileStorageConfig.getDir()).normalize().toAbsolutePath();
    }

    private boolean isSupportedImage(MultipartFile file) throws IOException {
        byte[] header = file.getInputStream().readNBytes(12);
        if (header.length < 3) {
            return false;
        }

        return startsWith(header, JPEG_MAGIC)
                || startsWith(header, PNG_MAGIC)
                || startsWith(header, GIF_MAGIC)
                || (startsWith(header, WEBP_RIFF) && containsAt(header, WEBP_WEBP, 8));
    }

    private boolean startsWith(byte[] data, byte[] signature) {
        if (data.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (data[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean containsAt(byte[] data, byte[] signature, int offset) {
        if (data.length < offset + signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (data[offset + i] != signature[i]) {
                return false;
            }
        }
        return true;
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
