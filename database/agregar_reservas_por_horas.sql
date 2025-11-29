-- ============================================
-- ACTUALIZACIÓN: Agregar soporte para reservas por horas
-- ============================================

USE HotelDemo;
GO

-- Agregar columnas para reservas por horas a la tabla Reserva
ALTER TABLE [Reserva]
ADD hora_checkin TIME NULL,
    hora_checkout TIME NULL,
    reserva_por_horas BIT DEFAULT 0;
GO

-- Comentarios sobre las columnas
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Hora de check-in para reservas por horas (day use)', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE',  @level1name = N'Reserva',
    @level2type = N'COLUMN', @level2name = N'hora_checkin';
GO

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Hora de check-out para reservas por horas (day use)', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE',  @level1name = N'Reserva',
    @level2type = N'COLUMN', @level2name = N'hora_checkout';
GO

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Indica si es una reserva por horas (true) o por noche (false)', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE',  @level1name = N'Reserva',
    @level2type = N'COLUMN', @level2name = N'reserva_por_horas';
GO

-- Actualizar las reservas existentes como reservas por noche (false)
UPDATE [Reserva]
SET reserva_por_horas = 0
WHERE reserva_por_horas IS NULL;
GO

-- Verificar que las columnas se agregaron correctamente
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Reserva'
  AND COLUMN_NAME IN ('hora_checkin', 'hora_checkout', 'reserva_por_horas');
GO

PRINT 'Columnas agregadas exitosamente para soporte de reservas por horas';
GO
