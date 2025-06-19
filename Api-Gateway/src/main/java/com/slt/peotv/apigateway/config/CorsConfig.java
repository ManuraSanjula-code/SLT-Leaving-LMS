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

        /*corsConfig.addAllowedOrigin("https://localhost");
        corsConfig.addAllowedOrigin("http://localhost");
        corsConfig.addAllowedOrigin("http://localhost:8080");
        corsConfig.addAllowedOrigin("http://localhost:3000");

        corsConfig.addAllowedOrigin("https://192.168.3.20");
        corsConfig.addAllowedOrigin("http://192.168.3.20");
        corsConfig.addAllowedOrigin("http://192.168.3.20:8080");
        corsConfig.addAllowedOrigin("http://192.168.3.20:3000");*/

        corsConfig.addAllowedOriginPattern("*");
        corsConfig.addAllowedMethod("*");
        corsConfig.addAllowedHeader("*");

        corsConfig.addExposedHeader("Authorization");
        corsConfig.addExposedHeader("UserID");

        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}