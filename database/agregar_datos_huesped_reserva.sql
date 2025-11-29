-- Script para agregar campos de datos del huésped a la tabla Reserva
-- Ejecutar en SQL Server Management Studio

USE HotelReservas;
GO

-- Verificar si las columnas ya existen antes de agregarlas
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Reserva') AND name = 'nombre_huesped')
BEGIN
    ALTER TABLE Reserva ADD nombre_huesped NVARCHAR(100) NULL;
    PRINT 'Columna nombre_huesped agregada exitosamente';
END
ELSE
BEGIN
    PRINT 'La columna nombre_huesped ya existe';
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Reserva') AND name = 'apellido_huesped')
BEGIN
    ALTER TABLE Reserva ADD apellido_huesped NVARCHAR(100) NULL;
    PRINT 'Columna apellido_huesped agregada exitosamente';
END
ELSE
BEGIN
    PRINT 'La columna apellido_huesped ya existe';
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Reserva') AND name = 'dni_huesped')
BEGIN
    ALTER TABLE Reserva ADD dni_huesped NVARCHAR(20) NULL;
    PRINT 'Columna dni_huesped agregada exitosamente';
END
ELSE
BEGIN
    PRINT 'La columna dni_huesped ya existe';
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Reserva') AND name = 'telefono_huesped')
BEGIN
    ALTER TABLE Reserva ADD telefono_huesped NVARCHAR(20) NULL;
    PRINT 'Columna telefono_huesped agregada exitosamente';
END
ELSE
BEGIN
    PRINT 'La columna telefono_huesped ya existe';
END
GO

-- Eliminar columna email_huesped si existe (ya no se usa, se toma del usuario registrado)
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Reserva') AND name = 'email_huesped')
BEGIN
    ALTER TABLE Reserva DROP COLUMN email_huesped;
    PRINT 'Columna email_huesped eliminada (se usa el email del usuario registrado)';
END
GO

-- Actualizar las columnas para que sean NOT NULL después de agregar datos de prueba (opcional)
-- Descomentar estas líneas después de tener datos en las reservas existentes
/*
ALTER TABLE Reserva ALTER COLUMN nombre_huesped NVARCHAR(100) NOT NULL;
ALTER TABLE Reserva ALTER COLUMN apellido_huesped NVARCHAR(100) NOT NULL;
ALTER TABLE Reserva ALTER COLUMN dni_huesped NVARCHAR(20) NOT NULL;
ALTER TABLE Reserva ALTER COLUMN email_huesped NVARCHAR(200) NOT NULL;
ALTER TABLE Reserva ALTER COLUMN telefono_huesped NVARCHAR(20) NOT NULL;
PRINT 'Columnas actualizadas a NOT NULL exitosamente';
*/
GO

-- Verificar las columnas agregadas
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Reserva' 
AND COLUMN_NAME IN ('nombre_huesped', 'apellido_huesped', 'dni_huesped', 'telefono_huesped')
ORDER BY ORDINAL_POSITION;
GO

PRINT 'Script completado exitosamente';
GO
