package com.example.PROYECTO.FINAL_WEB.repository;

import com.example.PROYECTO.FINAL_WEB.entity.TipoHabitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para TipoHabitacion
 */
@Repository
public interface TipoHabitacionRepository extends JpaRepository<TipoHabitacion, Long> {

    /**
     * Buscar tipo por nombre
     */
    @Query("SELECT t FROM TipoHabitacion t WHERE LOWER(t.nombre) = LOWER(:nombre)")
    Optional<TipoHabitacion> findByNombreIgnoreCase(@Param("nombre") String nombre);

    /**
     * Listar todos ordenados por nombre
     */
    @Query("SELECT t FROM TipoHabitacion t ORDER BY t.nombre ASC")
    List<TipoHabitacion> findAllOrdenados();
}
