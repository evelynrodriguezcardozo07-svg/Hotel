package com.example.PROYECTO.FINAL_WEB.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad HabitacionImagen - Almacena imágenes de habitaciones
 */
@Entity
@Table(name = \"habitacion_imagen\", indexes = {
    @Index(name = "IX_HabitacionImagen_Habitacion", columnList = "habitacion_id, orden")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitacionImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;

    @NotBlank
    @Column(name = "url", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String url;

    @Size(max = 300)
    @Column(name = "alt_text", length = 300)
    private String altText;

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
