-- =============================================
-- Sistema de Gestión Hotelera - Base de Datos
-- Proyecto Final Web - 8vo Ciclo
-- =============================================

-- Base de datos
CREATE DATABASE HotelDemo;
GO
USE HotelDemo;
GO

-- =============================================
-- TABLAS PRINCIPALES
-- =============================================

-- Tabla Usuario (incluye guest, admin, host)
CREATE TABLE [Usuario] (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre NVARCHAR(150) NOT NULL,
    email NVARCHAR(200) NOT NULL UNIQUE,
    telefono NVARCHAR(30) NULL,
    password_hash NVARCHAR(255) NOT NULL,
    rol NVARCHAR(20) NOT NULL CONSTRAINT DF_Usuario_Rol DEFAULT ('guest') 
        CHECK (rol IN ('guest', 'admin', 'host')),
    estado NVARCHAR(20) NOT NULL CONSTRAINT DF_Usuario_Estado DEFAULT ('activo')
        CHECK (estado IN ('activo', 'inactivo', 'bloqueado')),
    verificado BIT DEFAULT 0, -- Para verificación de email
    fecha_ultimo_acceso DATETIME2 NULL,
    creado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    actualizado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    eliminado_en DATETIME2 NULL -- Soft delete
);
GO

-- Índice para búsquedas por email y rol
CREATE INDEX IX_Usuario_Email ON Usuario(email) WHERE eliminado_en IS NULL;
CREATE INDEX IX_Usuario_Rol ON Usuario(rol) WHERE eliminado_en IS NULL;
GO

-- Tabla Direccion (opcional pero recomendada)
CREATE TABLE [Direccion] (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    calle NVARCHAR(250),
    ciudad NVARCHAR(100) NOT NULL, -- IMPORTANTE para búsquedas
    estado_provincia NVARCHAR(100) NULL,
    pais NVARCHAR(100) NOT NULL,
    codigo_postal NVARCHAR(20),
    latitud FLOAT NULL,
    longitud FLOAT NULL,
    creado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    actualizado_en DATETIME2 DEFAULT SYSUTCDATETIME()
);
GO

-- Índice para búsquedas por ciudad/país
CREATE INDEX IX_Direccion_Ciudad ON Direccion(ciudad, pais);
GO

-- Tabla Hotel
CREATE TABLE [Hotel] (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    propietario_id BIGINT NULL, -- NUEVO: FK al usuario propietario (rol=host)
    nombre NVARCHAR(200) NOT NULL,
    descripcion NVARCHAR(MAX) NULL,
    direccion_id BIGINT NULL,
    telefono NVARCHAR(50),
    email_contacto NVARCHAR(200) NULL,
    estrellas TINYINT NULL CHECK (estrellas BETWEEN 1 AND 5),
    precio_minimo DECIMAL(12,2) NULL, -- NUEVO: Para filtros rápidos
    precio_maximo DECIMAL(12,2) NULL, -- NUEVO: Para filtros rápidos
    estado NVARCHAR(30) NOT NULL DEFAULT ('pendiente') 
        CHECK (estado IN ('pendiente', 'aprobado', 'rechazado', 'inactivo')),
    destacado BIT DEFAULT 0, -- Para hoteles destacados
    puntuacion_promedio DECIMAL(3,2) NULL, -- Calculado desde reviews
    total_reviews INT DEFAULT 0,
    creado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    actualizado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    eliminado_en DATETIME2 NULL,
    CONSTRAINT FK_Hotel_Propietario FOREIGN KEY (propietario_id) REFERENCES Usuario(id),
    CONSTRAINT FK_Hotel_Direccion FOREIGN KEY (direccion_id) REFERENCES Direccion(id)
);
GO

-- Índices para búsquedas y filtros
CREATE INDEX IX_Hotel_Propietario ON Hotel(propietario_id) WHERE eliminado_en IS NULL;
CREATE INDEX IX_Hotel_Estado ON Hotel(estado) WHERE eliminado_en IS NULL;
CREATE INDEX IX_Hotel_Precio ON Hotel(precio_minimo, precio_maximo) WHERE eliminado_en IS NULL AND estado = 'aprobado';
CREATE INDEX IX_Hotel_Destacado ON Hotel(destacado, puntuacion_promedio DESC) WHERE eliminado_en IS NULL AND estado = 'aprobado';
GO

-- Tabla TipoHabitacion
CREATE TABLE [TipoHabitacion] (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre NVARCHAR(100) NOT NULL UNIQUE,
    descripcion NVARCHAR(MAX) NULL,
    icono NVARCHAR(100) NULL, -- Para UI (bed, star, etc)
    creado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    actualizado_en DATETIME2 DEFAULT SYSUTCDATETIME()
);
GO

-- Tabla Habitacion
CREATE TABLE [Habitacion] (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    numero NVARCHAR(50) NOT NULL,
    room_type_id BIGINT NOT NULL,
    nombre_corto NVARCHAR(100) NULL, -- Ej: "Suite Presidencial"
    precio_base DECIMAL(12,2) NOT NULL,
    capacidad INT NOT NULL DEFAULT 1,
    num_camas INT DEFAULT 1,
    metros_cuadrados DECIMAL(6,2) NULL,
    estado NVARCHAR(30) NOT NULL DEFAULT ('disponible')
        CHECK (estado IN ('disponible', 'mantenimiento', 'inactivo')),
    creado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    actualizado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    eliminado_en DATETIME2 NULL,
    CONSTRAINT FK_Habitacion_Hotel FOREIGN KEY (hotel_id) REFERENCES Hotel(id) ON DELETE CASCADE,
    CONSTRAINT FK_Habitacion_Tipo FOREIGN KEY (room_type_id) REFERENCES TipoHabitacion(id)
);
GO

-- índice único por hotel+numero
CREATE UNIQUE INDEX UX_Habitacion_Hotel_Numero ON Habitacion(hotel_id, numero) WHERE eliminado_en IS NULL;
CREATE INDEX IX_Habitacion_Precio ON Habitacion(precio_base, capacidad) WHERE eliminado_en IS NULL;
GO

-- =============================================
-- TABLAS DE TARIFAS Y DISPONIBILIDAD
-- =============================================

-- NUEVA: Tabla de Tarifas Especiales (temporadas altas, ofertas)
CREATE TABLE [TarifaEspecial] (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    habitacion_id BIGINT NOT NULL,
    nombre NVARCHAR(100) NOT NULL, -- "Temporada Alta", "Black Friday"
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    precio_especial DECIMAL(12,2) NOT NULL,
    tipo NVARCHAR(20) CHECK (tipo IN ('temporada', 'promocion', 'evento')),
    activo BIT DEFAULT 1,
    creado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    actualizado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_TarifaEspecial_Habitacion FOREIGN KEY (habitacion_id) REFERENCES Habitacion(id) ON DELETE CASCADE
);
GO

CREATE INDEX IX_TarifaEspecial_Fechas ON TarifaEspecial(habitacion_id, fecha_inicio, fecha_fin) WHERE activo = 1;
GO

-- Tabla RoomAvailability (disponibilidad por fecha)
CREATE TABLE [RoomAvailability] (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    habitacion_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    estado NVARCHAR(30) NOT NULL DEFAULT ('disponible') 
        CHECK (estado IN ('disponible', 'bloqueado', 'reservado', 'mantenimiento')),
    precio_dia DECIMAL(12,2) NULL, -- Precio efectivo para ese día (incluye tarifas especiales)
    nota NVARCHAR(500) NULL,
    creado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    actualizado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_RoomAvailability_Habitacion FOREIGN KEY (habitacion_id) REFERENCES Habitacion(id) ON DELETE CASCADE
);
GO

CREATE UNIQUE INDEX UX_RoomAvailability_Habitacion_Fecha ON RoomAvailability(habitacion_id, fecha);
CREATE INDEX IX_RoomAvailability_Fecha_Estado ON RoomAvailability(fecha, estado) WHERE estado = 'disponible';
GO

-- =============================================
-- TABLAS DE RESERVAS Y PAGOS
-- =============================================

-- Tabla Reserva
CREATE TABLE [Reserva] (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    codigo_reserva NVARCHAR(20) NOT NULL UNIQUE, -- NUEVO: Código único para el usuario
    usuario_id BIGINT NOT NULL,
    habitacion_id BIGINT NOT NULL,
    fecha_checkin DATE NOT NULL,
    fecha_checkout DATE NOT NULL,
    cantidad_huespedes INT NOT NULL DEFAULT 1,
    estado NVARCHAR(30) NOT NULL DEFAULT ('pendiente')
        CHECK (estado IN ('pendiente', 'confirmada', 'cancelada', 'completada', 'no_show')),
    subtotal DECIMAL(12,2) NOT NULL,
    impuestos DECIMAL(12,2) DEFAULT 0,
    total DECIMAL(12,2) NOT NULL,
    notas_especiales NVARCHAR(MAX) NULL,
    fecha_cancelacion DATETIME2 NULL,
    motivo_cancelacion NVARCHAR(500) NULL,
    creado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    actualizado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_Reserva_Usuario FOREIGN KEY (usuario_id) REFERENCES Usuario(id),
    CONSTRAINT FK_Reserva_Habitacion FOREIGN KEY (habitacion_id) REFERENCES Habitacion(id),
    CONSTRAINT CK_Reserva_Fechas CHECK (fecha_checkout > fecha_checkin)
);
GO

-- índice para buscar reservas por habitacion y fechas
CREATE INDEX IX_Reserva_Habitacion_Fechas ON Reserva(habitacion_id, fecha_checkin, fecha_checkout);
CREATE INDEX IX_Reserva_Usuario ON Reserva(usuario_id, estado);
CREATE INDEX IX_Reserva_Codigo ON Reserva(codigo_reserva);
CREATE INDEX IX_Reserva_Estado_Fechas ON Reserva(estado, fecha_checkin) WHERE estado IN ('confirmada', 'pendiente');
GO

-- Tabla Pago
CREATE TABLE [Pago] (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    reserva_id BIGINT NOT NULL,
    monto DECIMAL(12,2) NOT NULL,
    moneda NVARCHAR(10) NOT NULL DEFAULT ('PEN'),
    metodo NVARCHAR(50) NULL CHECK (metodo IN ('tarjeta', 'paypal', 'transferencia', 'efectivo', 'yape', 'plin')),
    estado NVARCHAR(30) NOT NULL DEFAULT ('pendiente')
        CHECK (estado IN ('pendiente', 'procesando', 'completado', 'fallido', 'reembolsado')),
    transaccion_id NVARCHAR(200) NULL,
    proveedor_pago NVARCHAR(100) NULL, -- Stripe, PayPal, Culqi, etc.
    fecha_pago DATETIME2 NULL,
    creado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    actualizado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_Pago_Reserva FOREIGN KEY (reserva_id) REFERENCES Reserva(id)
);
GO

CREATE INDEX IX_Pago_Reserva ON Pago(reserva_id);
CREATE INDEX IX_Pago_Estado ON Pago(estado, fecha_pago);
GO

-- =============================================
-- TABLAS DE AMENIDADES Y SERVICIOS
-- =============================================

-- Tabla Amenidad
CREATE TABLE [Amenity] (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre NVARCHAR(120) NOT NULL UNIQUE,
    descripcion NVARCHAR(MAX) NULL,
    icono NVARCHAR(100) NULL, -- Para UI (wifi, pool, gym, etc)
    categoria NVARCHAR(50) NULL, -- 'basico', 'premium', 'entretenimiento', 'negocios'
    creado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    actualizado_en DATETIME2 DEFAULT SYSUTCDATETIME()
);
GO

-- Tabla HotelAmenity (many-to-many)
CREATE TABLE [HotelAmenity] (
    hotel_id BIGINT NOT NULL,
    amenity_id BIGINT NOT NULL,
    detalle NVARCHAR(200) NULL, -- "24h", "Gratuito", "Con costo adicional"
    es_gratuito BIT DEFAULT 1,
    precio_adicional DECIMAL(10,2) NULL,
    creado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    actualizado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_HotelAmenity PRIMARY KEY (hotel_id, amenity_id),
    CONSTRAINT FK_HotelAmenity_Hotel FOREIGN KEY (hotel_id) REFERENCES Hotel(id) ON DELETE CASCADE,
    CONSTRAINT FK_HotelAmenity_Amenity FOREIGN KEY (amenity_id) REFERENCES Amenity(id)
);
GO

-- NUEVA: Amenidades de Habitación
CREATE TABLE [HabitacionAmenity] (
    habitacion_id BIGINT NOT NULL,
    amenity_id BIGINT NOT NULL,
    detalle NVARCHAR(200) NULL,
    creado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_HabitacionAmenity PRIMARY KEY (habitacion_id, amenity_id),
    CONSTRAINT FK_HabitacionAmenity_Habitacion FOREIGN KEY (habitacion_id) REFERENCES Habitacion(id) ON DELETE CASCADE,
    CONSTRAINT FK_HabitacionAmenity_Amenity FOREIGN KEY (amenity_id) REFERENCES Amenity(id)
);
GO

-- =============================================
-- TABLAS DE REVIEWS E IMÁGENES
-- =============================================

-- Tabla Review
CREATE TABLE [Review] (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    hotel_id BIGINT NOT NULL,
    reserva_id BIGINT NULL, -- NUEVO: Vincular con reserva para verificar estadía
    puntuacion TINYINT NOT NULL CHECK (puntuacion BETWEEN 1 AND 5),
    puntuacion_limpieza TINYINT NULL CHECK (puntuacion_limpieza BETWEEN 1 AND 5),
    puntuacion_servicio TINYINT NULL CHECK (puntuacion_servicio BETWEEN 1 AND 5),
    puntuacion_ubicacion TINYINT NULL CHECK (puntuacion_ubicacion BETWEEN 1 AND 5),
    comentario NVARCHAR(MAX) NULL,
    respuesta_hotel NVARCHAR(MAX) NULL, -- Respuesta del propietario
    fecha_respuesta DATETIME2 NULL,
    verificado BIT DEFAULT 0, -- Si la reserva fue verificada
    util_count INT DEFAULT 0, -- Contador de "útil"
    creado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    actualizado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    eliminado_en DATETIME2 NULL,
    CONSTRAINT FK_Review_Usuario FOREIGN KEY (usuario_id) REFERENCES Usuario(id),
    CONSTRAINT FK_Review_Hotel FOREIGN KEY (hotel_id) REFERENCES Hotel(id),
    CONSTRAINT FK_Review_Reserva FOREIGN KEY (reserva_id) REFERENCES Reserva(id)
);
GO

CREATE INDEX IX_Review_Hotel ON Review(hotel_id, creado_en DESC) WHERE eliminado_en IS NULL;
CREATE INDEX IX_Review_Usuario ON Review(usuario_id) WHERE eliminado_en IS NULL;
CREATE UNIQUE INDEX UX_Review_Reserva ON Review(reserva_id) WHERE reserva_id IS NOT NULL AND eliminado_en IS NULL;
GO

-- Tabla HotelImagen
CREATE TABLE [HotelImagen] (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    url NVARCHAR(500) NOT NULL,
    alt_text NVARCHAR(300) NULL,
    tipo NVARCHAR(50) DEFAULT 'general', -- 'portada', 'general', 'lobby', 'exterior'
    orden INT DEFAULT 0,
    es_principal BIT DEFAULT 0,
    creado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    actualizado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_HotelImagen_Hotel FOREIGN KEY (hotel_id) REFERENCES Hotel(id) ON DELETE CASCADE
);
GO

CREATE INDEX IX_HotelImagen_Hotel ON HotelImagen(hotel_id, orden);
GO

-- Tabla HabitacionImagen
CREATE TABLE [HabitacionImagen] (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    habitacion_id BIGINT NOT NULL,
    url NVARCHAR(500) NOT NULL,
    alt_text NVARCHAR(300) NULL,
    orden INT DEFAULT 0,
    es_principal BIT DEFAULT 0,
    creado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    actualizado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_HabitacionImagen_Habitacion FOREIGN KEY (habitacion_id) REFERENCES Habitacion(id) ON DELETE CASCADE
);
GO

CREATE INDEX IX_HabitacionImagen_Habitacion ON HabitacionImagen(habitacion_id, orden);
GO

-- =============================================
-- TABLAS ADICIONALES PARA FUNCIONALIDAD COMPLETA
-- =============================================

-- NUEVA: Tabla de Logs de Auditoría
CREATE TABLE [AuditLog] (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    usuario_id BIGINT NULL,
    tabla NVARCHAR(100) NOT NULL,
    registro_id BIGINT NOT NULL,
    accion NVARCHAR(50) NOT NULL, -- 'INSERT', 'UPDATE', 'DELETE'
    valores_antiguos NVARCHAR(MAX) NULL,
    valores_nuevos NVARCHAR(MAX) NULL,
    ip_address NVARCHAR(50) NULL,
    user_agent NVARCHAR(500) NULL,
    creado_en DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_AuditLog_Usuario FOREIGN KEY (usuario_id) REFERENCES Usuario(id)
);
GO

CREATE INDEX IX_AuditLog_Tabla_Registro ON AuditLog(tabla, registro_id, creado_en DESC);
GO

-- NUEVA: Tabla de Configuración del Sistema
CREATE TABLE [ConfiguracionSistema] (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    clave NVARCHAR(100) NOT NULL UNIQUE,
    valor NVARCHAR(MAX) NOT NULL,
    tipo NVARCHAR(50) NOT NULL, -- 'string', 'number', 'boolean', 'json'
    descripcion NVARCHAR(500) NULL,
    categoria NVARCHAR(100) NULL,
    actualizado_en DATETIME2 DEFAULT SYSUTCDATETIME()
);
GO

-- =============================================
-- VISTA PARA BÚSQUEDAS OPTIMIZADAS
-- =============================================

-- Vista para búsqueda de hoteles (incluye todos los campos necesarios para filtros)
CREATE VIEW [vw_HotelBusqueda] AS
SELECT 
    h.id,
    h.nombre,
    h.descripcion,
    h.estrellas,
    h.precio_minimo,
    h.precio_maximo,
    h.puntuacion_promedio,
    h.total_reviews,
    h.destacado,
    h.estado,
    d.ciudad,
    d.pais,
    d.estado_provincia,
    d.latitud,
    d.longitud,
    u.nombre AS propietario_nombre,
    u.email AS propietario_email,
    (SELECT COUNT(*) FROM Habitacion hab WHERE hab.hotel_id = h.id AND hab.eliminado_en IS NULL) AS total_habitaciones,
    (SELECT TOP 1 url FROM HotelImagen hi WHERE hi.hotel_id = h.id AND hi.es_principal = 1) AS imagen_principal,
    (SELECT STRING_AGG(a.nombre, ', ') 
     FROM HotelAmenity ha 
     INNER JOIN Amenity a ON ha.amenity_id = a.id 
     WHERE ha.hotel_id = h.id) AS amenidades
FROM Hotel h
LEFT JOIN Direccion d ON h.direccion_id = d.id
LEFT JOIN Usuario u ON h.propietario_id = u.id
WHERE h.eliminado_en IS NULL AND h.estado = 'aprobado';
GO

-- =============================================
-- DATOS DEMO Y CONFIGURACIÓN INICIAL
-- =============================================

-- Usuarios de prueba (passwords: "password123" - debes hashearlos en la aplicación)
INSERT INTO Usuario (nombre, email, telefono, password_hash, rol, verificado) VALUES 
('Admin Sistema', 'admin@hotel.com', '999-111-111', '$2a$10$DummyHashForDemo', 'admin', 1),
('Juan Propietario', 'propietario@hotel.com', '999-222-222', '$2a$10$DummyHashForDemo', 'host', 1),
('María Cliente', 'cliente@hotel.com', '999-333-333', '$2a$10$DummyHashForDemo', 'guest', 1);
GO

-- Tipos de Habitación
INSERT INTO TipoHabitacion (nombre, descripcion, icono) VALUES 
('Individual', 'Habitación para una persona', 'single-bed'),
('Doble', 'Habitación con cama doble o dos camas', 'double-bed'),
('Suite', 'Suite premium con sala', 'star'),
('Suite Ejecutiva', 'Suite con área de trabajo', 'briefcase'),
('Familiar', 'Habitación amplia para familias', 'users');
GO

-- Amenidades
INSERT INTO Amenity(nombre, descripcion, icono, categoria) VALUES 
('WiFi Gratis', 'Internet inalámbrico de alta velocidad', 'wifi', 'basico'),
('Piscina', 'Piscina al aire libre', 'swimming-pool', 'entretenimiento'),
('Gimnasio', 'Gimnasio equipado 24h', 'dumbbell', 'entretenimiento'),
('Estacionamiento', 'Estacionamiento privado', 'car', 'basico'),
('Restaurant', 'Restaurant en el hotel', 'utensils', 'basico'),
('Room Service', 'Servicio a la habitación 24h', 'concierge-bell', 'premium'),
('Spa', 'Spa y masajes', 'spa', 'premium'),
('Centro de Negocios', 'Sala de reuniones y coworking', 'laptop', 'negocios'),
('Aire Acondicionado', 'Climatización en habitaciones', 'snowflake', 'basico'),
('TV Cable', 'Televisión por cable', 'tv', 'basico'),
('Bar', 'Bar en el hotel', 'glass-martini', 'entretenimiento'),
('Lavandería', 'Servicio de lavandería', 'tshirt', 'basico');
GO

-- Direcciones
INSERT INTO Direccion(calle, ciudad, estado_provincia, pais, codigo_postal, latitud, longitud) VALUES 
('Av. Larco 1234', 'Lima', 'Lima', 'Perú', '15074', -12.1186, -77.0289),
('Jr. Pizarro 567', 'Trujillo', 'La Libertad', 'Perú', '13001', -8.1116, -79.0288),
('Av. El Sol 890', 'Cusco', 'Cusco', 'Perú', '08000', -13.5320, -71.9675),
('Malecón Cisneros 1456', 'Lima', 'Lima', 'Perú', '15074', -12.1317, -77.0210);
GO

-- Hoteles Demo
INSERT INTO Hotel(propietario_id, nombre, descripcion, direccion_id, telefono, email_contacto, estrellas, precio_minimo, precio_maximo, estado, destacado, puntuacion_promedio, total_reviews) VALUES 
(2, 'Hotel Miraflores Grand', 'Hotel de lujo en el corazón de Miraflores con vista al mar', 1, '01-445-5678', 'info@mirafgrand.com', 5, 150.00, 450.00, 'aprobado', 1, 4.5, 127),
(2, 'Hotel Centro Trujillo', 'Hotel céntrico ideal para negocios y turismo', 2, '044-234-567', 'contacto@centrotrujillo.com', 4, 80.00, 220.00, 'aprobado', 0, 4.2, 89),
(2, 'Cusco Plaza Hotel', 'Hotel colonial cerca de la Plaza de Armas', 3, '084-123-456', 'reservas@cuscoplaza.com', 4, 100.00, 280.00, 'aprobado', 1, 4.7, 203);
GO

-- Habitaciones
INSERT INTO Habitacion(hotel_id, numero, room_type_id, nombre_corto, precio_base, capacidad, num_camas, metros_cuadrados, estado) VALUES 
-- Hotel Miraflores Grand
(1, '101', 2, 'Doble Standard', 150.00, 2, 1, 25.0, 'disponible'),
(1, '102', 2, 'Doble Standard', 150.00, 2, 1, 25.0, 'disponible'),
(1, '201', 3, 'Suite Ocean View', 350.00, 3, 2, 45.0, 'disponible'),
(1, '301', 4, 'Suite Ejecutiva', 450.00, 2, 1, 55.0, 'disponible'),
-- Hotel Centro Trujillo
(2, '101', 1, 'Individual', 80.00, 1, 1, 18.0, 'disponible'),
(2, '102', 2, 'Doble', 120.00, 2, 1, 22.0, 'disponible'),
(2, '201', 3, 'Suite', 220.00, 3, 2, 35.0, 'disponible'),
-- Cusco Plaza Hotel
(3, '101', 2, 'Doble Colonial', 100.00, 2, 1, 20.0, 'disponible'),
(3, '102', 5, 'Familiar', 200.00, 4, 2, 40.0, 'disponible'),
(3, '301', 3, 'Suite Plaza', 280.00, 3, 2, 50.0, 'disponible');
GO

-- Amenidades de Hoteles
INSERT INTO HotelAmenity(hotel_id, amenity_id, detalle, es_gratuito) VALUES
-- Hotel Miraflores Grand
(1, 1, 'Alta velocidad en todas las áreas', 1),
(1, 2, 'Piscina infinity con vista al mar', 1),
(1, 3, 'Gimnasio 24 horas', 1),
(1, 4, 'Estacionamiento con valet', 0),
(1, 5, 'Restaurant gourmet', 0),
(1, 6, 'Disponible 24/7', 0),
(1, 7, 'Spa premium', 0),
(1, 8, 'Con equipamiento completo', 1),
-- Hotel Centro Trujillo
(2, 1, 'WiFi gratuito', 1),
(2, 3, 'Gimnasio básico', 1),
(2, 4, 'Estacionamiento gratuito', 1),
(2, 5, 'Desayuno buffet', 0),
(2, 8, 'Sala de reuniones', 0),
-- Cusco Plaza Hotel
(3, 1, 'WiFi en áreas comunes', 1),
(3, 4, 'Estacionamiento limitado', 1),
(3, 5, 'Restaurant típico', 0),
(3, 11, 'Bar con tragos típicos', 0);
GO

-- Imágenes de Hoteles (URLs demo)
INSERT INTO HotelImagen(hotel_id, url, alt_text, tipo, orden, es_principal) VALUES
(1, '/images/hotels/miraflores-1.jpg', 'Fachada Hotel Miraflores Grand', 'portada', 0, 1),
(1, '/images/hotels/miraflores-2.jpg', 'Piscina infinity', 'general', 1, 0),
(1, '/images/hotels/miraflores-3.jpg', 'Lobby principal', 'lobby', 2, 0),
(2, '/images/hotels/trujillo-1.jpg', 'Hotel Centro Trujillo', 'portada', 0, 1),
(3, '/images/hotels/cusco-1.jpg', 'Cusco Plaza Hotel colonial', 'portada', 0, 1);
GO

-- Configuración del Sistema
INSERT INTO ConfiguracionSistema(clave, valor, tipo, descripcion, categoria) VALUES
('impuesto_igv', '18', 'number', 'Porcentaje de IGV para Perú', 'finanzas'),
('moneda_default', 'PEN', 'string', 'Moneda por defecto del sistema', 'finanzas'),
('dias_cancelacion_gratis', '3', 'number', 'Días antes del check-in para cancelación gratuita', 'reservas'),
('max_huespedes_habitacion', '6', 'number', 'Máximo de huéspedes por habitación', 'reservas'),
('email_notificaciones', 'notificaciones@hotel.com', 'string', 'Email para notificaciones del sistema', 'sistema'),
('requiere_verificacion_email', 'true', 'boolean', 'Requiere verificar email al registrarse', 'seguridad'),
('dias_review_disponible', '30', 'number', 'Días después del checkout para dejar review', 'reviews');
GO

-- Reservas Demo
INSERT INTO Reserva(codigo_reserva, usuario_id, habitacion_id, fecha_checkin, fecha_checkout, cantidad_huespedes, estado, subtotal, impuestos, total, notas_especiales) VALUES
('RES-2025-0001', 3, 1, '2025-12-15', '2025-12-18', 2, 'confirmada', 450.00, 81.00, 531.00, 'Habitación en piso alto, vista al mar'),
('RES-2025-0002', 3, 8, '2025-11-20', '2025-11-23', 2, 'confirmada', 300.00, 54.00, 354.00, NULL);
GO

-- Reviews Demo
INSERT INTO Review(usuario_id, hotel_id, reserva_id, puntuacion, puntuacion_limpieza, puntuacion_servicio, puntuacion_ubicacion, comentario, verificado) VALUES
(3, 1, NULL, 5, 5, 5, 5, 'Excelente hotel, la vista al mar es espectacular. El personal muy atento y las instalaciones de primera.', 0);
GO

PRINT 'Base de datos creada exitosamente con datos demo';
PRINT '==============================================';
PRINT 'Usuarios creados:';
PRINT '  Admin: admin@hotel.com';
PRINT '  Propietario: propietario@hotel.com';
PRINT '  Cliente: cliente@hotel.com';
PRINT '  Password para todos: password123 (debe hashearse)';
PRINT '==============================================';
GO
