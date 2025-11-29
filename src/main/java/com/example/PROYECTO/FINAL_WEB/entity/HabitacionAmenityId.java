package com.example.PROYECTO.FINAL_WEB.entity;

import lombok.*;

import java.io.Serializable;

/**
 * Clase de clave compuesta para HabitacionAmenity
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class HabitacionAmenityId implements Serializable {
    private Long habitacion;
    private Long amenity;
}
