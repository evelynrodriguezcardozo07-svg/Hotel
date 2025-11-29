-- ============================================
-- CONSULTAS DE DEBUG PARA RESERVAS
-- ============================================

-- 1. Ver todas las habitaciones con su estado actual
SELECT 
    h.id AS habitacion_id,
    h.numero,
    h.nombre_corto,
    h.estado,
    h.capacidad,
    h.precio_base,
    hot.nombre AS hotel_nombre,
    th.nombre AS tipo_habitacion
FROM [Habitacion] h
INNER JOIN [Hotel] hot ON h.hotel_id = hot.id
LEFT JOIN [TipoHabitacion] th ON h.room_type_id = th.id
WHERE h.eliminado_en IS NULL
ORDER BY hot.nombre, h.numero;

-- 2. Ver todas las reservas activas (no canceladas)
SELECT 
    r.id,
    r.codigo_reserva,
    r.estado,
    r.fecha_checkin,
    r.fecha_checkout,
    r.cantidad_huespedes,
    r.total,
    h.numero AS habitacion_numero,
    hot.nombre AS hotel_nombre,
    u.nombre AS cliente_nombre,
    u.email AS cliente_email,
    r.creado_en
FROM [Reserva] r
INNER JOIN [Habitacion] h ON r.habitacion_id = h.id
INNER JOIN [Hotel] hot ON h.hotel_id = hot.id
INNER JOIN [Usuario] u ON r.usuario_id = u.id
WHERE r.estado IN ('pendiente', 'confirmada')
ORDER BY r.fecha_checkin DESC;

-- 3. Ver reservas que solapan con fechas específicas
-- Ejemplo: Para verificar disponibilidad del 03/11/2025 al 05/11/2025
-- CAMBIA LAS FECHAS SEGÚN LO QUE ESTÉS PROBANDO
DECLARE @checkin DATE = '2025-11-03';
DECLARE @checkout DATE = '2025-11-05';

SELECT 
    r.id,
    r.codigo_reserva,
    r.estado,
    r.fecha_checkin,
    r.fecha_checkout,
    h.id AS habitacion_id,
    h.numero AS habitacion_numero,
    hot.nombre AS hotel_nombre,
    u.nombre AS cliente_nombre,
    CASE 
        WHEN r.fecha_checkin < @checkout AND r.fecha_checkout > @checkin 
        THEN 'SOLAPA CON LAS FECHAS'
        ELSE 'NO SOLAPA'
    END AS solapa
FROM [Reserva] r
INNER JOIN [Habitacion] h ON r.habitacion_id = h.id
INNER JOIN [Hotel] hot ON h.hotel_id = hot.id
INNER JOIN [Usuario] u ON r.usuario_id = u.id
WHERE r.estado IN ('pendiente', 'confirmada')
  AND r.fecha_checkin < @checkout 
  AND r.fecha_checkout > @checkin
ORDER BY h.numero, r.fecha_checkin;

-- 4. Ver habitaciones disponibles (sin reservas) para fechas específicas
DECLARE @checkin2 DATE = '2025-11-03';
DECLARE @checkout2 DATE = '2025-11-05';

SELECT 
    h.id AS habitacion_id,
    h.numero,
    h.nombre_corto,
    h.estado AS estado_habitacion,
    h.precio_base,
    hot.nombre AS hotel_nombre,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM [Reserva] r2
            WHERE r2.habitacion_id = h.id
              AND r2.estado IN ('pendiente', 'confirmada')
              AND r2.fecha_checkin < @checkout2
              AND r2.fecha_checkout > @checkin2
        ) THEN 'OCUPADA EN ESAS FECHAS'
        WHEN h.estado != 'disponible' THEN 'NO DISPONIBLE (estado: ' + h.estado + ')'
        ELSE 'DISPONIBLE'
    END AS disponibilidad
FROM [Habitacion] h
INNER JOIN [Hotel] hot ON h.hotel_id = hot.id
WHERE h.eliminado_en IS NULL
ORDER BY hot.nombre, h.numero;

-- 5. Contar reservas por estado
SELECT 
    estado,
    COUNT(*) AS cantidad
FROM [Reserva]
GROUP BY estado
ORDER BY cantidad DESC;

-- 6. Ver habitaciones por estado
SELECT 
    estado,
    COUNT(*) AS cantidad
FROM [Habitacion]
WHERE eliminado_en IS NULL
GROUP BY estado
ORDER BY cantidad DESC;

-- 7. Ver hoteles con sus estadísticas de habitaciones
SELECT 
    hot.id,
    hot.nombre AS hotel_nombre,
    hot.estado AS hotel_estado,
    COUNT(h.id) AS total_habitaciones,
    SUM(CASE WHEN h.estado = 'disponible' THEN 1 ELSE 0 END) AS disponibles,
    SUM(CASE WHEN h.estado = 'ocupada' THEN 1 ELSE 0 END) AS ocupadas,
    SUM(CASE WHEN h.estado = 'mantenimiento' THEN 1 ELSE 0 END) AS mantenimiento,
    SUM(CASE WHEN h.estado = 'inactivo' THEN 1 ELSE 0 END) AS inactivas
FROM [Hotel] hot
LEFT JOIN [Habitacion] h ON hot.id = h.hotel_id AND h.eliminado_en IS NULL
WHERE hot.eliminado_en IS NULL
GROUP BY hot.id, hot.nombre, hot.estado
ORDER BY hot.nombre;

-- 8. Ver reservas de un hotel específico (cambiar el ID según necesites)
DECLARE @hotelId INT = 1; -- CAMBIA ESTE ID

SELECT 
    r.id,
    r.codigo_reserva,
    r.estado,
    r.fecha_checkin,
    r.fecha_checkout,
    r.cantidad_huespedes,
    r.total,
    h.numero AS habitacion_numero,
    h.nombre_corto AS habitacion_nombre,
    u.nombre AS cliente_nombre,
    u.email AS cliente_email,
    r.creado_en
FROM [Reserva] r
INNER JOIN [Habitacion] h ON r.habitacion_id = h.id
INNER JOIN [Usuario] u ON r.usuario_id = u.id
WHERE h.hotel_id = @hotelId
ORDER BY r.creado_en DESC;

-- 9. Ver detalles de una habitación específica con sus reservas
DECLARE @habitacionId INT = 1; -- CAMBIA ESTE ID

SELECT 
    'DATOS DE LA HABITACION' AS tipo,
    CAST(h.id AS VARCHAR) AS id,
    h.numero AS numero,
    h.nombre_corto AS nombre,
    h.estado AS estado,
    CAST(h.capacidad AS VARCHAR) AS capacidad,
    CAST(h.precio_base AS VARCHAR) AS precio,
    hot.nombre AS hotel
FROM [Habitacion] h
INNER JOIN [Hotel] hot ON h.hotel_id = hot.id
WHERE h.id = @habitacionId

UNION ALL

SELECT 
    'RESERVAS' AS tipo,
    CAST(r.id AS VARCHAR) AS id,
    r.codigo_reserva AS numero,
    r.estado AS nombre,
    CONVERT(VARCHAR, r.fecha_checkin, 103) AS estado,
    CONVERT(VARCHAR, r.fecha_checkout, 103) AS capacidad,
    CAST(r.total AS VARCHAR) AS precio,
    u.nombre AS hotel
FROM [Reserva] r
INNER JOIN [Usuario] u ON r.usuario_id = u.id
WHERE r.habitacion_id = @habitacionId
  AND r.estado IN ('pendiente', 'confirmada')
ORDER BY tipo, id;

-- 10. Actualizar estado de habitaciones a 'disponible' (SOLO SI ES NECESARIO)
-- DESCOMENTA ESTO SOLO SI NECESITAS RESETEAR EL ESTADO
/*
UPDATE [Habitacion]
SET estado = 'disponible'
WHERE eliminado_en IS NULL;

SELECT 'Habitaciones actualizadas a disponible' AS mensaje;
*/

-- 11. Cancelar todas las reservas (SOLO PARA DEBUG - NO USAR EN PRODUCCIÓN)
-- DESCOMENTA ESTO SOLO SI NECESITAS LIMPIAR TODAS LAS RESERVAS
/*
UPDATE [Reserva]
SET estado = 'cancelada',
    motivo_cancelacion = 'Cancelada para pruebas',
    fecha_cancelacion = GETDATE()
WHERE estado IN ('pendiente', 'confirmada');

SELECT 'Todas las reservas canceladas' AS mensaje;
*/
