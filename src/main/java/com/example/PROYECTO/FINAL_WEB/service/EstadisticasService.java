package com.example.PROYECTO.FINAL_WEB.service;

import com.example.PROYECTO.FINAL_WEB.entity.Hotel;
import com.example.PROYECTO.FINAL_WEB.entity.Reserva;
import com.example.PROYECTO.FINAL_WEB.entity.Review;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de Estadísticas usando EntityManager y @PersistenceContext
 * Demuestra el uso directo de JPA EntityManager para consultas complejas
 * sin afectar el flujo normal del proyecto que usa JpaRepository
 */
@Service
@Slf4j
public class EstadisticasService {

    /**
     * EntityManager inyectado directamente con @PersistenceContext
     * Permite acceso de bajo nivel a las operaciones de persistencia
     */
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Obtiene estadísticas generales usando JPQL con EntityManager
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasGenerales() {
        Map<String, Object> estadisticas = new HashMap<>();

        // Consulta JPQL usando EntityManager directamente
        Long totalHoteles = entityManager
            .createQuery("SELECT COUNT(h) FROM Hotel h WHERE h.eliminadoEn IS NULL", Long.class)
            .getSingleResult();

        Long totalReservas = entityManager
            .createQuery("SELECT COUNT(r) FROM Reserva r WHERE r.eliminadoEn IS NULL", Long.class)
            .getSingleResult();

        Long totalReviews = entityManager
            .createQuery("SELECT COUNT(rv) FROM Review rv WHERE rv.eliminadoEn IS NULL", Long.class)
            .getSingleResult();

        estadisticas.put("totalHoteles", totalHoteles);
        estadisticas.put("totalReservas", totalReservas);
        estadisticas.put("totalReviews", totalReviews);

        log.info("Estadísticas generales calculadas con EntityManager");
        return estadisticas;
    }

    /**
     * Obtiene hoteles usando Named Query (ejecutada desde EntityManager)
     */
    @Transactional(readOnly = true)
    public List<Hotel> obtenerHotelesPorEstadoNamedQuery(String estado) {
        // Ejecuta Named Query definida en la entidad Hotel
        TypedQuery<Hotel> query = entityManager.createNamedQuery("Hotel.findByEstado", Hotel.class);
        query.setParameter("estado", estado);
        return query.getResultList();
    }

    /**
     * Obtiene reviews usando Named Query con parámetros
     */
    @Transactional(readOnly = true)
    public List<Review> obtenerReviewsPorPuntuacionMinima(Integer puntuacionMinima) {
        // Ejecuta Named Query definida en la entidad Review
        TypedQuery<Review> query = entityManager.createNamedQuery("Review.findByPuntuacionMinima", Review.class);
        query.setParameter("puntuacionMinima", puntuacionMinima);
        return query.getResultList();
    }

    /**
     * Búsqueda con Criteria API usando EntityManager
     * Alternativa programática a JPQL
     */
    @Transactional(readOnly = true)
    public List<Hotel> buscarHotelesConCriteriaAPI(String ciudad, Integer estrellasMinimas) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Hotel> criteriaQuery = cb.createQuery(Hotel.class);
        Root<Hotel> hotel = criteriaQuery.from(Hotel.class);

        // Join con Direccion
        Join<Object, Object> direccion = hotel.join("direccion", JoinType.LEFT);

        // Construir predicados dinámicamente
        Predicate predicate = cb.conjunction();

        if (ciudad != null && !ciudad.isEmpty()) {
            predicate = cb.and(predicate, 
                cb.like(cb.lower(direccion.get("ciudad")), "%" + ciudad.toLowerCase() + "%"));
        }

        if (estrellasMinimas != null) {
            predicate = cb.and(predicate, 
                cb.greaterThanOrEqualTo(hotel.get("estrellas"), estrellasMinimas));
        }

        // Solo hoteles no eliminados
        predicate = cb.and(predicate, 
            cb.isNull(hotel.get("eliminadoEn")));

        criteriaQuery.where(predicate);
        criteriaQuery.orderBy(cb.desc(hotel.get("puntuacionPromedio")));

        TypedQuery<Hotel> query = entityManager.createQuery(criteriaQuery);
        query.setMaxResults(20); // Limitar resultados

        log.info("Búsqueda con Criteria API: ciudad={}, estrellas>={}", ciudad, estrellasMinimas);
        return query.getResultList();
    }

    /**
     * Actualización masiva usando EntityManager.createQuery()
     */
    @Transactional
    public int actualizarDestacadosPorPuntuacion(BigDecimal puntuacionMinima) {
        String jpql = "UPDATE Hotel h SET h.destacado = true " +
                     "WHERE h.puntuacionPromedio >= :puntuacion " +
                     "AND h.eliminadoEn IS NULL " +
                     "AND h.estado = 'aprobado'";
        
        int actualizados = entityManager.createQuery(jpql)
            .setParameter("puntuacion", puntuacionMinima)
            .executeUpdate();

        log.info("Hoteles actualizados como destacados: {}", actualizados);
        return actualizados;
    }

    /**
     * Consulta nativa SQL usando EntityManager
     * Para casos donde JPQL no es suficiente
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Object[]> obtenerReporteMensualReservas(int anio, int mes) {
        String sql = "SELECT " +
                    "CAST(fecha_checkin AS DATE) as fecha, " +
                    "COUNT(*) as total_reservas, " +
                    "SUM(precio_total) as ingresos_totales " +
                    "FROM Reserva " +
                    "WHERE YEAR(fecha_checkin) = :anio " +
                    "AND MONTH(fecha_checkin) = :mes " +
                    "AND eliminado_en IS NULL " +
                    "GROUP BY CAST(fecha_checkin AS DATE) " +
                    "ORDER BY fecha";

        return entityManager.createNativeQuery(sql)
            .setParameter("anio", anio)
            .setParameter("mes", mes)
            .getResultList();
    }

    /**
     * Flush manual del EntityManager
     * Útil para sincronizar cambios con la base de datos
     */
    @Transactional
    public void sincronizarCambios() {
        entityManager.flush();
        log.info("Cambios sincronizados con EntityManager.flush()");
    }

    /**
     * Clear del contexto de persistencia
     * Limpia la caché de primer nivel
     */
    @Transactional
    public void limpiarContextoPersistencia() {
        entityManager.clear();
        log.info("Contexto de persistencia limpiado con EntityManager.clear()");
    }

    /**
     * Obtiene una entidad usando find() de EntityManager
     */
    @Transactional(readOnly = true)
    public Hotel obtenerHotelPorId(Long id) {
        Hotel hotel = entityManager.find(Hotel.class, id);
        log.info("Hotel obtenido con EntityManager.find(): {}", id);
        return hotel;
    }

    /**
     * Merge de una entidad desconectada
     */
    @Transactional
    public Hotel actualizarHotelConMerge(Hotel hotel) {
        Hotel merged = entityManager.merge(hotel);
        log.info("Hotel actualizado con EntityManager.merge(): {}", hotel.getId());
        return merged;
    }

    /**
     * Persistir nueva entidad con EntityManager
     */
    @Transactional
    public void persistirConEntityManager(Object entity) {
        entityManager.persist(entity);
        log.info("Entidad persistida con EntityManager.persist()");
    }

    /**
     * Refresh de entidad desde la base de datos
     */
    @Transactional
    public void refrescarEntidad(Object entity) {
        entityManager.refresh(entity);
        log.info("Entidad refrescada desde la BD con EntityManager.refresh()");
    }

    /**
     * Detach de entidad del contexto de persistencia
     */
    @Transactional
    public void desconectarEntidad(Object entity) {
        entityManager.detach(entity);
        log.info("Entidad desconectada con EntityManager.detach()");
    }

    /**
     * Verifica si una entidad está en el contexto de persistencia
     */
    @Transactional(readOnly = true)
    public boolean contieneEntidad(Object entity) {
        boolean contains = entityManager.contains(entity);
        log.info("EntityManager.contains(): {}", contains);
        return contains;
    }

    /**
     * Ejemplo de consulta con múltiples parámetros usando EntityManager
     */
    @Transactional(readOnly = true)
    public List<Reserva> buscarReservasComplejas(
            LocalDate fechaInicio, 
            LocalDate fechaFin, 
            String estado) {
        
        String jpql = "SELECT r FROM Reserva r " +
                     "WHERE r.fechaCheckin BETWEEN :inicio AND :fin " +
                     "AND r.estado = :estado " +
                     "AND r.eliminadoEn IS NULL " +
                     "ORDER BY r.fechaCheckin DESC";

        TypedQuery<Reserva> query = entityManager.createQuery(jpql, Reserva.class);
        query.setParameter("inicio", fechaInicio.atStartOfDay());
        query.setParameter("fin", fechaFin.atTime(23, 59, 59));
        query.setParameter("estado", estado);
        query.setMaxResults(100);

        return query.getResultList();
    }
}
