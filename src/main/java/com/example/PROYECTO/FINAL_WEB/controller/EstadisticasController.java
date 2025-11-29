package com.example.PROYECTO.FINAL_WEB.controller;

import com.example.PROYECTO.FINAL_WEB.dto.response.ApiResponse;
import com.example.PROYECTO.FINAL_WEB.entity.Hotel;
import com.example.PROYECTO.FINAL_WEB.entity.Review;
import com.example.PROYECTO.FINAL_WEB.service.EstadisticasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador de Estadísticas - Demuestra el uso de EntityManager
 * Este controlador es opcional y no afecta el flujo principal del proyecto
 */
@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
public class EstadisticasController {

    private final EstadisticasService estadisticasService;

    /**
     * Obtiene estadísticas generales usando EntityManager
     */
    @GetMapping("/generales")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerEstadisticasGenerales() {
        Map<String, Object> estadisticas = estadisticasService.obtenerEstadisticasGenerales();
        return ResponseEntity.ok(ApiResponse.success(estadisticas, "Estadísticas obtenidas correctamente"));
    }

    /**
     * Obtiene hoteles usando Named Query
     */
    @GetMapping("/hoteles/estado/{estado}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Hotel>>> obtenerHotelesPorEstado(@PathVariable String estado) {
        List<Hotel> hoteles = estadisticasService.obtenerHotelesPorEstadoNamedQuery(estado);
        return ResponseEntity.ok(ApiResponse.success(hoteles, 
            "Hoteles obtenidos usando Named Query: " + hoteles.size() + " encontrados"));
    }

    /**
     * Obtiene reviews usando Named Query con parámetros
     */
    @GetMapping("/reviews/puntuacion-minima/{puntuacion}")
    public ResponseEntity<ApiResponse<List<Review>>> obtenerReviewsPorPuntuacion(
            @PathVariable Integer puntuacion) {
        List<Review> reviews = estadisticasService.obtenerReviewsPorPuntuacionMinima(puntuacion);
        return ResponseEntity.ok(ApiResponse.success(reviews, 
            "Reviews obtenidas usando Named Query: " + reviews.size() + " encontradas"));
    }

    /**
     * Búsqueda usando Criteria API
     */
    @GetMapping("/hoteles/buscar")
    public ResponseEntity<ApiResponse<List<Hotel>>> buscarHotelesCriteria(
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) Integer estrellasMinimas) {
        List<Hotel> hoteles = estadisticasService.buscarHotelesConCriteriaAPI(ciudad, estrellasMinimas);
        return ResponseEntity.ok(ApiResponse.success(hoteles, 
            "Búsqueda con Criteria API completada: " + hoteles.size() + " hoteles"));
    }

    /**
     * Obtiene reporte mensual usando consulta nativa SQL
     */
    @GetMapping("/reporte-mensual")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    public ResponseEntity<ApiResponse<List<Object[]>>> obtenerReporteMensual(
            @RequestParam int anio,
            @RequestParam int mes) {
        List<Object[]> reporte = estadisticasService.obtenerReporteMensualReservas(anio, mes);
        return ResponseEntity.ok(ApiResponse.success(reporte, 
            "Reporte mensual obtenido con consulta nativa SQL"));
    }
}
