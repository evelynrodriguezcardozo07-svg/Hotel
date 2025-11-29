package com.example.PROYECTO.FINAL_WEB.controller;

import com.example.PROYECTO.FINAL_WEB.dto.response.ApiResponse;
import com.example.PROYECTO.FINAL_WEB.dto.response.HotelResponse;
import com.example.PROYECTO.FINAL_WEB.service.HotelService;
import com.example.PROYECTO.FINAL_WEB.security.JwtTokenProvider;
import com.example.PROYECTO.FINAL_WEB.repository.UsuarioRepository;
import com.example.PROYECTO.FINAL_WEB.entity.Usuario;
import com.example.PROYECTO.FINAL_WEB.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador para operaciones de propietarios (hosts)
 */
@RestController
@RequestMapping("/api/host")
@PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HostController {

    @Autowired
    private HotelService hotelService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    /**
     * Método helper para obtener el ID del usuario desde el token JWT
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String jwt = jwtTokenProvider.getJwtFromRequest(request);
        if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
            String email = jwtTokenProvider.getUsernameFromJWT(jwt);
            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            return usuario.getId();
        }
        throw new RuntimeException("Token JWT inválido");
    }

    /**
     * Crear un nuevo hotel
     */
    @PostMapping("/hotel")
    public ResponseEntity<ApiResponse<HotelResponse>> crearHotel(
            @Valid @RequestBody Map<String, Object> hotelData,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        HotelResponse hotel = hotelService.crearHotelPorPropietario(hotelData, userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(hotel, "Hotel creado exitosamente. Pendiente de aprobación."));
    }

    /**
     * Obtener hoteles del propietario autenticado
     */
    @GetMapping("/mis-hoteles")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> obtenerMisHoteles(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<HotelResponse> hoteles = hotelService.obtenerHotelesPorPropietario(userId);
        return ResponseEntity.ok(ApiResponse.success(hoteles, "Hoteles obtenidos exitosamente"));
    }

    /**
     * Actualizar hotel (solo si es el propietario)
     */
    @PutMapping("/hotel/{id}")
    public ResponseEntity<ApiResponse<HotelResponse>> actualizarHotel(
            @PathVariable Long id,
            @Valid @RequestBody Map<String, Object> hotelData,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        HotelResponse hotel = hotelService.actualizarHotelPorPropietario(id, hotelData, userId);
        return ResponseEntity.ok(ApiResponse.success(hotel, "Hotel actualizado exitosamente"));
    }

    /**
     * Eliminar hotel (solo si es el propietario)
     */
    @DeleteMapping("/hotel/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarHotel(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        hotelService.eliminarHotelPorPropietario(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Hotel eliminado exitosamente"));
    }
}
