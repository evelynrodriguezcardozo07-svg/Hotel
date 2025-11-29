package com.example.PROYECTO.FINAL_WEB.repository;

import com.example.PROYECTO.FINAL_WEB.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository para Review
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Buscar reviews por hotel
     */
    @Query("SELECT r FROM Review r " +
           "WHERE r.hotel.id = :hotelId " +
           "AND r.eliminadoEn IS NULL " +
           "ORDER BY r.creadoEn DESC")
    Page<Review> findByHotelId(@Param("hotelId") Long hotelId, Pageable pageable);

    /**
     * Buscar reviews verificadas de un hotel
     */
    @Query("SELECT r FROM Review r " +
           "WHERE r.hotel.id = :hotelId " +
           "AND r.verificado = true " +
           "AND r.eliminadoEn IS NULL " +
           "ORDER BY r.creadoEn DESC")
    List<Review> findReviewsVerificadasByHotelId(@Param("hotelId") Long hotelId);

    /**
     * Buscar reviews por usuario
     */
    @Query("SELECT r FROM Review r " +
           "WHERE r.usuario.id = :usuarioId " +
           "AND r.eliminadoEn IS NULL " +
           "ORDER BY r.creadoEn DESC")
    List<Review> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Verificar si usuario ya dejó review en hotel
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM Review r " +
           "WHERE r.usuario.id = :usuarioId " +
           "AND r.hotel.id = :hotelId " +
           "AND r.eliminadoEn IS NULL")
    boolean existsByUsuarioAndHotel(@Param("usuarioId") Long usuarioId, @Param("hotelId") Long hotelId);

    /**
     * Calcular puntuación promedio de un hotel
     */
    @Query("SELECT AVG(r.puntuacion) FROM Review r " +
           "WHERE r.hotel.id = :hotelId " +
           "AND r.eliminadoEn IS NULL")
    Double calcularPuntuacionPromedio(@Param("hotelId") Long hotelId);

    /**
     * Contar reviews de un hotel
     */
    @Query("SELECT COUNT(r) FROM Review r " +
           "WHERE r.hotel.id = :hotelId " +
           "AND r.eliminadoEn IS NULL")
    long countByHotelId(@Param("hotelId") Long hotelId);

    /**
     * Soft delete de review
     */
    @Modifying
    @Query("UPDATE Review r SET r.eliminadoEn = :fecha WHERE r.id = :id")
    void softDelete(@Param("id") Long id, @Param("fecha") LocalDateTime fecha);

    /**
     * Incrementar contador de utilidad
     */
    @Modifying
    @Query("UPDATE Review r SET r.utilCount = r.utilCount + 1 WHERE r.id = :id")
    void incrementarUtilCount(@Param("id") Long id);

    /**
     * Responder a review (propietario)
     */
    @Modifying
    @Query("UPDATE Review r SET r.respuestaHotel = :respuesta, r.fechaRespuesta = :fecha WHERE r.id = :id")
    void responderReview(@Param("id") Long id, @Param("respuesta") String respuesta, @Param("fecha") LocalDateTime fecha);
}
