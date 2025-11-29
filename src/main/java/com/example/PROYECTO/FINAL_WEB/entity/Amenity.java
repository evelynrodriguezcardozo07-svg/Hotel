package com.example.PROYECTO.FINAL_WEB.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Amenity - Representa las amenidades/servicios (WiFi, Piscina, etc.)
 */
@Entity
@Table(name = \"amenity\")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Amenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la amenidad es obligatorio")
    @Size(max = 120)
    @Column(name = "nombre", nullable = false, unique = true, length = 120)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "NVARCHAR(MAX)")
    private String descripcion;

    @Size(max = 100)
    @Column(name = "icono", length = 100)
    private String icono; // Para UI (wifi, pool, gym, etc.)

    @Size(max = 50)
    @Column(name = "categoria", length = 50)
    private String categoria; // basico, premium, entretenimiento, negocios

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    // Relaciones
    @OneToMany(mappedBy = "amenity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<HotelAmenity> hotelAmenities = new HashSet<>();

    @OneToMany(mappedBy = "amenity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<HabitacionAmenity> habitacionAmenities = new HashSet<>();
}
