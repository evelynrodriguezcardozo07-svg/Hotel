package com.example.PROYECTO.FINAL_WEB.controller;

import com.example.PROYECTO.FINAL_WEB.dto.request.ReservaRequest;
import com.example.PROYECTO.FINAL_WEB.dto.response.ApiResponse;
import com.example.PROYECTO.FINAL_WEB.dto.response.PageResponse;
import com.example.PROYECTO.FINAL_WEB.dto.response.ReservaResponse;
import com.example.PROYECTO.FINAL_WEB.entity.Usuario;
import com.example.PROYECTO.FINAL_WEB.service.AuthService;
import com.example.PROYECTO.FINAL_WEB.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller de reservas
 */
@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private AuthService authService;

    /**
     * POST /api/reservas - Crear nueva reserva
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReservaResponse>> crearReserva(@Valid @RequestBody ReservaRequest request) {
        Usuario usuario = authService.getCurrentUser();
        ReservaResponse response = reservaService.crearReserva(request, usuario.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Reserva creada exitosamente"));
    }

    /**
     * GET /api/reservas/{id} - Obtener reserva por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReservaResponse>> getReserva(@PathVariable Long id) {
        ReservaResponse response = reservaService.getReservaById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Reserva obtenida"));
    }

    /**
     * GET /api/reservas/codigo/{codigo} - Buscar por código de reserva
     */
    @GetMapping("/codigo/{codigo}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReservaResponse>> getReservaPorCodigo(@PathVariable String codigo) {
        ReservaResponse response = reservaService.getReservaByCodigo(codigo);
        return ResponseEntity.ok(ApiResponse.success(response, "Reserva encontrada"));
    }

    /**
     * GET /api/reservas/mis-reservas - Obtener reservas del usuario actual
     */
    @GetMapping("/mis-reservas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<ReservaResponse>>> getMisReservas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Usuario usuario = authService.getCurrentUser();
        PageResponse<ReservaResponse> response = reservaService.getReservasByUsuario(usuario.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "Mis reservas"));
    }

    /**
     * GET /api/reservas/activas - Obtener reservas activas del usuario
     */
    @GetMapping("/activas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ReservaResponse>>> getReservasActivas() {
        Usuario usuario = authService.getCurrentUser();
        List<ReservaResponse> response = reservaService.getReservasActivas(usuario.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Reservas activas"));
    }

    /**
     * GET /api/reservas/hotel/{hotelId} - Obtener reservas de un hotel (propietario)
     */
    @GetMapping("/hotel/{hotelId}")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ReservaResponse>>> getReservasByHotel(
            @PathVariable Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<ReservaResponse> response = reservaService.getReservasByHotel(hotelId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "Reservas del hotel"));
    }

    /**
     * PATCH /api/reservas/{id}/confirmar - Confirmar reserva
     */
    @PatchMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<ApiResponse<ReservaResponse>> confirmarReserva(@PathVariable Long id) {
        ReservaResponse response = reservaService.confirmarReserva(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Reserva confirmada"));
    }

    /**
     * PATCH /api/reservas/{id}/cancelar - Cancelar reserva
     */
    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReservaResponse>> cancelarReserva(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo) {
        Usuario usuario = authService.getCurrentUser();
        ReservaResponse response = reservaService.cancelarReserva(id, motivo, usuario.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Reserva cancelada"));
    }

    /**
     * PATCH /api/reservas/{id}/completar - Completar reserva (después del checkout)
     */
    @PatchMapping("/{id}/completar")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<ApiResponse<ReservaResponse>> completarReserva(@PathVariable Long id) {
        ReservaResponse response = reservaService.completarReserva(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Reserva completada"));
    }
}
