package com.example.PROYECTO.FINAL_WEB.repository;

import com.example.PROYECTO.FINAL_WEB.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para Usuario con queries JPQL personalizadas
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Buscar usuario por email (ignorando mayúsculas/minúsculas)
     */
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.email) = LOWER(:email) AND u.eliminadoEn IS NULL")
    Optional<Usuario> findByEmailIgnoreCase(@Param("email") String email);

    /**
     * Buscar usuario por email y que esté activo
     */
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.email) = LOWER(:email) AND u.estado = 'activo' AND u.eliminadoEn IS NULL")
    Optional<Usuario> findByEmailAndActivo(@Param("email") String email);

    /**
     * Verificar si existe un email (para registro)
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Usuario u WHERE LOWER(u.email) = LOWER(:email) AND u.eliminadoEn IS NULL")
    boolean existsByEmail(@Param("email") String email);

    /**
     * Buscar usuarios por rol
     */
    @Query("SELECT u FROM Usuario u WHERE u.rol = :rol AND u.eliminadoEn IS NULL ORDER BY u.creadoEn DESC")
    List<Usuario> findByRol(@Param("rol") String rol);

    /**
     * Buscar usuarios activos por rol
     */
    @Query("SELECT u FROM Usuario u WHERE u.rol = :rol AND u.estado = 'activo' AND u.eliminadoEn IS NULL")
    List<Usuario> findActivosByRol(@Param("rol") String rol);

    /**
     * Actualizar último acceso del usuario
     */
    @Modifying
    @Query("UPDATE Usuario u SET u.fechaUltimoAcceso = :fecha WHERE u.id = :id")
    void actualizarUltimoAcceso(@Param("id") Long id, @Param("fecha") LocalDateTime fecha);

    /**
     * Soft delete de usuario
     */
    @Modifying
    @Query("UPDATE Usuario u SET u.eliminadoEn = :fecha, u.estado = 'inactivo' WHERE u.id = :id")
    void softDelete(@Param("id") Long id, @Param("fecha") LocalDateTime fecha);

    /**
     * Contar usuarios por rol y estado
     */
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol = :rol AND u.estado = :estado AND u.eliminadoEn IS NULL")
    long countByRolAndEstado(@Param("rol") String rol, @Param("estado") String estado);

    /**
     * Buscar propietarios (hosts) con hoteles
     */
    @Query("SELECT DISTINCT u FROM Usuario u JOIN u.hoteles h WHERE u.rol = 'host' AND u.eliminadoEn IS NULL AND h.eliminadoEn IS NULL")
    List<Usuario> findPropietariosConHoteles();
}
