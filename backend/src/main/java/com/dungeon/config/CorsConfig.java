package com.dungeon.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的源（使用 addAllowedOriginPattern 以支持 Spring Boot 2.4+）
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            // 支持逗号分隔的多个origin
            String[] origins = allowedOrigins.split(",");
            for (String origin : origins) {
                origin = origin.trim();
                if (!origin.isEmpty()) {
                    config.addAllowedOriginPattern(origin);
                }
            }
        } else {
            // 如果没有配置，允许所有源（仅开发环境）
            config.addAllowedOriginPattern("*");
        }
        
        // 允许的请求方法
        config.addAllowedMethod("*");
        // 允许的请求头
        config.addAllowedHeader("*");
        // 允许携带凭证
        config.setAllowCredentials(true);
        // 预检请求的缓存时间
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

