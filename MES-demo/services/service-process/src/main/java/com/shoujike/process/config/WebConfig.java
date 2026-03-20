package com.shoujike.process.config;

import com.shoujike.common.config.FileStorageConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web配置类
 * 配置静态资源访问和CORS
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageConfig fileStorageConfig;
    
    /**
     * 配置静态资源访问路径
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadsRoot = getUploadsRootPath();
        String resourceLocation = uploadsRoot.toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation)
                .setCachePeriod(3600);

        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations(resourceLocation)
                .setCachePeriod(3600);
    }

    private Path getUploadsRootPath() {
        Path patternsDir = Paths.get(fileStorageConfig.getDir()).normalize().toAbsolutePath();
        Path uploadsRoot = patternsDir.getParent();
        return uploadsRoot != null ? uploadsRoot : patternsDir;
    }
}
