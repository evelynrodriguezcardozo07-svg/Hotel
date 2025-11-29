-- Convertir usuario en ADMIN para aprobar hoteles desde la interfaz
-- Esto te permite usar el panel de administrador para aprobar/rechazar hoteles

USE HotelDemo;
GO

-- Ver tu usuario actual
SELECT id, nombre, email, rol 
FROM Usuario 
WHERE email = 'rodriguezcardozoevelyn@gmail.com';
GO

-- Cambiar rol a ADMIN (descomenta las siguientes líneas)
/*
UPDATE Usuario 
SET rol = 'admin'
WHERE email = 'rodriguezcardozoevelyn@gmail.com';

PRINT 'Usuario convertido a ADMIN exitosamente';
GO

-- Verificar el cambio
SELECT id, nombre, email, rol 
FROM Usuario 
WHERE email = 'rodriguezcardozoevelyn@gmail.com';
GO
*/

-- NOTA: Con rol 'admin' podrás:
-- 1. Acceder a /admin en el frontend
-- 2. Ver todos los hoteles pendientes
-- 3. Aprobar o rechazar hoteles
-- 4. Después puedes volver a cambiar tu rol a 'host' si lo deseas
