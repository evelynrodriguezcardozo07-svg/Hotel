package com.example.PROYECTO.FINAL_WEB.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuesta de Usuario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {

    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private String rol;
    private String estado;
    private Boolean verificado;
    private LocalDateTime fechaUltimoAcceso;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
