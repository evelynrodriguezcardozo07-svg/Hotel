-- Insertar tipos de habitación predefinidos
USE HotelDemo;
GO

-- Verificar si ya existen tipos de habitación
IF NOT EXISTS (SELECT 1 FROM TipoHabitacion WHERE nombre = 'Individual')
BEGIN
    INSERT INTO TipoHabitacion (nombre, descripcion, icono, creado_en, actualizado_en)
    VALUES 
    ('Individual', 'Habitación individual con una cama simple, ideal para una persona', 'bed-single', GETDATE(), GETDATE()),
    ('Doble', 'Habitación doble con dos camas simples o una cama matrimonial', 'bed-double', GETDATE(), GETDATE()),
    ('Suite', 'Suite de lujo con sala de estar, dormitorio separado y amenidades premium', 'building', GETDATE(), GETDATE()),
    ('Familiar', 'Habitación espaciosa para familias con capacidad para 4-6 personas', 'users', GETDATE(), GETDATE());
    
    PRINT 'Tipos de habitación insertados correctamente.';
END
ELSE
BEGIN
    PRINT 'Los tipos de habitación ya existen.';
END
GO

-- Mostrar los tipos de habitación
SELECT * FROM TipoHabitacion;
GO
