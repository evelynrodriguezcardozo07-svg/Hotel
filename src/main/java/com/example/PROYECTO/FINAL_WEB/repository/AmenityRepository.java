package com.example.PROYECTO.FINAL_WEB.repository;

import com.example.PROYECTO.FINAL_WEB.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para Amenity
 */
@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {

    /**
     * Buscar amenidad por nombre
     */
    @Query("SELECT a FROM Amenity a WHERE LOWER(a.nombre) = LOWER(:nombre)")
    Optional<Amenity> findByNombreIgnoreCase(@Param("nombre") String nombre);

    /**
     * Buscar amenidades por categoría
     */
    @Query("SELECT a FROM Amenity a WHERE a.categoria = :categoria ORDER BY a.nombre ASC")
    List<Amenity> findByCategoria(@Param("categoria") String categoria);

    /**
     * Buscar amenidades más populares (más usadas en hoteles)
     */
    @Query("SELECT a, COUNT(ha) as usage FROM Amenity a " +
           "JOIN a.hotelAmenities ha " +
           "GROUP BY a " +
           "ORDER BY usage DESC")
    List<Amenity> findMasPopulares();

    /**
     * Buscar todas las amenidades de un hotel
     */
    @Query("SELECT a FROM Amenity a " +
           "JOIN a.hotelAmenities ha " +
           "WHERE ha.hotel.id = :hotelId " +
           "ORDER BY a.nombre ASC")
    List<Amenity> findByHotelId(@Param("hotelId") Long hotelId);
}
