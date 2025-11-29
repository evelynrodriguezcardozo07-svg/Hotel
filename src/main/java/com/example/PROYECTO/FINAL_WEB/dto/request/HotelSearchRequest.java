package com.example.PROYECTO.FINAL_WEB.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para búsqueda de hoteles con filtros
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelSearchRequest {

    private String ciudad;
    private String pais;
    private LocalDate fechaCheckin;
    private LocalDate fechaCheckout;
    private Integer cantidadHuespedes;
    private BigDecimal precioMinimo;
    private BigDecimal precioMaximo;
    private Integer estrellas;
    private BigDecimal puntuacionMinima;
    private Boolean destacado;
    private String[] amenidades;
    private Double latitud;
    private Double longitud;
    private Integer radioKm; // Radio de búsqueda en kilómetros
    
    // Paginación
    @Builder.Default
    private Integer page = 0;
    @Builder.Default
    private Integer size = 10;
    @Builder.Default
    private String sortBy = "puntuacionPromedio";
    @Builder.Default
    private String sortDirection = "DESC";
}
