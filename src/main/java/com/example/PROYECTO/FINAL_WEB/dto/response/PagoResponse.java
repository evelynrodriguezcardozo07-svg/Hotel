package com.example.PROYECTO.FINAL_WEB.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para pagos procesados
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoResponse {

    private Long id;
    private Long reservaId;
    private String codigoReserva;
    private BigDecimal monto;
    private String moneda;
    private String metodo;
    private String estado; // pendiente, procesando, completado, fallido, reembolsado
    private String transaccionId; // ID de Culqi
    private String proveedorPago; // "Culqi"
    private LocalDateTime fechaPago;
    private String mensaje;
    private String urlRecibo; // URL para descargar recibo
    
    // Información adicional
    private String ultimosDigitos;
    private String marcaTarjeta;
}
