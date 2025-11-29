-- =============================================
-- FIX: Permitir reservas por horas (fechas iguales)
-- =============================================

USE HotelDemo;
GO

-- 1. Eliminar la constraint antigua que no permite fechas iguales
ALTER TABLE [Reserva] DROP CONSTRAINT [CK_Reserva_Fechas];
GO

-- 2. Agregar las columnas para reservas por horas (si no existen)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[Reserva]') AND name = 'reserva_por_horas')
BEGIN
    ALTER TABLE [Reserva] ADD reserva_por_horas BIT DEFAULT 0;
    PRINT 'Columna reserva_por_horas agregada';
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[Reserva]') AND name = 'hora_checkin')
BEGIN
    ALTER TABLE [Reserva] ADD hora_checkin TIME NULL;
    PRINT 'Columna hora_checkin agregada';
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[Reserva]') AND name = 'hora_checkout')
BEGIN
    ALTER TABLE [Reserva] ADD hora_checkout TIME NULL;
    PRINT 'Columna hora_checkout agregada';
END
GO

-- 3. Crear nueva constraint que diferencia entre reservas por días y por horas
ALTER TABLE [Reserva] ADD CONSTRAINT [CK_Reserva_Fechas_Flexibles] 
CHECK (
    (reserva_por_horas = 0 AND fecha_checkout > fecha_checkin) -- Reservas por días: checkout debe ser posterior
    OR
    (reserva_por_horas = 1 AND fecha_checkout >= fecha_checkin) -- Reservas por horas: puede ser mismo día
);
GO

PRINT '✅ Fix aplicado correctamente!';
PRINT '   - Constraint antigua eliminada';
PRINT '   - Columnas para reservas por horas agregadas (si faltaban)';
PRINT '   - Nueva constraint flexible creada';
GO
