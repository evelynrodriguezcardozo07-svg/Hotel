package com.example.PROYECTO.FINAL_WEB.repository;

import com.example.PROYECTO.FINAL_WEB.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para Pago
 */
@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    /**
     * Buscar pagos por reserva
     */
    @Query("SELECT p FROM Pago p WHERE p.reserva.id = :reservaId ORDER BY p.creadoEn DESC")
    List<Pago> findByReservaId(@Param("reservaId") Long reservaId);

    /**
     * Buscar pagos pendientes
     */
    @Query("SELECT p FROM Pago p WHERE p.estado = 'pendiente' ORDER BY p.creadoEn ASC")
    List<Pago> findPagosPendientes();

    /**
     * Verificar si reserva tiene pago completado
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM Pago p " +
           "WHERE p.reserva.id = :reservaId " +
           "AND p.estado = 'completado'")
    boolean reservaTienePagoCompletado(@Param("reservaId") Long reservaId);
}
