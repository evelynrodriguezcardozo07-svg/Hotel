package com.example.PROYECTO.FINAL_WEB.repository;

import com.example.PROYECTO.FINAL_WEB.entity.Habitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para Habitacion con queries JPQL
 */
@Repository
public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

    /**
     * Buscar habitación por ID con relaciones necesarias
     */
    @Query("SELECT h FROM Habitacion h " +
           "LEFT JOIN FETCH h.hotel hot " +
           "LEFT JOIN FETCH h.roomType " +
           "WHERE h.id = :id AND h.eliminadoEn IS NULL")
    Optional<Habitacion> findByIdWithDetails(@Param("id") Long id);

    /**
     * Buscar habitaciones por hotel
     */
    @Query("SELECT h FROM Habitacion h " +
           "WHERE h.hotel.id = :hotelId AND h.eliminadoEn IS NULL " +
           "ORDER BY h.numero ASC")
    List<Habitacion> findByHotelId(@Param("hotelId") Long hotelId);

    /**
     * Buscar habitaciones disponibles de un hotel
     */
    @Query("SELECT h FROM Habitacion h " +
           "WHERE h.hotel.id = :hotelId " +
           "AND h.estado = 'disponible' " +
           "AND h.eliminadoEn IS NULL " +
           "ORDER BY h.precioBase ASC")
    List<Habitacion> findDisponiblesByHotelId(@Param("hotelId") Long hotelId);

    /**
     * Buscar habitaciones disponibles para fechas y capacidad específicas
     */
    @Query("SELECT h FROM Habitacion h " +
           "WHERE h.hotel.id = :hotelId " +
           "AND h.capacidad >= :capacidad " +
           "AND h.estado = 'disponible' " +
           "AND h.eliminadoEn IS NULL " +
           "AND NOT EXISTS (" +
           "  SELECT r FROM Reserva r " +
           "  WHERE r.habitacion.id = h.id " +
           "  AND r.estado IN ('confirmada', 'pendiente') " +
           "  AND ((r.fechaCheckin < :checkout AND r.fechaCheckout > :checkin))" +
           ") " +
           "ORDER BY h.precioBase ASC")
    List<Habitacion> findDisponiblesConFiltros(
        @Param("hotelId") Long hotelId,
        @Param("checkin") LocalDate checkin,
        @Param("checkout") LocalDate checkout,
        @Param("capacidad") Integer capacidad
    );

    /**
     * Verificar disponibilidad de habitación específica
     */
    @Query("SELECT CASE WHEN COUNT(r) = 0 THEN true ELSE false END " +
           "FROM Reserva r " +
           "WHERE r.habitacion.id = :habitacionId " +
           "AND r.estado IN ('confirmada', 'pendiente') " +
           "AND ((r.fechaCheckin < :checkout AND r.fechaCheckout > :checkin))")
    boolean isHabitacionDisponible(
        @Param("habitacionId") Long habitacionId,
        @Param("checkin") LocalDate checkin,
        @Param("checkout") LocalDate checkout
    );

    /**
     * Obtener lista de precios de habitaciones disponibles de un hotel
     */
    @Query("SELECT h.precioBase FROM Habitacion h " +
           "WHERE h.hotel.id = :hotelId " +
           "AND h.estado = 'disponible' " +
           "AND h.eliminadoEn IS NULL")
    List<BigDecimal> findPreciosByHotelId(@Param("hotelId") Long hotelId);

    /**
     * Buscar habitación por hotel y número
     */
    @Query("SELECT h FROM Habitacion h " +
           "WHERE h.hotel.id = :hotelId " +
           "AND h.numero = :numero " +
           "AND h.eliminadoEn IS NULL")
    Optional<Habitacion> findByHotelIdAndNumero(@Param("hotelId") Long hotelId, @Param("numero") String numero);

    /**
     * Verificar si existe habitación con ese número en el hotel
     */
    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END " +
           "FROM Habitacion h " +
           "WHERE h.hotel.id = :hotelId " +
           "AND h.numero = :numero " +
           "AND h.eliminadoEn IS NULL " +
           "AND (:excludeId IS NULL OR h.id <> :excludeId)")
    boolean existsByHotelIdAndNumero(
        @Param("hotelId") Long hotelId,
        @Param("numero") String numero,
        @Param("excludeId") Long excludeId
    );

    /**
     * Soft delete de habitación
     */
    @Modifying
    @Query("UPDATE Habitacion h SET h.eliminadoEn = :fecha, h.estado = 'inactivo' WHERE h.id = :id")
    void softDelete(@Param("id") Long id, @Param("fecha") LocalDateTime fecha);

    /**
     * Contar habitaciones disponibles por hotel
     */
    @Query("SELECT COUNT(h) FROM Habitacion h " +
           "WHERE h.hotel.id = :hotelId " +
           "AND h.estado = 'disponible' " +
           "AND h.eliminadoEn IS NULL")
    long countDisponiblesByHotelId(@Param("hotelId") Long hotelId);

    /**
     * Buscar habitaciones por rango de precio
     */
    @Query("SELECT h FROM Habitacion h " +
           "WHERE h.hotel.id = :hotelId " +
           "AND h.precioBase BETWEEN :min AND :max " +
           "AND h.eliminadoEn IS NULL " +
           "ORDER BY h.precioBase ASC")
    List<Habitacion> findByPrecioRange(
        @Param("hotelId") Long hotelId,
        @Param("min") BigDecimal min,
        @Param("max") BigDecimal max
    );
}
