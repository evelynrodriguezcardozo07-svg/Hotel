package com.example.PROYECTO.FINAL_WEB.controller;

import com.example.PROYECTO.FINAL_WEB.dto.response.ApiResponse;
import com.example.PROYECTO.FINAL_WEB.dto.response.HotelResponse;
import com.example.PROYECTO.FINAL_WEB.dto.response.UsuarioResponse;
import com.example.PROYECTO.FINAL_WEB.service.HotelService;
import com.example.PROYECTO.FINAL_WEB.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para operaciones administrativas
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Obtener hoteles pendientes de aprobación
     */
    @GetMapping("/hoteles/pendientes")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> obtenerHotelesPendientes() {
        List<HotelResponse> hoteles = hotelService.obtenerHotelesPendientes();
        return ResponseEntity.ok(ApiResponse.success(hoteles, "Hoteles pendientes obtenidos"));
    }

    /**
     * Aprobar hotel
     */
    @PutMapping("/hoteles/{id}/aprobar")
    public ResponseEntity<ApiResponse<HotelResponse>> aprobarHotel(@PathVariable Long id) {
        HotelResponse hotel = hotelService.aprobarHotel(id);
        return ResponseEntity.ok(ApiResponse.success(hotel, "Hotel aprobado exitosamente"));
    }

    /**
     * Rechazar hotel
     */
    @PutMapping("/hoteles/{id}/rechazar")
    public ResponseEntity<ApiResponse<HotelResponse>> rechazarHotel(@PathVariable Long id) {
        HotelResponse hotel = hotelService.rechazarHotel(id);
        return ResponseEntity.ok(ApiResponse.success(hotel, "Hotel rechazado"));
    }

    /**
     * Obtener todos los usuarios
     */
    @GetMapping("/usuarios")
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> obtenerTodosUsuarios() {
        List<UsuarioResponse> usuarios = usuarioService.obtenerTodosUsuarios();
        return ResponseEntity.ok(ApiResponse.success(usuarios, "Usuarios obtenidos"));
    }

    /**
     * Cambiar estado de usuario
     */
    @PutMapping("/usuarios/{id}/estado")
    public ResponseEntity<ApiResponse<UsuarioResponse>> cambiarEstadoUsuario(
            @PathVariable Long id,
            @RequestParam String estado) {
        UsuarioResponse usuario = usuarioService.cambiarEstadoUsuario(id, estado);
        return ResponseEntity.ok(ApiResponse.success(usuario, "Estado de usuario actualizado"));
    }
}
