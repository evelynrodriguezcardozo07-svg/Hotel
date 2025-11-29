package com.example.PROYECTO.FINAL_WEB.controller;

import com.example.PROYECTO.FINAL_WEB.dto.request.HabitacionRequest;
import com.example.PROYECTO.FINAL_WEB.dto.response.ApiResponse;
import com.example.PROYECTO.FINAL_WEB.dto.response.HabitacionResponse;
import com.example.PROYECTO.FINAL_WEB.entity.Usuario;
import com.example.PROYECTO.FINAL_WEB.service.AuthService;
import com.example.PROYECTO.FINAL_WEB.service.HabitacionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller de habitaciones
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HabitacionController {

    @Autowired
    private HabitacionService habitacionService;

    @Autowired
    private AuthService authService;

    /**
     * POST /api/host/habitaciones - Crear habitación
     */
    @PostMapping("/host/habitaciones")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<ApiResponse<HabitacionResponse>> crearHabitacion(@Valid @RequestBody HabitacionRequest request) {
        Usuario usuario = authService.getCurrentUser();
        HabitacionResponse response = habitacionService.crearHabitacion(request, usuario.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Habitación creada"));
    }

    /**
     * GET /api/habitaciones/{id} - Obtener habitación por ID (público)
     */
    @GetMapping("/habitaciones/{id}")
    public ResponseEntity<ApiResponse<HabitacionResponse>> getHabitacion(@PathVariable Long id) {
        HabitacionResponse response = habitacionService.getHabitacionById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Habitación obtenida"));
    }

    /**
     * GET /api/hotels/{hotelId}/habitaciones - Obtener habitaciones de un hotel
     */
    @GetMapping("/hotels/{hotelId}/habitaciones")
    public ResponseEntity<ApiResponse<List<HabitacionResponse>>> getHabitacionesByHotel(@PathVariable Long hotelId) {
        List<HabitacionResponse> response = habitacionService.getHabitacionesByHotelId(hotelId);
        return ResponseEntity.ok(ApiResponse.success(response, "Habitaciones del hotel"));
    }

    /**
     * GET /api/hotels/{hotelId}/habitaciones/disponibles - Buscar habitaciones disponibles
     */
    @GetMapping("/hotels/{hotelId}/habitaciones/disponibles")
    public ResponseEntity<ApiResponse<List<HabitacionResponse>>> buscarDisponibles(
            @PathVariable Long hotelId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout,
            @RequestParam(required = false) Integer huespedes) {
        
        List<HabitacionResponse> response;
        if (checkin != null && checkout != null) {
            response = habitacionService.buscarHabitacionesDisponibles(hotelId, checkin, checkout, huespedes);
        } else {
            response = habitacionService.getHabitacionesDisponibles(hotelId);
        }
        
        return ResponseEntity.ok(ApiResponse.success(response, "Habitaciones disponibles"));
    }

    /**
     * PUT /api/host/habitaciones/{id} - Actualizar habitación
     */
    @PutMapping("/host/habitaciones/{id}")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<ApiResponse<HabitacionResponse>> actualizarHabitacion(
            @PathVariable Long id,
            @Valid @RequestBody HabitacionRequest request) {
        Usuario usuario = authService.getCurrentUser();
        HabitacionResponse response = habitacionService.actualizarHabitacion(id, request, usuario.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Habitación actualizada"));
    }

    /**
     * PATCH /api/host/habitaciones/{id}/estado - Cambiar estado
     */
    @PatchMapping("/host/habitaciones/{id}/estado")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<ApiResponse<HabitacionResponse>> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado) {
        Usuario usuario = authService.getCurrentUser();
        HabitacionResponse response = habitacionService.cambiarEstado(id, estado, usuario.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Estado actualizado"));
    }

    /**
     * DELETE /api/host/habitaciones/{id} - Eliminar habitación
     */
    @DeleteMapping("/host/habitaciones/{id}")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> eliminarHabitacion(@PathVariable Long id) {
        Usuario usuario = authService.getCurrentUser();
        habitacionService.eliminarHabitacion(id, usuario.getId());
        return ResponseEntity.ok(ApiResponse.success("Eliminada", "Habitación eliminada"));
    }
}
