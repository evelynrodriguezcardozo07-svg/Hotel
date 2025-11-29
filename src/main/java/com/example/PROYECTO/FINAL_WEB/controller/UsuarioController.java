package com.example.PROYECTO.FINAL_WEB.controller;

import com.example.PROYECTO.FINAL_WEB.dto.response.ApiResponse;
import com.example.PROYECTO.FINAL_WEB.dto.response.UsuarioResponse;
import com.example.PROYECTO.FINAL_WEB.entity.Usuario;
import com.example.PROYECTO.FINAL_WEB.service.AuthService;
import com.example.PROYECTO.FINAL_WEB.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller de gestión de usuarios
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AuthService authService;

    /**
     * GET /api/usuario/perfil - Obtener perfil del usuario autenticado
     */
    @GetMapping("/usuario/perfil")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UsuarioResponse>> getPerfil() {
        Usuario usuario = authService.getCurrentUser();
        UsuarioResponse response = usuarioService.getUsuarioById(usuario.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Perfil obtenido"));
    }

    /**
     * PUT /api/usuario/perfil - Actualizar perfil
     */
    @PutMapping("/usuario/perfil")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizarPerfil(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String telefono) {
        Usuario usuario = authService.getCurrentUser();
        UsuarioResponse response = usuarioService.actualizarPerfil(usuario.getId(), nombre, telefono);
        return ResponseEntity.ok(ApiResponse.success(response, "Perfil actualizado"));
    }

    /*
     * Endpoints de administración movidos a AdminController
     * para mejor organización del código
     */

    /**
     * PATCH /api/admin/usuarios/{id}/estado - Cambiar estado de usuario
     */
    @PatchMapping("/admin/usuarios/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado) {
        UsuarioResponse response = usuarioService.cambiarEstado(id, estado);
        return ResponseEntity.ok(ApiResponse.success(response, "Estado actualizado"));
    }

    /**
     * DELETE /api/admin/usuarios/{id} - Eliminar usuario (soft delete)
     */
    @DeleteMapping("/admin/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.ok(ApiResponse.success("Usuario eliminado", "Usuario eliminado correctamente"));
    }
}
