package com.example.PROYECTO.FINAL_WEB.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad Review - Representa reseñas/opiniones de usuarios sobre hoteles
 */
@Entity
@Table(name = "Review", indexes = {
    @Index(name = "IX_Review_Hotel", columnList = "hotel_id, creado_en"),
    @Index(name = "IX_Review_Usuario", columnList = "usuario_id")
})
@NamedQueries({
    @NamedQuery(
        name = "Review.findByPuntuacionMinima",
        query = "SELECT r FROM Review r WHERE r.puntuacion >= :puntuacionMinima AND r.eliminadoEn IS NULL ORDER BY r.puntuacion DESC, r.creadoEn DESC"
    ),
    @NamedQuery(
        name = "Review.findVerificadas",
        query = "SELECT r FROM Review r WHERE r.verificado = true AND r.eliminadoEn IS NULL ORDER BY r.creadoEn DESC"
    ),
    @NamedQuery(
        name = "Review.findByHotelAndUsuario",
        query = "SELECT r FROM Review r WHERE r.hotel.id = :hotelId AND r.usuario.id = :usuarioId AND r.eliminadoEn IS NULL"
    ),
    @NamedQuery(
        name = "Review.countByHotel",
        query = "SELECT COUNT(r) FROM Review r WHERE r.hotel.id = :hotelId AND r.eliminadoEn IS NULL"
    ),
    @NamedQuery(
        name = "Review.avgPuntuacionByHotel",
        query = "SELECT AVG(r.puntuacion) FROM Review r WHERE r.hotel.id = :hotelId AND r.verificado = true AND r.eliminadoEn IS NULL"
    )
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    @NotNull(message = "La puntuación es obligatoria")
    @Min(value = 1, message = "La puntuación mínima es 1")
    @Max(value = 5, message = "La puntuación máxima es 5")
    @Column(name = "puntuacion", nullable = false)
    private Integer puntuacion;

    @Min(value = 1)
    @Max(value = 5)
    @Column(name = "puntuacion_limpieza")
    private Integer puntuacionLimpieza;

    @Min(value = 1)
    @Max(value = 5)
    @Column(name = "puntuacion_servicio")
    private Integer puntuacionServicio;

    @Min(value = 1)
    @Max(value = 5)
    @Column(name = "puntuacion_ubicacion")
    private Integer puntuacionUbicacion;

    @Column(name = "comentario", columnDefinition = "NVARCHAR(MAX)")
    private String comentario;

    @Column(name = "respuesta_hotel", columnDefinition = "NVARCHAR(MAX)")
    private String respuestaHotel;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @Builder.Default
    @Column(name = "verificado")
    private Boolean verificado = false;

    @Builder.Default
    @Column(name = "util_count")
    private Integer utilCount = 0;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @Column(name = "eliminado_en")
    private LocalDateTime eliminadoEn; // Soft delete

    /**
     * Calcula la puntuación promedio de los detalles
     */
    public Double calcularPuntuacionPromedio() {
        int count = 1; // puntuacion general siempre existe
        double total = puntuacion;

        if (puntuacionLimpieza != null) {
            total += puntuacionLimpieza;
            count++;
        }
        if (puntuacionServicio != null) {
            total += puntuacionServicio;
            count++;
        }
        if (puntuacionUbicacion != null) {
            total += puntuacionUbicacion;
            count++;
        }

        return total / count;
    }

    /**
     * Verifica si el hotel ha respondido
     */
    public boolean tieneRespuesta() {
        return respuestaHotel != null && !respuestaHotel.isEmpty();
    }
}
