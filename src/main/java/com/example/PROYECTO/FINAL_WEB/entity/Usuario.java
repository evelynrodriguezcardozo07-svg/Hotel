package com.example.PROYECTO.FINAL_WEB.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Usuario - Representa usuarios del sistema (guest, host, admin)
 * Incluye soft delete y auditoría de fechas
 */
@Entity
@Table(name = \"usuario\", indexes = {
    @Index(name = "IX_Usuario_Email", columnList = "email"),
    @Index(name = "IX_Usuario_Rol", columnList = "rol")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Size(max = 200, message = "El email no puede exceder 200 caracteres")
    @Column(name = "email", nullable = false, unique = true, length = 200)
    private String email;

    @Size(max = 30, message = "El teléfono no puede exceder 30 caracteres")
    @Column(name = "telefono", length = 30)
    private String telefono;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @NotBlank(message = "El rol es obligatorio")
    @Column(name = "rol", nullable = false, length = 20)
    private String rol; // guest, admin, host

    @NotBlank(message = "El estado es obligatorio")
    @Column(name = "estado", nullable = false, length = 20)
    private String estado; // activo, inactivo, bloqueado

    @Builder.Default
    @Column(name = "verificado")
    private Boolean verificado = false;

    @Column(name = "fecha_ultimo_acceso")
    private LocalDateTime fechaUltimoAcceso;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @Column(name = "eliminado_en")
    private LocalDateTime eliminadoEn; // Soft delete

    // Relaciones
    @OneToMany(mappedBy = "propietario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Hotel> hoteles = new HashSet<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Reserva> reservas = new HashSet<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Review> reviews = new HashSet<>();

    /**
     * Verifica si el usuario está activo y no eliminado
     */
    public boolean isActivo() {
        return "activo".equalsIgnoreCase(this.estado) && this.eliminadoEn == null;
    }

    /**
     * Verifica si el usuario es administrador
     */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(this.rol);
    }

    /**
     * Verifica si el usuario es propietario de hotel
     */
    public boolean isHost() {
        return "host".equalsIgnoreCase(this.rol);
    }

    /**
     * Verifica si el usuario es huésped/cliente
     */
    public boolean isGuest() {
        return "guest".equalsIgnoreCase(this.rol);
    }
}
