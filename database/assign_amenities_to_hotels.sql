-- Script para asignar amenidades a todos los hoteles existentes
-- Esto permite que los filtros funcionen correctamente

DECLARE @wifi_id INT, @piscina_id INT, @gimnasio_id INT, @estacionamiento_id INT;
DECLARE @restaurant_id INT, @room_service_id INT, @spa_id INT, @bar_id INT;

-- Obtener IDs de las amenidades
SELECT @wifi_id = id FROM Amenity WHERE nombre = 'WiFi Gratis';
SELECT @piscina_id = id FROM Amenity WHERE nombre = 'Piscina';
SELECT @gimnasio_id = id FROM Amenity WHERE nombre = 'Gimnasio';
SELECT @estacionamiento_id = id FROM Amenity WHERE nombre = 'Estacionamiento';
SELECT @restaurant_id = id FROM Amenity WHERE nombre = 'Restaurant';
SELECT @room_service_id = id FROM Amenity WHERE nombre = 'Room Service';
SELECT @spa_id = id FROM Amenity WHERE nombre = 'Spa';
SELECT @bar_id = id FROM Amenity WHERE nombre = 'Bar';

PRINT 'Asignando amenidades a hoteles existentes...';

-- Para cada hotel, asignar algunas amenidades de ejemplo
-- Puedes ajustar esto según las características reales de cada hotel

-- Hotel 1 y 2: Hoteles básicos (WiFi, Estacionamiento)
INSERT INTO HotelAmenity (hotel_id, amenity_id, es_gratuito, creado_en, actualizado_en)
SELECT h.id, @wifi_id, 1, GETDATE(), GETDATE()
FROM Hotel h
WHERE h.id IN (1, 2)
AND NOT EXISTS (SELECT 1 FROM HotelAmenity WHERE hotel_id = h.id AND amenity_id = @wifi_id);

INSERT INTO HotelAmenity (hotel_id, amenity_id, es_gratuito, creado_en, actualizado_en)
SELECT h.id, @estacionamiento_id, 1, GETDATE(), GETDATE()
FROM Hotel h
WHERE h.id IN (1, 2)
AND NOT EXISTS (SELECT 1 FROM HotelAmenity WHERE hotel_id = h.id AND amenity_id = @estacionamiento_id);

-- Hotel 3 y 4: Hoteles de rango medio (WiFi, Estacionamiento, Restaurant, Bar)
INSERT INTO HotelAmenity (hotel_id, amenity_id, es_gratuito, creado_en, actualizado_en)
SELECT h.id, a.id, 1, GETDATE(), GETDATE()
FROM Hotel h
CROSS JOIN (
    SELECT id FROM Amenity WHERE nombre IN ('WiFi Gratis', 'Estacionamiento', 'Restaurant', 'Bar')
) a
WHERE h.id IN (3, 4)
AND NOT EXISTS (SELECT 1 FROM HotelAmenity WHERE hotel_id = h.id AND amenity_id = a.id);

-- Hoteles de 4 y 5 estrellas: Todas las amenidades
INSERT INTO HotelAmenity (hotel_id, amenity_id, es_gratuito, creado_en, actualizado_en)
SELECT h.id, a.id, 1, GETDATE(), GETDATE()
FROM Hotel h
CROSS JOIN Amenity a
WHERE h.estrellas >= 4
AND NOT EXISTS (SELECT 1 FROM HotelAmenity WHERE hotel_id = h.id AND amenity_id = a.id);

PRINT 'Amenidades asignadas correctamente';

-- Ver resumen de amenidades por hotel
SELECT 
    h.id,
    h.nombre,
    h.estrellas,
    COUNT(ha.amenity_id) as total_amenidades,
    STRING_AGG(am.nombre, ', ') as amenidades
FROM Hotel h
LEFT JOIN HotelAmenity ha ON h.id = ha.hotel_id
LEFT JOIN Amenity am ON ha.amenity_id = am.id
GROUP BY h.id, h.nombre, h.estrellas
ORDER BY h.id;
