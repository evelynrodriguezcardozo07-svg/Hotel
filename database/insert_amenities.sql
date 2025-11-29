-- Script para insertar las amenidades básicas en el sistema
-- Ejecutar este script para agregar las amenidades que se pueden filtrar

-- Insertar amenidades si no existen
IF NOT EXISTS (SELECT 1 FROM Amenity WHERE nombre = 'WiFi Gratis')
    INSERT INTO Amenity (nombre, descripcion, icono, categoria, creado_en, actualizado_en)
    VALUES ('WiFi Gratis', 'Internet inalámbrico de alta velocidad sin costo adicional', 'wifi', 'basico', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM Amenity WHERE nombre = 'Piscina')
    INSERT INTO Amenity (nombre, descripcion, icono, categoria, creado_en, actualizado_en)
    VALUES ('Piscina', 'Piscina al aire libre o techada para huéspedes', 'pool', 'entretenimiento', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM Amenity WHERE nombre = 'Gimnasio')
    INSERT INTO Amenity (nombre, descripcion, icono, categoria, creado_en, actualizado_en)
    VALUES ('Gimnasio', 'Centro de fitness con equipo moderno', 'gym', 'entretenimiento', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM Amenity WHERE nombre = 'Estacionamiento')
    INSERT INTO Amenity (nombre, descripcion, icono, categoria, creado_en, actualizado_en)
    VALUES ('Estacionamiento', 'Estacionamiento privado para huéspedes', 'parking', 'basico', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM Amenity WHERE nombre = 'Restaurant')
    INSERT INTO Amenity (nombre, descripcion, icono, categoria, creado_en, actualizado_en)
    VALUES ('Restaurant', 'Restaurante en las instalaciones del hotel', 'restaurant', 'premium', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM Amenity WHERE nombre = 'Room Service')
    INSERT INTO Amenity (nombre, descripcion, icono, categoria, creado_en, actualizado_en)
    VALUES ('Room Service', 'Servicio a la habitación disponible', 'room-service', 'premium', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM Amenity WHERE nombre = 'Spa')
    INSERT INTO Amenity (nombre, descripcion, icono, categoria, creado_en, actualizado_en)
    VALUES ('Spa', 'Spa y servicios de bienestar', 'spa', 'premium', GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM Amenity WHERE nombre = 'Bar')
    INSERT INTO Amenity (nombre, descripcion, icono, categoria, creado_en, actualizado_en)
    VALUES ('Bar', 'Bar o lounge en el hotel', 'bar', 'entretenimiento', GETDATE(), GETDATE());

PRINT 'Amenidades insertadas correctamente';

-- Ver las amenidades insertadas
SELECT * FROM Amenity ORDER BY categoria, nombre;
