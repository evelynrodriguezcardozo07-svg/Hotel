package com.example.PROYECTO.FINAL_WEB.repository;

import com.example.PROYECTO.FINAL_WEB.entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para Hotel con queries JPQL avanzadas
 */
@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    /**
     * Buscar hotel por ID incluyendo dirección y propietario (fetch join para evitar N+1)
     */
    @Query("SELECT h FROM Hotel h " +
           "LEFT JOIN FETCH h.direccion " +
           "LEFT JOIN FETCH h.propietario " +
           "WHERE h.id = :id AND h.eliminadoEn IS NULL")
    Optional<Hotel> findByIdWithDetails(@Param("id") Long id);

    /**
     * Buscar hoteles aprobados y activos
     */
    @Query("SELECT h FROM Hotel h WHERE h.estado = 'aprobado' AND h.eliminadoEn IS NULL")
    Page<Hotel> findHotelesAprobados(Pageable pageable);

    /**
     * Buscar hoteles destacados
     */
    @Query("SELECT h FROM Hotel h " +
           "LEFT JOIN FETCH h.direccion " +
           "WHERE h.destacado = true AND h.estado = 'aprobado' AND h.eliminadoEn IS NULL " +
           "ORDER BY h.puntuacionPromedio DESC")
    List<Hotel> findHotelesDestacados();

    /**
     * Buscar hoteles por ciudad (búsqueda compleja con filtros)
     */
    @Query("SELECT DISTINCT h FROM Hotel h " +
           "LEFT JOIN FETCH h.direccion d " +
           "WHERE (:ciudad IS NULL OR LOWER(d.ciudad) LIKE LOWER(CONCAT('%', :ciudad, '%'))) " +
           "AND (:pais IS NULL OR LOWER(d.pais) LIKE LOWER(CONCAT('%', :pais, '%'))) " +
           "AND (:estrellas IS NULL OR h.estrellas = :estrellas) " +
           "AND (:precioMin IS NULL OR h.precioMinimo >= :precioMin) " +
           "AND (:precioMax IS NULL OR h.precioMaximo <= :precioMax) " +
           "AND h.estado = 'aprobado' AND h.eliminadoEn IS NULL")
    Page<Hotel> buscarHotelesConFiltros(
        @Param("ciudad") String ciudad,
        @Param("pais") String pais,
        @Param("estrellas") Integer estrellas,
        @Param("precioMin") BigDecimal precioMin,
        @Param("precioMax") BigDecimal precioMax,
        Pageable pageable
    );

    /**
     * Buscar hoteles por propietario
     */
    @Query("SELECT h FROM Hotel h WHERE h.propietario.id = :propietarioId AND h.eliminadoEn IS NULL ORDER BY h.creadoEn DESC")
    List<Hotel> findByPropietarioId(@Param("propietarioId") Long propietarioId);

    /**
     * Buscar hoteles por propietario (con eliminados)
     */
    List<Hotel> findByPropietarioIdAndEliminadoEnIsNull(Long propietarioId);

    /**
     * Buscar hoteles por estado
     */
    List<Hotel> findByEstadoAndEliminadoEnIsNull(String estado);

    /**
     * Buscar hotel por ID (sin eliminados)
     */
    Optional<Hotel> findByIdAndEliminadoEnIsNull(Long id);

    /**
     * Buscar hoteles con amenidades específicas
     */
    @Query("SELECT DISTINCT h FROM Hotel h " +
           "JOIN h.hotelAmenities ha " +
           "JOIN ha.amenity a " +
           "WHERE a.nombre IN :amenidades " +
           "AND h.estado = 'aprobado' AND h.eliminadoEn IS NULL " +
           "GROUP BY h.id " +
           "HAVING COUNT(DISTINCT a.id) = :cantidadAmenidades")
    List<Hotel> findByAmenidades(
        @Param("amenidades") List<String> amenidades,
        @Param("cantidadAmenidades") long cantidadAmenidades
    );

    /**
     * Actualizar puntuación promedio del hotel
     */
    @Modifying
    @Query("UPDATE Hotel h SET h.puntuacionPromedio = :puntuacion, h.totalReviews = :total WHERE h.id = :id")
    void actualizarPuntuacion(@Param("id") Long id, @Param("puntuacion") BigDecimal puntuacion, @Param("total") Integer total);

    /**
     * Actualizar rango de precios del hotel
     */
    @Modifying
    @Query("UPDATE Hotel h SET h.precioMinimo = :min, h.precioMaximo = :max WHERE h.id = :id")
    void actualizarRangoPrecios(@Param("id") Long id, @Param("min") BigDecimal min, @Param("max") BigDecimal max);

    /**
     * Soft delete de hotel
     */
    @Modifying
    @Query("UPDATE Hotel h SET h.eliminadoEn = :fecha, h.estado = 'inactivo' WHERE h.id = :id")
    void softDelete(@Param("id") Long id, @Param("fecha") LocalDateTime fecha);

    /**
     * Verificar si existe hotel con ese nombre (activo)
     */
    @Query("SELECT COUNT(h) > 0 FROM Hotel h WHERE LOWER(h.nombre) = LOWER(:nombre) AND h.eliminadoEn IS NULL")
    boolean existsByNombreIgnoreCase(@Param("nombre") String nombre);

    /**
     * Contar hoteles por estado
     */
    @Query("SELECT COUNT(h) FROM Hotel h WHERE h.estado = :estado AND h.eliminadoEn IS NULL")
    long countByEstado(@Param("estado") String estado);

    /**
     * Obtener estadísticas de hoteles por ciudad
     */
    @Query("SELECT d.ciudad, COUNT(h) FROM Hotel h " +
           "JOIN h.direccion d " +
           "WHERE h.estado = 'aprobado' AND h.eliminadoEn IS NULL " +
           "GROUP BY d.ciudad " +
           "ORDER BY COUNT(h) DESC")
    List<Object[]> getEstadisticasPorCiudad();

    /**
     * Buscar hoteles disponibles en fechas específicas
     */
    @Query("SELECT DISTINCT h FROM Hotel h " +
           "JOIN h.habitaciones hab " +
           "WHERE hab.estado = 'disponible' " +
           "AND hab.capacidad >= :huespedes " +
           "AND h.estado = 'aprobado' " +
           "AND h.eliminadoEn IS NULL " +
           "AND hab.eliminadoEn IS NULL " +
           "AND NOT EXISTS (" +
           "  SELECT r FROM Reserva r " +
           "  WHERE r.habitacion.id = hab.id " +
           "  AND r.estado IN ('confirmada', 'pendiente') " +
           "  AND ((r.fechaCheckin <= :checkout AND r.fechaCheckout >= :checkin))" +
           ")")
    List<Hotel> findHotelesDisponibles(
        @Param("checkin") java.time.LocalDate checkin,
        @Param("checkout") java.time.LocalDate checkout,
        @Param("huespedes") Integer huespedes
    );
}
