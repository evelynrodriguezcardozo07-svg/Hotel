package com.example.PROYECTO.FINAL_WEB.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad Pago - Representa los pagos asociados a una reserva
 */
@Entity
@Table(name = \"pago\", indexes = {
    @Index(name = "IX_Pago_Reserva", columnList = "reserva_id"),
    @Index(name = "IX_Pago_Estado", columnList = "estado, fecha_pago")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.0", message = "El monto no puede ser negativo")
    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @NotBlank
    @Builder.Default
    @Column(name = "moneda", nullable = false, length = 10)
    private String moneda = "PEN"; // PEN, USD, EUR

    @Column(name = "metodo", length = 50)
    private String metodo; // tarjeta, paypal, transferencia, efectivo, yape, plin

    @NotBlank
    @Column(name = "estado", nullable = false, length = 30)
    private String estado; // pendiente, procesando, completado, fallido, reembolsado

    @Column(name = "transaccion_id", length = 200)
    private String transaccionId;

    @Column(name = "proveedor_pago", length = 100)
    private String proveedorPago; // Stripe, PayPal, Culqi, etc.

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    /**
     * Verifica si el pago fue completado
     */
    public boolean isCompletado() {
        return "completado".equalsIgnoreCase(this.estado);
    }

    /**
     * Verifica si el pago está pendiente
     */
    public boolean isPendiente() {
        return "pendiente".equalsIgnoreCase(this.estado) || 
               "procesando".equalsIgnoreCase(this.estado);
    }
}
