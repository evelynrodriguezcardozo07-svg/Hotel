package com.example.PROYECTO.FINAL_WEB.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para respuesta de Reserva
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaResponse {

    private Long id;
    private String codigoReserva;
    private UsuarioDTO usuario;
    private HabitacionDTO habitacion;
    private HotelDTO hotel;
    private LocalDate fechaCheckin;
    private LocalDate fechaCheckout;
    
    // NUEVO: Soporte para reservas por horas
    private java.time.LocalTime horaCheckin;
    private java.time.LocalTime horaCheckout;
    private Boolean reservaPorHoras; // true = day use, false = por noche
    private Long numeroHoras; // Para reservas por horas
    
    private Integer cantidadHuespedes;
    private Long numeroNoches; // Para reservas por noche
    private String estado;
    private BigDecimal subtotal;
    private BigDecimal impuestos;
    private BigDecimal total;
    private String notasEspeciales;
    
    // Datos del huésped principal
    private String nombreHuesped;
    private String apellidoHuesped;
    private String dniHuesped;
    private String telefonoHuesped;
    
    private Boolean puedeCancelarse;
    private LocalDateTime fechaCancelacion;
    private String motivoCancelacion;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioDTO {
        private Long id;
        private String nombre;
        private String email;
        private String telefono;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HabitacionDTO {
        private Long id;
        private String numero;
        private String nombreCompleto;
        private String imagenPrincipal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotelDTO {
        private Long id;
        private String nombre;
        private String direccion;
        private String telefono;
        private String imagenPrincipal;
    }
}
