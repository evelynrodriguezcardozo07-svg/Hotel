package com.example.PROYECTO.FINAL_WEB.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuesta de Review
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private UsuarioDTO usuario;
    private Long hotelId;
    private String hotelNombre;
    private Integer puntuacion;
    private Integer puntuacionLimpieza;
    private Integer puntuacionServicio;
    private Integer puntuacionUbicacion;
    private Double puntuacionPromedio;
    private String comentario;
    private String respuestaHotel;
    private LocalDateTime fechaRespuesta;
    private Boolean verificado;
    private Integer utilCount;
    private LocalDateTime creadoEn;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioDTO {
        private Long id;
        private String nombre;
    }
}
