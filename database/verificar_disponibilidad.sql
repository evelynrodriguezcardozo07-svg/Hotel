-- ============================================
-- VERIFICAR DISPONIBILIDAD PARA FECHAS ESPECÍFICAS
-- ============================================

-- Ver si hay reservas que bloquean las fechas 03/11 al 05/11/2025
SELECT 
    r.id,
    r.codigo_reserva,
    r.estado,
    r.fecha_checkin,
    r.fecha_checkout,
    h.id AS habitacion_id,
    h.numero AS habitacion_numero,
    h.nombre_corto,
    hot.nombre AS hotel_nombre,
    u.nombre AS cliente_nombre,
    'SOLAPA CON 03/11-05/11' AS mensaje
FROM [Reserva] r
INNER JOIN [Habitacion] h ON r.habitacion_id = h.id
INNER JOIN [Hotel] hot ON h.hotel_id = hot.id
INNER JOIN [Usuario] u ON r.usuario_id = u.id
WHERE r.estado IN ('pendiente', 'confirmada')
  AND r.fecha_checkin < '2025-11-05'
  AND r.fecha_checkout > '2025-11-03'
ORDER BY h.numero, r.fecha_checkin;

-- Si NO sale nada, significa que NO HAY RESERVAS bloqueando esas fechas
-- Si sale algo, esas son las reservas que están causando el conflicto
