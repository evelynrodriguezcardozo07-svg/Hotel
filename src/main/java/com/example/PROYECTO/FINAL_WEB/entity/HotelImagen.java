package com.example.PROYECTO.FINAL_WEB.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad HotelImagen - Almacena imágenes del hotel
 */
@Entity
@Table(name = "hotel_imagen", indexes = {
    @Index(name = "idx_hotel_imagen_hotel", columnList = "hotel_id, orden")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @NotBlank
    @Column(name = "url", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String url;

    @Size(max = 300)
    @Column(name = "alt_text", length = 300)
    private String altText;

    @Size(max = 50)
    @Column(name = "tipo", length = 50)
    @Builder.Default
    private String tipo = "general"; // portada, general, lobby, exterior

    @Column(name = "orden")
    @Builder.Default
    private Integer orden = 0;

    @Column(name = "es_principal")
    @Builder.Default
    private Boolean esPrincipal = false;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}
