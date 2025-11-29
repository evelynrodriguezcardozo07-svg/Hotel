package com.example.PROYECTO.FINAL_WEB.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

/**
 * DTO para crear/actualizar Hotel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelRequest {

    @NotBlank(message = "El nombre del hotel es obligatorio")
    @Size(min = 5, max = 200, message = "El nombre debe tener entre 5 y 200 caracteres")
    private String nombre;

    @Size(max = 5000, message = "La descripción no puede exceder 5000 caracteres")
    private String descripcion;

    @Valid
    private DireccionRequest direccion;

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "El teléfono debe contener solo números")
    private String telefono;

    @Email(message = "El email de contacto debe ser válido")
    private String emailContacto;

    @Min(value = 1, message = "Las estrellas deben ser entre 1 y 5")
    @Max(value = 5, message = "Las estrellas deben ser entre 1 y 5")
    private Integer estrellas;

    private Boolean destacado;

    private String imagen; // Imagen en base64

    private Set<Long> amenidadesIds; // IDs de amenidades a asociar

    /**
     * DTO interno para dirección
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DireccionRequest {
        
        private String calle;

        @NotBlank(message = "La ciudad es obligatoria")
        private String ciudad;

        private String estadoProvincia;

        @NotBlank(message = "El país es obligatorio")
        private String pais;

        private String codigoPostal;

        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0")
        private Double latitud;

        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0")
        private Double longitud;
    }
}
