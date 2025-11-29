package com.example.PROYECTO.FINAL_WEB.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuesta de autenticación (Login/Register)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String refreshToken;
    @Builder.Default
    private String tipo = "Bearer";
    private Long id;
    private String nombre;
    private String email;
    private String rol;
    private Boolean verificado;
}
