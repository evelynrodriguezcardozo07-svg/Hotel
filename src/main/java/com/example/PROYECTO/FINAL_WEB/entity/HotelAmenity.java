package com.example.PROYECTO.FINAL_WEB.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad HotelAmenity - Relación Many-to-Many entre Hotel y Amenity
 */
@Entity
@Table(name = "HotelAmenity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(HotelAmenityId.class)
public class HotelAmenity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "amenity_id", nullable = false)
    private Amenity amenity;

    @Column(name = "detalle", length = 200)
    private String detalle; // "24h", "Gratuito", etc.

    @Builder.Default
    @Column(name = "es_gratuito")
    private Boolean esGratuito = true;

    @DecimalMin(value = "0.0")
    @Column(name = "precio_adicional", precision = 10, scale = 2)
    private BigDecimal precioAdicional;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}
