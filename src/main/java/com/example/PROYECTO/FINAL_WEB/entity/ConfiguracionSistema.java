package com.example.PROYECTO.FINAL_WEB.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad ConfiguracionSistema - Almacena configuraciones del sistema
 */
@Entity
@Table(name = "ConfiguracionSistema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "clave", nullable = false, unique = true, length = 100)
    private String clave;

    @NotBlank
    @Column(name = "valor", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String valor;

    @NotBlank
    @Size(max = 50)
    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo; // string, number, boolean, json

    @Size(max = 500)
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Size(max = 100)
    @Column(name = "categoria", length = 100)
    private String categoria;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}
