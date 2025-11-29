package com.example.PROYECTO.FINAL_WEB.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Habitacion - Representa una habitación de un hotel
 */
@Entity
@Table(name = "Habitacion", 
    uniqueConstraints = @UniqueConstraint(
        name = "UX_Habitacion_Hotel_Numero", 
        columnNames = {"hotel_id", "numero"}
    ),
    indexes = {
        @Index(name = "IX_Habitacion_Precio", columnList = "precio_base, capacidad")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @NotBlank(message = "El número de habitación es obligatorio")
    @Size(max = 50)
    @Column(name = "numero", nullable = false, length = 50)
    private String numero;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_type_id", nullable = false)
    private TipoHabitacion roomType;

    @Size(max = 100)
    @Column(name = "nombre_corto", length = 100)
    private String nombreCorto;

    @NotNull(message = "El precio base es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    @Column(name = "precio_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioBase;

    @Min(value = 1, message = "La capacidad debe ser al menos 1")
    @Builder.Default
    @Column(name = "capacidad", nullable = false)
    private Integer capacidad = 1;

    @Min(value = 1)
    @Builder.Default
    @Column(name = "num_camas")
    private Integer numCamas = 1;

    @DecimalMin(value = "0.0")
    @Column(name = "metros_cuadrados", precision = 6, scale = 2)
    private BigDecimal metrosCuadrados;

    @NotBlank(message = "El estado es obligatorio")
    @Column(name = "estado", nullable = false, length = 30)
    private String estado; // disponible, mantenimiento, inactivo

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @Column(name = "eliminado_en")
    private LocalDateTime eliminadoEn; // Soft delete

    // Relaciones
    @OneToMany(mappedBy = "habitacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<Reserva> reservas = new HashSet<>();

    @OneToMany(mappedBy = "habitacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<HabitacionImagen> imagenes = new HashSet<>();

    @OneToMany(mappedBy = "habitacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<HabitacionAmenity> habitacionAmenities = new HashSet<>();

    @OneToMany(mappedBy = "habitacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<RoomAvailability> disponibilidad = new HashSet<>();

    @OneToMany(mappedBy = "habitacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<TarifaEspecial> tarifasEspeciales = new HashSet<>();

    /**
     * Verifica si la habitación está disponible
     */
    public boolean isDisponible() {
        return "disponible".equalsIgnoreCase(this.estado) && this.eliminadoEn == null;
    }

    /**
     * Retorna el nombre completo de la habitación
     */
    public String getNombreCompleto() {
        if (nombreCorto != null && !nombreCorto.isEmpty()) {
            return nombreCorto + " - " + numero;
        }
        return roomType.getNombre() + " - " + numero;
    }
}
