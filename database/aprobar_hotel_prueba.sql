-- Script para aprobar hoteles pendientes (SOLO PARA PRUEBAS)
-- En producción, esto debe hacerse desde el panel de administrador

USE HotelDemo;
GO

-- Ver hoteles pendientes
SELECT id, nombre, estado, creado_en 
FROM Hotel 
WHERE estado = 'pendiente' 
  AND eliminado_en IS NULL
ORDER BY creado_en DESC;
GO

-- Aprobar el último hotel creado (descomenta las siguientes líneas)
/*
UPDATE Hotel 
SET estado = 'aprobado'
WHERE id = (
    SELECT TOP 1 id 
    FROM Hotel 
    WHERE estado = 'pendiente' 
      AND eliminado_en IS NULL
    ORDER BY creado_en DESC
);

PRINT 'Hotel aprobado correctamente';
GO
*/

-- O aprobar un hotel específico por su ID (reemplaza el número)
/*
UPDATE Hotel 
SET estado = 'aprobado'
WHERE id = 1;  -- Cambia este número por el ID de tu hotel

PRINT 'Hotel ID 1 aprobado correctamente';
GO
*/
