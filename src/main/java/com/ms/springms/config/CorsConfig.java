package com.ms.springms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:3000",
                        "http://172.20.10.2:3000",
                        "http://192.168.199.199:3000",
                        "http://192.168.1.7:3000" // home
                )
                .allowedMethods("GET","POST","PUT","DELETE")
                .allowedHeaders("Content-Type", "Authorization")
                .exposedHeaders("Content-Disposition") // Menambahkan header yang diizinkan untuk diakses
                .allowCredentials(true) // Mengizinkan kredensial, jika diperlukan
                .maxAge(3600); // Batas waktu untuk caching konfigurasi CORS
    }
}