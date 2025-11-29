package com.example.PROYECTO.FINAL_WEB.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para crear/actualizar Habitación
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabitacionRequest {

    @NotNull(message = "El ID del hotel es obligatorio")
    private Long hotelId;

    @NotBlank(message = "El número de habitación es obligatorio")
    @Size(max = 50, message = "El número no puede exceder 50 caracteres")
    private String numero;

    @NotNull(message = "El tipo de habitación es obligatorio")
    private Long roomTypeId;

    @Size(max = 100, message = "El nombre corto no puede exceder 100 caracteres")
    private String nombreCorto;

    @NotNull(message = "El precio base es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    private BigDecimal precioBase;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser al menos 1")
    @Max(value = 10, message = "La capacidad no puede exceder 10 personas")
    private Integer capacidad;

    @Min(value = 1, message = "Debe haber al menos 1 cama")
    private Integer numCamas;

    @DecimalMin(value = "0.0", message = "Los metros cuadrados no pueden ser negativos")
    private BigDecimal metrosCuadrados;

    private String estado; // disponible, mantenimiento, inactivo
    
    private String imagen; // Imagen principal en base64
    private java.util.List<String> imagenes; // Múltiples imágenes en base64
}
