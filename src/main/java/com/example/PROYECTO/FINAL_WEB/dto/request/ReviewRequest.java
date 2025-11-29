package com.example.PROYECTO.FINAL_WEB.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear Review
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

    @NotNull(message = "El ID del hotel es obligatorio")
    private Long hotelId;

    private Long reservaId; // Opcional - para verificar estadía

    @NotNull(message = "La puntuación es obligatoria")
    @Min(value = 1, message = "La puntuación mínima es 1")
    @Max(value = 5, message = "La puntuación máxima es 5")
    private Integer puntuacion;

    @Min(value = 1)
    @Max(value = 5)
    private Integer puntuacionLimpieza;

    @Min(value = 1)
    @Max(value = 5)
    private Integer puntuacionServicio;

    @Min(value = 1)
    @Max(value = 5)
    private Integer puntuacionUbicacion;

    @NotBlank(message = "El comentario es obligatorio")
    @Size(min = 10, max = 2000, message = "El comentario debe tener entre 10 y 2000 caracteres")
    private String comentario;
}
