-- Actualizar columna url en HotelImagen para permitir imágenes base64 grandes
-- Esta columna almacenará imágenes en formato base64 que pueden ser muy extensas

USE HotelDemo;
GO

-- Verificar si existen datos en la tabla
IF EXISTS (SELECT 1 FROM HotelImagen)
BEGIN
    PRINT 'Se encontraron ' + CAST((SELECT COUNT(*) FROM HotelImagen) AS VARCHAR(10)) + ' registros en HotelImagen';
END

-- Modificar la columna url de VARCHAR/NVARCHAR(500) a NVARCHAR(MAX)
ALTER TABLE HotelImagen
ALTER COLUMN url NVARCHAR(MAX) NOT NULL;
GO

PRINT 'Columna url actualizada correctamente a NVARCHAR(MAX)';
GO

-- Verificar el cambio
SELECT 
    c.name AS ColumnName,
    t.name AS DataType,
    c.max_length AS MaxLength,
    c.is_nullable AS IsNullable
FROM sys.columns c
INNER JOIN sys.types t ON c.user_type_id = t.user_type_id
WHERE c.object_id = OBJECT_ID('HotelImagen')
AND c.name = 'url';
GO
