package com.shoujike.process.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 配置静态资源访问和CORS
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * 配置静态资源访问路径
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取当前工作目录
        String currentDir = System.getProperty("user.dir");
        System.out.println("当前工作目录: " + currentDir);
        
        // 配置上传文件的静态资源访问
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + currentDir + "/uploads/")
                .setCachePeriod(3600); // 缓存1小时
        
        System.out.println("配置静态资源路径: /uploads/** -> file:" + currentDir + "/uploads/");
        
        // 添加调试日志
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations("file:" + currentDir + "/uploads/")
                .setCachePeriod(3600);
                
        System.out.println("配置API静态资源路径: /api/uploads/** -> file:" + currentDir + "/uploads/");
    }
    
    /**
     * CORS配置已在网关层统一处理，移除重复配置以避免冲突
     */
    // @Override
    // public void addCorsMappings(CorsRegistry registry) {
    //     // 已在网关层配置CORS，此处不再重复配置
    // }
}