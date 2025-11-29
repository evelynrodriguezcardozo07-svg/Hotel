package com.example.PROYECTO.FINAL_WEB.service;

import com.example.PROYECTO.FINAL_WEB.dto.response.UsuarioResponse;
import com.example.PROYECTO.FINAL_WEB.entity.Usuario;
import com.example.PROYECTO.FINAL_WEB.exception.ResourceNotFoundException;
import com.example.PROYECTO.FINAL_WEB.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de usuarios
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Obtener usuario por ID
     */
    @Transactional(readOnly = true)
    public UsuarioResponse getUsuarioById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        return mapToResponse(usuario);
    }

    /**
     * Obtener usuario por email
     */
    @Transactional(readOnly = true)
    public UsuarioResponse getUsuarioByEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));

        return mapToResponse(usuario);
    }

    /**
     * Obtener todos los usuarios por rol
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> getUsuariosByRol(String rol) {
        return usuarioRepository.findByRol(rol).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener propietarios con hoteles
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> getPropietariosConHoteles() {
        return usuarioRepository.findPropietariosConHoteles().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar perfil de usuario
     */
    @Transactional
    public UsuarioResponse actualizarPerfil(Long id, String nombre, String telefono) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (nombre != null && !nombre.isEmpty()) {
            usuario.setNombre(nombre);
        }
        if (telefono != null) {
            usuario.setTelefono(telefono);
        }

        usuario = usuarioRepository.save(usuario);
        return mapToResponse(usuario);
    }

    /**
     * Cambiar estado de usuario (solo admin)
     */
    @Transactional
    public UsuarioResponse cambiarEstado(Long id, String nuevoEstado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        usuario.setEstado(nuevoEstado);
        usuario = usuarioRepository.save(usuario);

        return mapToResponse(usuario);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario", "id", id);
        }
        usuarioRepository.softDelete(id, LocalDateTime.now());
    }

    /**
     * Obtener todos los usuarios (admin)
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> obtenerTodosUsuarios() {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getEliminadoEn() == null)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cambiar estado de usuario (admin)
     */
    @Transactional
    public UsuarioResponse cambiarEstadoUsuario(Long id, String estado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        usuario.setEstado(estado);
        usuario = usuarioRepository.save(usuario);
        return mapToResponse(usuario);
    }

    /**
     * Mapear entidad a DTO de respuesta
     */
    private UsuarioResponse mapToResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .rol(usuario.getRol())
                .estado(usuario.getEstado())
                .verificado(usuario.getVerificado())
                .fechaUltimoAcceso(usuario.getFechaUltimoAcceso())
                .creadoEn(usuario.getCreadoEn())
                .actualizadoEn(usuario.getActualizadoEn())
                .build();
    }
}
