package com.example.PROYECTO.FINAL_WEB.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respuesta de Hotel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private DireccionDTO direccion;
    private String telefono;
    private String emailContacto;
    private Integer estrellas;
    private BigDecimal precioMinimo;
    private BigDecimal precioMaximo;
    private String estado;
    private Boolean destacado;
    private BigDecimal puntuacionPromedio;
    private Integer totalReviews;
    private String imagenPrincipal;
    private List<String> imagenes;
    private List<AmenidadDTO> amenidades;
    private List<ReviewDTO> reviews;
    private PropietarioDTO propietario;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DireccionDTO {
        private Long id;
        private String calle;
        private String ciudad;
        private String estadoProvincia;
        private String pais;
        private String codigoPostal;
        private Double latitud;
        private Double longitud;
        private String direccionCompleta;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AmenidadDTO {
        private Long id;
        private String nombre;
        private String icono;
        private String categoria;
        private Boolean esGratuito;
        private String detalle;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewDTO {
        private Long id;
        private UsuarioSimpleDTO usuario;
        private Integer puntuacion;
        private String comentario;
        private String respuesta;
        private LocalDateTime creadoEn;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioSimpleDTO {
        private Long id;
        private String nombre;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropietarioDTO {
        private Long id;
        private String nombre;
        private String email;
    }
}
