package com.example.PROYECTO.FINAL_WEB.config;

import com.example.PROYECTO.FINAL_WEB.service.HotelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Configuración de inicio de la aplicación
 */
@Slf4j
@Component
public class StartupConfig {

    @Autowired
    private HotelService hotelService;

    /**
     * Se ejecuta cuando la aplicación está completamente iniciada
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("=== Aplicación iniciada - Sincronizando precios de hoteles ===");
        try {
            hotelService.actualizarTodosLosPreciosHoteles();
            log.info("=== Sincronización de precios completada ===");
        } catch (Exception e) {
            log.error("Error al sincronizar precios de hoteles", e);
        }
    }
}
