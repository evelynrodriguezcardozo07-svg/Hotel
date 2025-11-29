package com.example.PROYECTO.FINAL_WEB.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO para procesar pagos con Culqi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoRequest {

    @NotNull(message = "El ID de reserva es obligatorio")
    private Long reservaId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @NotBlank(message = "La moneda es obligatoria")
    @Size(max = 10)
    @Builder.Default
    private String moneda = "PEN"; // PEN, USD

    @NotBlank(message = "El método de pago es obligatorio")
    private String metodo; // tarjeta, yape, plin, efectivo

    // Campos específicos de Culqi
    @NotBlank(message = "El token de Culqi es obligatorio")
    private String culqiToken; // Token generado por Culqi.js en el frontend

    @Email(message = "Email inválido")
    private String email;

    @Size(max = 500)
    private String descripcion;

    // Información de la tarjeta (para referencia, NO almacenar)
    private String ultimosDigitos; // Ej: "4242"
    private String marcaTarjeta; // visa, mastercard, etc.
}
