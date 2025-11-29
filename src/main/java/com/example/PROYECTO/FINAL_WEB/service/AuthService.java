package com.example.PROYECTO.FINAL_WEB.service;

import com.example.PROYECTO.FINAL_WEB.dto.request.LoginRequest;
import com.example.PROYECTO.FINAL_WEB.dto.request.RegisterRequest;
import com.example.PROYECTO.FINAL_WEB.dto.response.AuthResponse;
import com.example.PROYECTO.FINAL_WEB.entity.Usuario;
import com.example.PROYECTO.FINAL_WEB.exception.BusinessException;
import com.example.PROYECTO.FINAL_WEB.exception.ConflictException;
import com.example.PROYECTO.FINAL_WEB.exception.UnauthorizedException;
import com.example.PROYECTO.FINAL_WEB.repository.UsuarioRepository;
import com.example.PROYECTO.FINAL_WEB.security.JwtTokenProvider;
import com.example.PROYECTO.FINAL_WEB.util.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validar que las contraseñas coincidan
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Las contraseñas no coinciden");
        }

        // Verificar si el email ya existe
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("El email ya está registrado");
        }

        // Crear nuevo usuario
        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail().toLowerCase())
                .telefono(request.getTelefono())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(request.getRol() != null ? request.getRol() : AppConstants.ROLE_GUEST)
                .estado(AppConstants.USUARIO_ACTIVO)
                .verificado(false)
                .build();

        usuario = usuarioRepository.save(usuario);

        // Generar tokens
        String token = tokenProvider.generateToken(usuario.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken(usuario.getEmail());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tipo("Bearer")
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .verificado(usuario.getVerificado())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            // Autenticar con Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generar tokens
            String token = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(request.getEmail());

            // Actualizar último acceso
            Usuario usuario = usuarioRepository.findByEmailAndActivo(request.getEmail().toLowerCase())
                    .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

            usuarioRepository.actualizarUltimoAcceso(usuario.getId(), LocalDateTime.now());

            return AuthResponse.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .tipo("Bearer")
                    .id(usuario.getId())
                    .nombre(usuario.getNombre())
                    .email(usuario.getEmail())
                    .rol(usuario.getRol())
                    .verificado(usuario.getVerificado())
                    .build();

        } catch (Exception e) {
            throw new UnauthorizedException("Credenciales inválidas");
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Refresh token inválido o expirado");
        }

        String email = tokenProvider.getEmailFromToken(refreshToken);
        Usuario usuario = usuarioRepository.findByEmailAndActivo(email)
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));

        String newToken = tokenProvider.generateToken(email);
        String newRefreshToken = tokenProvider.generateRefreshToken(email);

        return AuthResponse.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .tipo("Bearer")
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .verificado(usuario.getVerificado())
                .build();
    }

    @Transactional(readOnly = true)
    public Usuario getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Usuario no autenticado");
        }

        String email = authentication.getName();
        return usuarioRepository.findByEmailAndActivo(email)
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));
    }
}
