package com.example.PROYECTO.FINAL_WEB.repository;

import com.example.PROYECTO.FINAL_WEB.entity.HotelImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar imágenes de hoteles
 */
@Repository
public interface HotelImagenRepository extends JpaRepository<HotelImagen, Long> {

    /**
     * Busca todas las imágenes de un hotel ordenadas por orden
     */
    List<HotelImagen> findByHotelIdOrderByOrdenAsc(Long hotelId);

    /**
     * Busca la imagen principal de un hotel
     */
    @Query("SELECT hi FROM HotelImagen hi WHERE hi.hotel.id = :hotelId AND hi.esPrincipal = true")
    Optional<HotelImagen> findImagenPrincipalByHotelId(@Param("hotelId") Long hotelId);

    /**
     * Elimina todas las imágenes de un hotel
     */
    void deleteByHotelId(Long hotelId);

    /**
     * Cuenta las imágenes de un hotel
     */
    long countByHotelId(Long hotelId);
}
