-- Crear tabla de cupones
CREATE TABLE [Cupon] (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    codigo NVARCHAR(50) NOT NULL UNIQUE,
    descripcion NVARCHAR(500),
    tipo_descuento NVARCHAR(20) NOT NULL CHECK (tipo_descuento IN ('porcentaje', 'monto_fijo')),
    valor_descuento DECIMAL(10,2) NOT NULL,
    monto_minimo DECIMAL(10,2),
    fecha_inicio DATE,
    fecha_fin DATE,
    usos_maximos INT,
    usos_actuales INT DEFAULT 0,
    activo BIT DEFAULT 1,
    hotel_id BIGINT NULL,
    creado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    actualizado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_Cupon_Hotel FOREIGN KEY (hotel_id) REFERENCES Hotel(id)
);
GO

-- Índices
CREATE INDEX IX_Cupon_Codigo ON Cupon(codigo);
CREATE INDEX IX_Cupon_Activo ON Cupon(activo, fecha_inicio, fecha_fin);
GO

-- Cupones de ejemplo
INSERT INTO Cupon (codigo, descripcion, tipo_descuento, valor_descuento, monto_minimo, fecha_inicio, fecha_fin, usos_maximos, activo) VALUES
('BIENVENIDA10', 'Descuento de bienvenida del 10%', 'porcentaje', 10.00, 100.00, '2025-01-01', '2025-12-31', 100, 1),
('VERANO2025', 'Descuento especial de verano', 'porcentaje', 15.00, 200.00, '2025-12-01', '2026-03-31', 500, 1),
('50SOLES', 'Descuento fijo de 50 soles', 'monto_fijo', 50.00, 300.00, '2025-01-01', '2025-12-31', NULL, 1);
GO

PRINT 'Tabla Cupon creada exitosamente';
