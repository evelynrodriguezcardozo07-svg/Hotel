package com.example.PROYECTO.FINAL_WEB.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respuesta de Habitación
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabitacionResponse {

    private Long id;
    private String numero;
    private String nombreCorto;
    private TipoHabitacionDTO tipoHabitacion;
    private BigDecimal precioBase;
    private Integer capacidad;
    private Integer numCamas;
    private BigDecimal metrosCuadrados;
    private String estado;
    private String imagenPrincipal;
    private List<String> imagenes;
    private List<AmenidadDTO> amenidades;
    private LocalDateTime creadoEn;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TipoHabitacionDTO {
        private Long id;
        private String nombre;
        private String descripcion;
        private String icono;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AmenidadDTO {
        private Long id;
        private String nombre;
        private String icono;
    }
}
