package com.shoujike.process.controller;

import com.shoujike.common.config.FileStorageConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
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
@RequiredArgsConstructor
public class StaticResourceController {

    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8};
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 0x50, 0x4E, 0x47};
    private static final byte[] GIF_MAGIC = {0x47, 0x49, 0x46};
    private static final byte[] WEBP_RIFF = {0x52, 0x49, 0x46, 0x46};
    private static final byte[] WEBP_WEBP = {0x57, 0x45, 0x42, 0x50};

    private final FileStorageConfig fileStorageConfig;

    /**
     * 获取上传的图片文件
     */
    @GetMapping("/patterns/{filename:.+}")
    public ResponseEntity<Resource> getPatternImage(@PathVariable String filename) {
        try {
            log.info("请求图片文件: {}", filename);
            Path filePath = resolveCategoryFile("patterns", filename);
            File file = filePath.toFile();
            Resource resource = new FileSystemResource(file);
            String contentType = detectImageContentType(filePath);

            log.info("返回图片文件: {}, 类型: {}, 大小: {} bytes", 
                    filename, contentType, file.length());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                    .header("X-Content-Type-Options", "nosniff")
                    .body(resource);
                    
        } catch (java.io.FileNotFoundException e) {
            return ResponseEntity.notFound().build();
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
            Path filePath = resolveCategoryFile(category, filename);
            File file = filePath.toFile();
            Resource resource = new FileSystemResource(file);
            String contentType = category.equals("patterns")
                    ? detectImageContentType(filePath)
                    : "application/octet-stream";
            
            log.info("返回文件: {}/{}, 类型: {}, 大小: {} bytes", 
                    category, filename, contentType, file.length());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                    .header("X-Content-Type-Options", "nosniff")
                    .body(resource);
                    
        } catch (java.io.FileNotFoundException e) {
            return ResponseEntity.notFound().build();
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
            Path filePath = resolveCategoryFile(category, filename);
            File file = filePath.toFile();
            boolean exists = file.exists() && file.isFile();
            
            return ResponseEntity.ok()
                    .body(new FileCheckResponse(exists, filePath.toAbsolutePath().toString(), 
                            exists ? file.length() : 0));
                            
        } catch (java.io.FileNotFoundException e) {
            return ResponseEntity.ok().body(new FileCheckResponse(false, "", 0));
        } catch (Exception e) {
            log.error("检查文件失败: {}/{}", category, filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new FileCheckResponse(false, "", 0));
        }
    }

    private Path resolveCategoryFile(String category, String filename) throws IOException {
        Path filePath = getUploadsRoot().resolve(category).resolve(filename).normalize();
        if (!filePath.startsWith(getUploadsRoot())) {
            throw new IOException("非法文件访问");
        }

        File file = filePath.toFile();
        if (!file.exists() || !file.isFile()) {
            throw new java.io.FileNotFoundException("文件不存在");
        }

        return filePath;
    }

    private Path getUploadsRoot() {
        Path patternsDir = Paths.get(fileStorageConfig.getDir()).normalize().toAbsolutePath();
        Path uploadsRoot = patternsDir.getParent();
        return uploadsRoot != null ? uploadsRoot : patternsDir;
    }

    private String detectImageContentType(Path filePath) throws IOException {
        byte[] header = Files.readAllBytes(filePath);
        if (startsWith(header, JPEG_MAGIC)) {
            return MediaType.IMAGE_JPEG_VALUE;
        }
        if (startsWith(header, PNG_MAGIC)) {
            return MediaType.IMAGE_PNG_VALUE;
        }
        if (startsWith(header, GIF_MAGIC)) {
            return MediaType.IMAGE_GIF_VALUE;
        }
        if (startsWith(header, WEBP_RIFF) && containsAt(header, WEBP_WEBP, 8)) {
            return "image/webp";
        }
        throw new IOException("不支持的图片类型");
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
