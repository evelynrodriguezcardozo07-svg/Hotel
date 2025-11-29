package com.example.PROYECTO.FINAL_WEB.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = \"cupon\")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, length = 50)
    private String codigo;

    @Column(length = 500)
    private String descripcion;

    @NotNull
    @Column(name = "tipo_descuento", length = 20)
    private String tipoDescuento; // porcentaje, monto_fijo

    @NotNull
    @DecimalMin("0")
    @Column(name = "valor_descuento", precision = 10, scale = 2)
    private BigDecimal valorDescuento;

    @Column(name = "monto_minimo", precision = 10, scale = 2)
    private BigDecimal montoMinimo;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "usos_maximos")
    private Integer usosMaximos;

    @Builder.Default
    @Column(name = "usos_actuales")
    private Integer usosActuales = 0;

    @Builder.Default
    @Column(name = "activo")
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel; // null = aplica a todos

    @CreationTimestamp
    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    public boolean isValid() {
        if (!activo) return false;
        
        LocalDate now = LocalDate.now();
        if (fechaInicio != null && now.isBefore(fechaInicio)) return false;
        if (fechaFin != null && now.isAfter(fechaFin)) return false;
        
        if (usosMaximos != null && usosActuales >= usosMaximos) return false;
        
        return true;
    }

    public BigDecimal calcularDescuento(BigDecimal montoOriginal) {
        if (!isValid()) return BigDecimal.ZERO;
        
        if (montoMinimo != null && montoOriginal.compareTo(montoMinimo) < 0) {
            return BigDecimal.ZERO;
        }

        if ("porcentaje".equals(tipoDescuento)) {
            return montoOriginal.multiply(valorDescuento).divide(new BigDecimal("100"));
        } else {
            return valorDescuento;
        }
    }

    public void incrementarUsos() {
        this.usosActuales++;
    }
}
