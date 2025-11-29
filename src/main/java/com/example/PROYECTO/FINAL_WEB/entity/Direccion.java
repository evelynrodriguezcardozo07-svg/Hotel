package com.example.PROYECTO.FINAL_WEB.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad Direccion - Almacena información de ubicación de hoteles
 */
@Entity
@Table(name = \"direccion\", indexes = {
    @Index(name = "IX_Direccion_Ciudad", columnList = "ciudad, pais")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 250)
    @Column(name = "calle", length = 250)
    private String calle;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 100)
    @Column(name = "ciudad", nullable = false, length = 100)
    private String ciudad;

    @Size(max = 100)
    @Column(name = "estado_provincia", length = 100)
    private String estadoProvincia;

    @NotBlank(message = "El país es obligatorio")
    @Size(max = 100)
    @Column(name = "pais", nullable = false, length = 100)
    private String pais;

    @Size(max = 20)
    @Column(name = "codigo_postal", length = 20)
    private String codigoPostal;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    /**
     * Retorna la dirección completa formateada
     */
    public String getDireccionCompleta() {
        StringBuilder sb = new StringBuilder();
        if (calle != null && !calle.isEmpty()) {
            sb.append(calle).append(", ");
        }
        sb.append(ciudad);
        if (estadoProvincia != null && !estadoProvincia.isEmpty()) {
            sb.append(", ").append(estadoProvincia);
        }
        sb.append(", ").append(pais);
        if (codigoPostal != null && !codigoPostal.isEmpty()) {
            sb.append(" ").append(codigoPostal);
        }
        return sb.toString();
    }

    /**
     * Verifica si tiene coordenadas GPS
     */
    public boolean tieneCoordenadasGPS() {
        return latitud != null && longitud != null;
    }
}
