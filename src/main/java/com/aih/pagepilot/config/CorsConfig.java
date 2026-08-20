package com.aih.pagepilot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局跨域问题解决
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Sandboxed preview iframe has origin "null" and loads Vue ES modules via CORS.
        // Must be registered before /** or Spring answers those GETs with 403.
        registry.addMapping("/static/**")
                .allowedOriginPatterns("*")
                .allowCredentials(false)
                .allowedMethods("GET", "HEAD", "OPTIONS")
                .allowedHeaders("*");
        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedOriginPatterns("http://localhost:5175", "http://127.0.0.1:5175")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*");
    }
}