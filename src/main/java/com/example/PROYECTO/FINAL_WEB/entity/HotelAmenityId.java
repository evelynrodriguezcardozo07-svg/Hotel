package com.example.PROYECTO.FINAL_WEB.entity;

import lombok.*;

import java.io.Serializable;

/**
 * Clase de clave compuesta para HotelAmenity
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class HotelAmenityId implements Serializable {
    private Long hotel;
    private Long amenity;
}
