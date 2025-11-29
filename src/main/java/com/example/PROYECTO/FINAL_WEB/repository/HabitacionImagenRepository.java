package com.example.PROYECTO.FINAL_WEB.repository;

import com.example.PROYECTO.FINAL_WEB.entity.HabitacionImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar imágenes de habitaciones
 */
@Repository
public interface HabitacionImagenRepository extends JpaRepository<HabitacionImagen, Long> {

    /**
     * Busca todas las imágenes de una habitación ordenadas por orden
     */
    List<HabitacionImagen> findByHabitacionIdOrderByOrdenAsc(Long habitacionId);

    /**
     * Busca la imagen principal de una habitación
     */
    @Query("SELECT hi FROM HabitacionImagen hi WHERE hi.habitacion.id = :habitacionId AND hi.esPrincipal = true")
    Optional<HabitacionImagen> findImagenPrincipalByHabitacionId(@Param("habitacionId") Long habitacionId);

    /**
     * Elimina todas las imágenes de una habitación
     */
    void deleteByHabitacionId(Long habitacionId);

    /**
     * Cuenta las imágenes de una habitación
     */
    long countByHabitacionId(Long habitacionId);
}
