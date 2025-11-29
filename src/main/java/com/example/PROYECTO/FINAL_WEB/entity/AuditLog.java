package com.example.PROYECTO.FINAL_WEB.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad AuditLog - Registro de auditoría para el sistema
 */
@Entity
@Table(name = \"audit_log\", indexes = {
    @Index(name = "IX_AuditLog_Tabla_Registro", columnList = "tabla, registro_id, creado_en")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @NotBlank
    @Size(max = 100)
    @Column(name = "tabla", nullable = false, length = 100)
    private String tabla;

    @Column(name = "registro_id", nullable = false)
    private Long registroId;

    @NotBlank
    @Size(max = 50)
    @Column(name = "accion", nullable = false, length = 50)
    private String accion; // INSERT, UPDATE, DELETE

    @Column(name = "valores_antiguos", columnDefinition = "NVARCHAR(MAX)")
    private String valoresAntiguos;

    @Column(name = "valores_nuevos", columnDefinition = "NVARCHAR(MAX)")
    private String valoresNuevos;

    @Size(max = 50)
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Size(max = 500)
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;
}
