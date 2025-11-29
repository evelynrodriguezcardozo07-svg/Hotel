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
 * Entidad Hotel - Representa un hotel en el sistema
 * Incluye relación con propietario, dirección, habitaciones y amenidades
 */
@Entity
@Table(name = "Hotel", indexes = {
    @Index(name = "IX_Hotel_Propietario", columnList = "propietario_id"),
    @Index(name = "IX_Hotel_Estado", columnList = "estado"),
    @Index(name = "IX_Hotel_Precio", columnList = "precio_minimo, precio_maximo"),
    @Index(name = "IX_Hotel_Destacado", columnList = "destacado, puntuacion_promedio")
})
@NamedQueries({
    @NamedQuery(
        name = "Hotel.findByEstado",
        query = "SELECT h FROM Hotel h WHERE h.estado = :estado AND h.eliminadoEn IS NULL ORDER BY h.creadoEn DESC"
    ),
    @NamedQuery(
        name = "Hotel.findDestacados",
        query = "SELECT h FROM Hotel h WHERE h.destacado = true AND h.estado = 'aprobado' AND h.eliminadoEn IS NULL ORDER BY h.puntuacionPromedio DESC"
    ),
    @NamedQuery(
        name = "Hotel.findByEstrellas",
        query = "SELECT h FROM Hotel h WHERE h.estrellas = :estrellas AND h.estado = 'aprobado' AND h.eliminadoEn IS NULL"
    ),
    @NamedQuery(
        name = "Hotel.countByEstado",
        query = "SELECT COUNT(h) FROM Hotel h WHERE h.estado = :estado AND h.eliminadoEn IS NULL"
    )
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_id")
    private Usuario propietario;

    @NotBlank(message = "El nombre del hotel es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "NVARCHAR(MAX)")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "direccion_id")
    private Direccion direccion;

    @Size(max = 50)
    @Column(name = "telefono", length = 50)
    private String telefono;

    @Email
    @Size(max = 200)
    @Column(name = "email_contacto", length = 200)
    private String emailContacto;

    @Min(value = 1, message = "Las estrellas deben ser entre 1 y 5")
    @Max(value = 5, message = "Las estrellas deben ser entre 1 y 5")
    @Column(name = "estrellas")
    private Integer estrellas;

    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    @Column(name = "precio_minimo", precision = 12, scale = 2)
    private BigDecimal precioMinimo;

    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    @Column(name = "precio_maximo", precision = 12, scale = 2)
    private BigDecimal precioMaximo;

    @NotBlank(message = "El estado es obligatorio")
    @Column(name = "estado", nullable = false, length = 30)
    private String estado; // pendiente, aprobado, rechazado, inactivo

    @Builder.Default
    @Column(name = "destacado")
    private Boolean destacado = false;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    @Column(name = "puntuacion_promedio", precision = 3, scale = 2)
    private BigDecimal puntuacionPromedio;

    @Builder.Default
    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @Column(name = "eliminado_en")
    private LocalDateTime eliminadoEn; // Soft delete

    // Relaciones
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<Habitacion> habitaciones = new HashSet<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<HotelAmenity> hotelAmenities = new HashSet<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<Review> reviews = new HashSet<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<HotelImagen> imagenes = new HashSet<>();

    /**
     * Verifica si el hotel está aprobado y activo
     */
    public boolean isAprobado() {
        return "aprobado".equalsIgnoreCase(this.estado) && this.eliminadoEn == null;
    }

    /**
     * Verifica si el hotel está pendiente de aprobación
     */
    public boolean isPendiente() {
        return "pendiente".equalsIgnoreCase(this.estado);
    }

    /**
     * Calcula el rango de precios basado en las habitaciones
     */
    public void actualizarRangoPrecios() {
        if (habitaciones != null && !habitaciones.isEmpty()) {
            this.precioMinimo = habitaciones.stream()
                .map(Habitacion::getPrecioBase)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
            
            this.precioMaximo = habitaciones.stream()
                .map(Habitacion::getPrecioBase)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        }
    }

    /**
     * Recalcula la puntuación promedio desde las reviews
     */
    public void recalcularPuntuacion() {
        if (reviews != null && !reviews.isEmpty()) {
            long validReviews = reviews.stream()
                .filter(r -> r.getEliminadoEn() == null)
                .count();
            
            if (validReviews > 0) {
                double promedio = reviews.stream()
                    .filter(r -> r.getEliminadoEn() == null)
                    .mapToInt(Review::getPuntuacion)
                    .average()
                    .orElse(0.0);
                
                this.puntuacionPromedio = BigDecimal.valueOf(promedio);
                this.totalReviews = (int) validReviews;
            }
        }
    }
}
