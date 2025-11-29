package com.example.PROYECTO.FINAL_WEB.controller;

import com.example.PROYECTO.FINAL_WEB.dto.request.HotelRequest;
import com.example.PROYECTO.FINAL_WEB.dto.request.HotelSearchRequest;
import com.example.PROYECTO.FINAL_WEB.dto.response.ApiResponse;
import com.example.PROYECTO.FINAL_WEB.dto.response.HotelResponse;
import com.example.PROYECTO.FINAL_WEB.dto.response.PageResponse;
import com.example.PROYECTO.FINAL_WEB.entity.Usuario;
import com.example.PROYECTO.FINAL_WEB.service.AuthService;
import com.example.PROYECTO.FINAL_WEB.service.HotelService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller de hoteles
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private AuthService authService;

    /**
     * POST /api/host/hoteles - Crear hotel (solo host/admin)
     */
    @PostMapping("/host/hoteles")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<ApiResponse<HotelResponse>> crearHotel(@Valid @RequestBody HotelRequest request) {
        Usuario usuario = authService.getCurrentUser();
        HotelResponse response = hotelService.crearHotel(request, usuario.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Hotel creado exitosamente"));
    }

    /**
     * GET /api/hotels - Buscar hoteles públicamente con filtros
     */
    @GetMapping("/hotels")
    public ResponseEntity<ApiResponse<PageResponse<HotelResponse>>> buscarHoteles(
            @ModelAttribute HotelSearchRequest request) {
        PageResponse<HotelResponse> response = hotelService.buscarHoteles(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Hoteles encontrados"));
    }

    /**
     * GET /api/hotels/{id} - Obtener hotel por ID (público)
     */
    @GetMapping("/hotels/{id}")
    public ResponseEntity<ApiResponse<HotelResponse>> getHotel(@PathVariable Long id) {
        HotelResponse response = hotelService.getHotelById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Hotel obtenido"));
    }

    /**
     * GET /api/hotels/destacados - Obtener hoteles destacados
     */
    @GetMapping("/hotels/destacados")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> getHotelesDestacados() {
        List<HotelResponse> response = hotelService.getHotelesDestacados();
        return ResponseEntity.ok(ApiResponse.success(response, "Hoteles destacados"));
    }

    /**
     * GET /api/host/hoteles - Obtener hoteles del propietario actual
     */
    @GetMapping("/host/hoteles")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> getMisHoteles() {
        Usuario usuario = authService.getCurrentUser();
        List<HotelResponse> response = hotelService.getHotelesByPropietario(usuario.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Mis hoteles"));
    }

    /**
     * PUT /api/host/hoteles/{id} - Actualizar hotel
     */
    @PutMapping("/host/hoteles/{id}")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<ApiResponse<HotelResponse>> actualizarHotel(
            @PathVariable Long id,
            @Valid @RequestBody HotelRequest request) {
        Usuario usuario = authService.getCurrentUser();
        HotelResponse response = hotelService.actualizarHotel(id, request, usuario.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Hotel actualizado"));
    }

    /**
     * PATCH /api/admin/hoteles/{id}/aprobar - Aprobar hotel
     */
    @PatchMapping("/admin/hoteles/{id}/aprobar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<HotelResponse>> aprobarHotel(@PathVariable Long id) {
        HotelResponse response = hotelService.aprobarHotel(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Hotel aprobado"));
    }

    /**
     * PATCH /api/admin/hoteles/{id}/rechazar - Rechazar hotel
     */
    @PatchMapping("/admin/hoteles/{id}/rechazar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<HotelResponse>> rechazarHotel(@PathVariable Long id) {
        HotelResponse response = hotelService.rechazarHotel(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Hotel rechazado"));
    }

    /**
     * DELETE /api/host/hoteles/{id} - Eliminar hotel
     */
    @DeleteMapping("/host/hoteles/{id}")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> eliminarHotel(@PathVariable Long id) {
        hotelService.eliminarHotel(id);
        return ResponseEntity.ok(ApiResponse.success("Eliminado", "Hotel eliminado correctamente"));
    }
}
