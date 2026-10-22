package com.roof.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Permite acceso a todas las rutas de tu API
                        .allowedOrigins("*") // Permite peticiones desde cualquier IP o URL
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Permite todos estos métodos
                        .allowedHeaders("*"); // Permite cualquier tipo de encabezado
            }
        };
    }
}