package com.slt.peotv.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Allowed origins
        corsConfig.addAllowedOrigin("https://localhost");  // Web frontend
        corsConfig.addAllowedOrigin("http://localhost");  // Web frontend
        corsConfig.addAllowedOrigin("http://10.0.2.2");  // Android Emulator
        corsConfig.addAllowedOrigin("http://192.168.1.8"); // Local network (Android devices)
        corsConfig.addAllowedOriginPattern("*"); // Allow all for development

        // Allowed methods
        corsConfig.addAllowedMethod("*"); // Allow all HTTP methods

        // Allowed headers
        corsConfig.addAllowedHeader("*"); // Allow all headers

        // Expose specific headers (e.g., Authorization)
        corsConfig.addExposedHeader("Authorization");
        corsConfig.addExposedHeader("UserID");

        // Allow credentials (e.g., cookies, authorization headers)
        corsConfig.setAllowCredentials(true);

        // Register CORS configuration for all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}