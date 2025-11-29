package com.example.PROYECTO.FINAL_WEB.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad HabitacionAmenity - Relación Many-to-Many entre Habitacion y Amenity
 */
@Entity
@Table(name = \"habitacion_amenity\")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(HabitacionAmenityId.class)
public class HabitacionAmenity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "amenity_id", nullable = false)
    private Amenity amenity;

    @Column(name = "detalle", length = 200)
    private String detalle;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;
}
