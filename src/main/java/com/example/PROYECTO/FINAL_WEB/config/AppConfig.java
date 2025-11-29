package com.example.PROYECTO.FINAL_WEB.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración para beans de aplicación
 */
@Configuration
public class AppConfig {

    /**
     * Bean RestTemplate para llamadas HTTP externas (Culqi API)
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
