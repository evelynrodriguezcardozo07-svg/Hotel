package com.example.PROYECTO.FINAL_WEB.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO para crear Reserva
 * Soporta: Reservas por noche (estándar) y reservas por horas (day use)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaRequest {

    @NotNull(message = "El ID de la habitación es obligatorio")
    private Long habitacionId;

    @NotNull(message = "La fecha de check-in es obligatoria")
    @FutureOrPresent(message = "La fecha de check-in debe ser hoy o futura")
    private LocalDate fechaCheckin;

    @NotNull(message = "La fecha de check-out es obligatoria")
    @FutureOrPresent(message = "La fecha de check-out debe ser hoy o futura")
    private LocalDate fechaCheckout;

    // NUEVO: Soporte para reservas por horas
    private LocalTime horaCheckin;  // Opcional: para reservas por horas (ej: 10:00)
    
    private LocalTime horaCheckout; // Opcional: para reservas por horas (ej: 18:00)
    
    @Builder.Default
    private Boolean reservaPorHoras = false; // true = day use, false = por noche (default)

    @NotNull(message = "La cantidad de huéspedes es obligatoria")
    @Min(value = 1, message = "Debe haber al menos 1 huésped")
    private Integer cantidadHuespedes;

    @Size(max = 1000, message = "Las notas no pueden exceder 1000 caracteres")
    private String notasEspeciales;
    
    @Size(max = 50, message = "El código de cupón no puede exceder 50 caracteres")
    private String codigoCupon;
    
    // Datos del huésped principal
    @NotBlank(message = "El nombre del huésped es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo debe contener letras")
    private String nombreHuesped;
    
    @NotBlank(message = "El apellido del huésped es obligatorio")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido solo debe contener letras")
    private String apellidoHuesped;
    
    @NotBlank(message = "El DNI del huésped es obligatorio")
    @Size(min = 8, max = 20, message = "El DNI debe tener entre 8 y 20 caracteres")
    @Pattern(regexp = "^[0-9A-Za-z]+$", message = "El DNI solo debe contener números y letras")
    private String dniHuesped;
    
    @NotBlank(message = "El teléfono del huésped es obligatorio")
    @Size(min = 9, max = 20, message = "El teléfono debe tener entre 9 y 20 caracteres")
    @Pattern(regexp = "^[+]?[0-9\\s()-]+$", message = "El teléfono debe contener solo números, espacios, paréntesis o guiones")
    private String telefonoHuesped;
}
