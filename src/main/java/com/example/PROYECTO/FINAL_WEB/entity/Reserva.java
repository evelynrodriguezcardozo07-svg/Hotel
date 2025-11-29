package com.example.PROYECTO.FINAL_WEB.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Reserva - Representa una reserva de habitación
 */
@Entity
@Table(name = "Reserva", indexes = {
    @Index(name = "IX_Reserva_Habitacion_Fechas", columnList = "habitacion_id, fecha_checkin, fecha_checkout"),
    @Index(name = "IX_Reserva_Usuario", columnList = "usuario_id, estado"),
    @Index(name = "IX_Reserva_Codigo", columnList = "codigo_reserva"),
    @Index(name = "IX_Reserva_Estado_Fechas", columnList = "estado, fecha_checkin")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código de reserva es obligatorio")
    @Size(max = 20)
    @Column(name = "codigo_reserva", nullable = false, unique = true, length = 20)
    private String codigoReserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;

    @NotNull(message = "La fecha de check-in es obligatoria")
    @Column(name = "fecha_checkin", nullable = false)
    private LocalDate fechaCheckin;

    @NotNull(message = "La fecha de check-out es obligatoria")
    @Column(name = "fecha_checkout", nullable = false)
    private LocalDate fechaCheckout;

    // NUEVO: Soporte para reservas por horas (Day Use)
    @Column(name = "hora_checkin")
    private java.time.LocalTime horaCheckin; // Ej: 10:00
    
    @Column(name = "hora_checkout")
    private java.time.LocalTime horaCheckout; // Ej: 18:00
    
    @Builder.Default
    @Column(name = "reserva_por_horas")
    private Boolean reservaPorHoras = false; // true = day use, false = por noche

    @Min(value = 1, message = "Debe haber al menos 1 huésped")
    @Builder.Default
    @Column(name = "cantidad_huespedes", nullable = false)
    private Integer cantidadHuespedes = 1;

    @NotBlank(message = "El estado es obligatorio")
    @Column(name = "estado", nullable = false, length = 30)
    private String estado; // pendiente, confirmada, cancelada, completada, no_show

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @DecimalMin(value = "0.0")
    @Column(name = "impuestos", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal impuestos = BigDecimal.ZERO;

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "notas_especiales", columnDefinition = "NVARCHAR(MAX)")
    private String notasEspeciales;

    // Datos del huésped principal
    @NotBlank(message = "El nombre del huésped es obligatorio")
    @Size(min = 2, max = 100)
    @Column(name = "nombre_huesped", nullable = false, length = 100)
    private String nombreHuesped;
    
    @NotBlank(message = "El apellido del huésped es obligatorio")
    @Size(min = 2, max = 100)
    @Column(name = "apellido_huesped", nullable = false, length = 100)
    private String apellidoHuesped;
    
    @NotBlank(message = "El DNI del huésped es obligatorio")
    @Size(min = 8, max = 20)
    @Column(name = "dni_huesped", nullable = false, length = 20)
    private String dniHuesped;
    
    @NotBlank(message = "El teléfono del huésped es obligatorio")
    @Size(min = 9, max = 20)
    @Column(name = "telefono_huesped", nullable = false, length = 20)
    private String telefonoHuesped;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @Column(name = "motivo_cancelacion", length = 500)
    private String motivoCancelacion;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    // Relaciones
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Pago> pagos = new HashSet<>();

    @OneToOne(mappedBy = "reserva", fetch = FetchType.LAZY)
    private Review review;

    /**
     * Valida que las fechas sean correctas
     */
    @PrePersist
    @PreUpdate
    private void validarFechas() {
        // Para reservas por horas, las fechas pueden ser iguales (mismo día)
        if (Boolean.TRUE.equals(reservaPorHoras)) {
            // Validar que las horas sean coherentes
            if (horaCheckout != null && horaCheckin != null && 
                !horaCheckout.isAfter(horaCheckin)) {
                throw new IllegalStateException("La hora de check-out debe ser posterior al check-in");
            }
            // Para reservas por horas, las fechas deben ser iguales (mismo día)
            if (fechaCheckout != null && fechaCheckin != null && 
                !fechaCheckout.equals(fechaCheckin)) {
                throw new IllegalStateException("Las reservas por horas deben ser en el mismo día");
            }
        } else {
            // Para reservas por días, la fecha de checkout debe ser posterior
            if (fechaCheckout != null && fechaCheckin != null && 
                !fechaCheckout.isAfter(fechaCheckin)) {
                throw new IllegalStateException("La fecha de check-out debe ser posterior al check-in");
            }
        }
    }

    /**
     * Calcula el número de noches de la reserva
     */
    public long calcularNumeroNoches() {
        if (Boolean.TRUE.equals(reservaPorHoras)) {
            return 0; // Las reservas por horas no cuentan noches
        }
        return java.time.temporal.ChronoUnit.DAYS.between(fechaCheckin, fechaCheckout);
    }

    /**
     * Calcula el número de horas de la reserva (para day use)
     */
    public long calcularNumeroHoras() {
        if (Boolean.TRUE.equals(reservaPorHoras) && horaCheckin != null && horaCheckout != null) {
            return java.time.Duration.between(horaCheckin, horaCheckout).toHours();
        }
        return 0;
    }

    /**
     * Verifica si es una reserva por horas (day use)
     */
    public boolean isReservaPorHoras() {
        return Boolean.TRUE.equals(reservaPorHoras);
    }

    /**
     * Verifica si la reserva está activa
     */
    public boolean isActiva() {
        return "confirmada".equalsIgnoreCase(this.estado) || 
               "pendiente".equalsIgnoreCase(this.estado);
    }

    /**
     * Verifica si la reserva puede ser cancelada
     */
    public boolean puedeCancelarse() {
        return isActiva() && LocalDate.now().isBefore(fechaCheckin);
    }
}
