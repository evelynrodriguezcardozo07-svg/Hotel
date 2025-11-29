package com.example.PROYECTO.FINAL_WEB.repository;

import com.example.PROYECTO.FINAL_WEB.entity.Reserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para Reserva con queries JPQL
 */
@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    /**
     * Buscar reserva por código
     */
    @Query("SELECT r FROM Reserva r " +
           "LEFT JOIN FETCH r.usuario " +
           "LEFT JOIN FETCH r.habitacion h " +
           "LEFT JOIN FETCH h.hotel " +
           "WHERE r.codigoReserva = :codigo")
    Optional<Reserva> findByCodigoReserva(@Param("codigo") String codigo);

    /**
     * Buscar reserva por ID con todas las relaciones
     */
    @Query("SELECT r FROM Reserva r " +
           "LEFT JOIN FETCH r.usuario " +
           "LEFT JOIN FETCH r.habitacion h " +
           "LEFT JOIN FETCH h.hotel hot " +
           "LEFT JOIN FETCH hot.direccion " +
           "LEFT JOIN FETCH hot.propietario " +
           "WHERE r.id = :id")
    Optional<Reserva> findByIdWithDetails(@Param("id") Long id);

    /**
     * Buscar reservas por usuario
     */
    @Query("SELECT r FROM Reserva r " +
           "WHERE r.usuario.id = :usuarioId " +
           "ORDER BY r.creadoEn DESC")
    Page<Reserva> findByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    /**
     * Buscar reservas activas de un usuario
     */
    @Query("SELECT r FROM Reserva r " +
           "WHERE r.usuario.id = :usuarioId " +
           "AND r.estado IN ('confirmada', 'pendiente') " +
           "ORDER BY r.fechaCheckin ASC")
    List<Reserva> findReservasActivasByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Buscar reservas por hotel
     */
    @Query("SELECT r FROM Reserva r " +
           "JOIN r.habitacion h " +
           "WHERE h.hotel.id = :hotelId " +
           "ORDER BY r.creadoEn DESC")
    Page<Reserva> findByHotelId(@Param("hotelId") Long hotelId, Pageable pageable);

    /**
     * Buscar reservas por habitación y fechas (para verificar solapamientos)
     */
    @Query("SELECT r FROM Reserva r " +
           "WHERE r.habitacion.id = :habitacionId " +
           "AND r.estado IN ('confirmada', 'pendiente') " +
           "AND ((r.fechaCheckin < :checkout AND r.fechaCheckout > :checkin))")
    List<Reserva> findReservasSolapadas(
        @Param("habitacionId") Long habitacionId,
        @Param("checkin") LocalDate checkin,
        @Param("checkout") LocalDate checkout
    );

    /**
     * Buscar próximas reservas (check-in en los próximos días)
     */
    @Query("SELECT r FROM Reserva r " +
           "WHERE r.estado = 'confirmada' " +
           "AND r.fechaCheckin BETWEEN :desde AND :hasta " +
           "ORDER BY r.fechaCheckin ASC")
    List<Reserva> findProximasReservas(
        @Param("desde") LocalDate desde,
        @Param("hasta") LocalDate hasta
    );

    /**
     * Buscar reservas por estado
     */
    @Query("SELECT r FROM Reserva r WHERE r.estado = :estado ORDER BY r.creadoEn DESC")
    Page<Reserva> findByEstado(@Param("estado") String estado, Pageable pageable);

    /**
     * Actualizar estado de reserva
     */
    @Modifying
    @Query("UPDATE Reserva r SET r.estado = :estado, r.actualizadoEn = :fecha WHERE r.id = :id")
    void actualizarEstado(@Param("id") Long id, @Param("estado") String estado, @Param("fecha") LocalDateTime fecha);

    /**
     * Cancelar reserva
     */
    @Modifying
    @Query("UPDATE Reserva r SET r.estado = 'cancelada', r.fechaCancelacion = :fecha, r.motivoCancelacion = :motivo WHERE r.id = :id")
    void cancelarReserva(@Param("id") Long id, @Param("fecha") LocalDateTime fecha, @Param("motivo") String motivo);

    /**
     * Verificar si usuario tiene reserva completada en hotel (para reviews verificadas)
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM Reserva r " +
           "JOIN r.habitacion h " +
           "WHERE r.usuario.id = :usuarioId " +
           "AND h.hotel.id = :hotelId " +
           "AND r.estado = 'completada' " +
           "AND r.fechaCheckout < CURRENT_DATE")
    boolean hasCompletedStayAtHotel(@Param("usuarioId") Long usuarioId, @Param("hotelId") Long hotelId);

    /**
     * Contar reservas por estado
     */
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.estado = :estado")
    long countByEstado(@Param("estado") String estado);

    /**
     * Obtener estadísticas de reservas por mes
     */
    @Query("SELECT FUNCTION('MONTH', r.creadoEn), COUNT(r), SUM(r.total) " +
           "FROM Reserva r " +
           "WHERE FUNCTION('YEAR', r.creadoEn) = :year " +
           "AND r.estado IN ('confirmada', 'completada') " +
           "GROUP BY FUNCTION('MONTH', r.creadoEn) " +
           "ORDER BY FUNCTION('MONTH', r.creadoEn)")
    List<Object[]> getEstadisticasPorMes(@Param("year") int year);

    /**
     * Buscar reservas pendientes de pago
     */
    @Query("SELECT r FROM Reserva r " +
           "WHERE r.estado = 'pendiente' " +
           "AND r.creadoEn < :fechaLimite " +
           "ORDER BY r.creadoEn ASC")
    List<Reserva> findReservasPendientesVencidas(@Param("fechaLimite") LocalDateTime fechaLimite);

    /**
     * Verificar disponibilidad antes de reservar (query transaccional)
     */
    @Query("SELECT CASE WHEN COUNT(r) = 0 THEN true ELSE false END " +
           "FROM Reserva r " +
           "WHERE r.habitacion.id = :habitacionId " +
           "AND r.estado IN ('confirmada', 'pendiente') " +
           "AND ((r.fechaCheckin < :checkout AND r.fechaCheckout > :checkin))")
    boolean verificarDisponibilidad(
        @Param("habitacionId") Long habitacionId,
        @Param("checkin") LocalDate checkin,
        @Param("checkout") LocalDate checkout
    );
}
