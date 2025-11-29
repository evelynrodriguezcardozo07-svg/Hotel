package com.example.PROYECTO.FINAL_WEB.controller;

import com.example.PROYECTO.FINAL_WEB.dto.response.ApiResponse;
import com.example.PROYECTO.FINAL_WEB.service.DisponibilidadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/disponibilidad")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    @GetMapping("/habitacion/{habitacionId}/precios")
    public ResponseEntity<ApiResponse<Map<LocalDate, BigDecimal>>> obtenerPrecios(
        @PathVariable Long habitacionId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        try {
            Map<LocalDate, BigDecimal> precios = disponibilidadService
                .obtenerPreciosPorFecha(habitacionId, inicio, fin);
            return ResponseEntity.ok(ApiResponse.success(precios));
        } catch (Exception e) {
            log.error("Error obteniendo precios: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Error al obtener precios"));
        }
    }

    @GetMapping("/habitacion/{habitacionId}/disponible")
    public ResponseEntity<ApiResponse<Boolean>> verificarDisponibilidad(
        @PathVariable Long habitacionId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        try {
            boolean disponible = disponibilidadService.estaDisponible(habitacionId, inicio, fin);
            return ResponseEntity.ok(ApiResponse.success(disponible));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Error al verificar disponibilidad"));
        }
    }
}
