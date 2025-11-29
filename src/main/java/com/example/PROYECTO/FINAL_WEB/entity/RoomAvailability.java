package com.example.PROYECTO.FINAL_WEB.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad RoomAvailability - Controla la disponibilidad de habitaciones por fecha
 */
@Entity
@Table(name = "RoomAvailability",
    uniqueConstraints = @UniqueConstraint(
        name = "UX_RoomAvailability_Habitacion_Fecha",
        columnNames = {"habitacion_id", "fecha"}
    ),
    indexes = {
        @Index(name = "IX_RoomAvailability_Fecha_Estado", columnList = "fecha, estado")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotBlank
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private String estado = "disponible"; // disponible, bloqueado, reservado, mantenimiento

    @DecimalMin(value = "0.0")
    @Column(name = "precio_dia", precision = 12, scale = 2)
    private BigDecimal precioDia;

    @Column(name = "nota", length = 500)
    private String nota;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    /**
     * Verifica si está disponible para reserva
     */
    public boolean isDisponible() {
        return "disponible".equalsIgnoreCase(this.estado);
    }
}
