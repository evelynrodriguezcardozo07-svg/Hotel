-- =============================================
-- Sistema de Gestión Hotelera - Base de Datos PostgreSQL
-- Proyecto Final Web - 8vo Ciclo
-- Migrado desde SQL Server a PostgreSQL
-- =============================================

-- =============================================
-- TABLAS PRINCIPALES
-- =============================================

-- Tabla Usuario (incluye guest, admin, host)
CREATE TABLE IF NOT EXISTS usuario (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    email VARCHAR(200) NOT NULL UNIQUE,
    telefono VARCHAR(30),
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL DEFAULT 'guest' CHECK (rol IN ('guest', 'admin', 'host')),
    estado VARCHAR(20) NOT NULL DEFAULT 'activo' CHECK (estado IN ('activo', 'inactivo', 'bloqueado')),
    verificado BOOLEAN DEFAULT FALSE,
    fecha_ultimo_acceso TIMESTAMP,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    eliminado_en TIMESTAMP
);

-- Índices para búsquedas por email y rol
CREATE INDEX idx_usuario_email ON usuario(email) WHERE eliminado_en IS NULL;
CREATE INDEX idx_usuario_rol ON usuario(rol) WHERE eliminado_en IS NULL;

-- Tabla Direccion
CREATE TABLE IF NOT EXISTS direccion (
    id BIGSERIAL PRIMARY KEY,
    calle VARCHAR(250),
    ciudad VARCHAR(100) NOT NULL,
    estado_provincia VARCHAR(100),
    pais VARCHAR(100) NOT NULL,
    codigo_postal VARCHAR(20),
    latitud DOUBLE PRECISION,
    longitud DOUBLE PRECISION,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índice para búsquedas por ciudad/país
CREATE INDEX idx_direccion_ciudad ON direccion(ciudad, pais);

-- Tabla Hotel
CREATE TABLE IF NOT EXISTS hotel (
    id BIGSERIAL PRIMARY KEY,
    propietario_id BIGINT,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    direccion_id BIGINT,
    telefono VARCHAR(50),
    email_contacto VARCHAR(200),
    estrellas SMALLINT CHECK (estrellas BETWEEN 1 AND 5),
    precio_minimo DECIMAL(12,2),
    precio_maximo DECIMAL(12,2),
    estado VARCHAR(30) NOT NULL DEFAULT 'pendiente' CHECK (estado IN ('pendiente', 'aprobado', 'rechazado', 'inactivo')),
    destacado BOOLEAN DEFAULT FALSE,
    puntuacion_promedio DECIMAL(3,2),
    total_reviews INTEGER DEFAULT 0,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    eliminado_en TIMESTAMP,
    CONSTRAINT fk_hotel_propietario FOREIGN KEY (propietario_id) REFERENCES usuario(id),
    CONSTRAINT fk_hotel_direccion FOREIGN KEY (direccion_id) REFERENCES direccion(id)
);

-- Índices para búsquedas y filtros
CREATE INDEX idx_hotel_propietario ON hotel(propietario_id) WHERE eliminado_en IS NULL;
CREATE INDEX idx_hotel_estado ON hotel(estado) WHERE eliminado_en IS NULL;
CREATE INDEX idx_hotel_precio ON hotel(precio_minimo, precio_maximo) WHERE eliminado_en IS NULL AND estado = 'aprobado';
CREATE INDEX idx_hotel_destacado ON hotel(destacado, puntuacion_promedio DESC) WHERE eliminado_en IS NULL AND estado = 'aprobado';

-- Tabla TipoHabitacion
CREATE TABLE IF NOT EXISTS tipo_habitacion (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    icono VARCHAR(100),
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla Habitacion
CREATE TABLE IF NOT EXISTS habitacion (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    numero VARCHAR(50) NOT NULL,
    room_type_id BIGINT NOT NULL,
    nombre_corto VARCHAR(100),
    precio_base DECIMAL(12,2) NOT NULL,
    capacidad INTEGER NOT NULL DEFAULT 1,
    num_camas INTEGER DEFAULT 1,
    metros_cuadrados DECIMAL(6,2),
    estado VARCHAR(30) NOT NULL DEFAULT 'disponible' CHECK (estado IN ('disponible', 'mantenimiento', 'inactivo')),
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    eliminado_en TIMESTAMP,
    CONSTRAINT fk_habitacion_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id) ON DELETE CASCADE,
    CONSTRAINT fk_habitacion_tipo FOREIGN KEY (room_type_id) REFERENCES tipo_habitacion(id)
);

-- Índice único por hotel+numero
CREATE UNIQUE INDEX ux_habitacion_hotel_numero ON habitacion(hotel_id, numero) WHERE eliminado_en IS NULL;
CREATE INDEX idx_habitacion_precio ON habitacion(precio_base, capacidad) WHERE eliminado_en IS NULL;

-- =============================================
-- TABLAS DE TARIFAS Y DISPONIBILIDAD
-- =============================================

-- Tabla de Tarifas Especiales
CREATE TABLE IF NOT EXISTS tarifa_especial (
    id BIGSERIAL PRIMARY KEY,
    habitacion_id BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    precio_especial DECIMAL(12,2) NOT NULL,
    tipo VARCHAR(20) CHECK (tipo IN ('temporada', 'promocion', 'evento')),
    activo BOOLEAN DEFAULT TRUE,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tarifa_especial_habitacion FOREIGN KEY (habitacion_id) REFERENCES habitacion(id) ON DELETE CASCADE
);

CREATE INDEX idx_tarifa_especial_fechas ON tarifa_especial(habitacion_id, fecha_inicio, fecha_fin) WHERE activo = TRUE;

-- Tabla RoomAvailability
CREATE TABLE IF NOT EXISTS room_availability (
    id BIGSERIAL PRIMARY KEY,
    habitacion_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'disponible' CHECK (estado IN ('disponible', 'bloqueado', 'reservado', 'mantenimiento')),
    precio_dia DECIMAL(12,2),
    nota VARCHAR(500),
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_room_availability_habitacion FOREIGN KEY (habitacion_id) REFERENCES habitacion(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX ux_room_availability_habitacion_fecha ON room_availability(habitacion_id, fecha);
CREATE INDEX idx_room_availability_fecha_estado ON room_availability(fecha, estado) WHERE estado = 'disponible';

-- =============================================
-- TABLAS DE RESERVAS Y PAGOS
-- =============================================

-- Tabla Reserva
CREATE TABLE IF NOT EXISTS reserva (
    id BIGSERIAL PRIMARY KEY,
    codigo_reserva VARCHAR(20) NOT NULL UNIQUE,
    usuario_id BIGINT NOT NULL,
    habitacion_id BIGINT NOT NULL,
    fecha_checkin DATE NOT NULL,
    fecha_checkout DATE NOT NULL,
    cantidad_huespedes INTEGER NOT NULL DEFAULT 1,
    estado VARCHAR(30) NOT NULL DEFAULT 'pendiente' CHECK (estado IN ('pendiente', 'confirmada', 'cancelada', 'completada', 'no_show')),
    subtotal DECIMAL(12,2) NOT NULL,
    impuestos DECIMAL(12,2) DEFAULT 0,
    total DECIMAL(12,2) NOT NULL,
    notas_especiales TEXT,
    fecha_cancelacion TIMESTAMP,
    motivo_cancelacion VARCHAR(500),
    nombre_huesped VARCHAR(200),
    email_huesped VARCHAR(200),
    telefono_huesped VARCHAR(20),
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reserva_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_reserva_habitacion FOREIGN KEY (habitacion_id) REFERENCES habitacion(id),
    CONSTRAINT ck_reserva_fechas CHECK (fecha_checkout > fecha_checkin)
);

-- Índices para buscar reservas
CREATE INDEX idx_reserva_habitacion_fechas ON reserva(habitacion_id, fecha_checkin, fecha_checkout);
CREATE INDEX idx_reserva_usuario ON reserva(usuario_id, estado);
CREATE INDEX idx_reserva_codigo ON reserva(codigo_reserva);
CREATE INDEX idx_reserva_estado_fechas ON reserva(estado, fecha_checkin) WHERE estado IN ('confirmada', 'pendiente');

-- Tabla Pago
CREATE TABLE IF NOT EXISTS pago (
    id BIGSERIAL PRIMARY KEY,
    reserva_id BIGINT NOT NULL,
    monto DECIMAL(12,2) NOT NULL,
    moneda VARCHAR(10) NOT NULL DEFAULT 'PEN',
    metodo VARCHAR(50) CHECK (metodo IN ('tarjeta', 'paypal', 'transferencia', 'efectivo', 'yape', 'plin')),
    estado VARCHAR(30) NOT NULL DEFAULT 'pendiente' CHECK (estado IN ('pendiente', 'procesando', 'completado', 'fallido', 'reembolsado')),
    transaccion_id VARCHAR(200),
    proveedor_pago VARCHAR(100),
    fecha_pago TIMESTAMP,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pago_reserva FOREIGN KEY (reserva_id) REFERENCES reserva(id)
);

CREATE INDEX idx_pago_reserva ON pago(reserva_id);
CREATE INDEX idx_pago_estado ON pago(estado, fecha_pago);

-- =============================================
-- TABLAS DE AMENIDADES Y SERVICIOS
-- =============================================

-- Tabla Amenidad
CREATE TABLE IF NOT EXISTS amenity (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL UNIQUE,
    descripcion TEXT,
    icono VARCHAR(100),
    categoria VARCHAR(50),
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla HotelAmenity (many-to-many)
CREATE TABLE IF NOT EXISTS hotel_amenity (
    hotel_id BIGINT NOT NULL,
    amenity_id BIGINT NOT NULL,
    detalle VARCHAR(200),
    es_gratuito BOOLEAN DEFAULT TRUE,
    precio_adicional DECIMAL(10,2),
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_hotel_amenity PRIMARY KEY (hotel_id, amenity_id),
    CONSTRAINT fk_hotel_amenity_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id) ON DELETE CASCADE,
    CONSTRAINT fk_hotel_amenity_amenity FOREIGN KEY (amenity_id) REFERENCES amenity(id)
);

-- Amenidades de Habitación
CREATE TABLE IF NOT EXISTS habitacion_amenity (
    habitacion_id BIGINT NOT NULL,
    amenity_id BIGINT NOT NULL,
    detalle VARCHAR(200),
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_habitacion_amenity PRIMARY KEY (habitacion_id, amenity_id),
    CONSTRAINT fk_habitacion_amenity_habitacion FOREIGN KEY (habitacion_id) REFERENCES habitacion(id) ON DELETE CASCADE,
    CONSTRAINT fk_habitacion_amenity_amenity FOREIGN KEY (amenity_id) REFERENCES amenity(id)
);

-- =============================================
-- TABLAS DE REVIEWS E IMÁGENES
-- =============================================

-- Tabla Review
CREATE TABLE IF NOT EXISTS review (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    hotel_id BIGINT NOT NULL,
    reserva_id BIGINT,
    puntuacion SMALLINT NOT NULL CHECK (puntuacion BETWEEN 1 AND 5),
    puntuacion_limpieza SMALLINT CHECK (puntuacion_limpieza BETWEEN 1 AND 5),
    puntuacion_servicio SMALLINT CHECK (puntuacion_servicio BETWEEN 1 AND 5),
    puntuacion_ubicacion SMALLINT CHECK (puntuacion_ubicacion BETWEEN 1 AND 5),
    comentario TEXT,
    respuesta_hotel TEXT,
    fecha_respuesta TIMESTAMP,
    verificado BOOLEAN DEFAULT FALSE,
    util_count INTEGER DEFAULT 0,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    eliminado_en TIMESTAMP,
    CONSTRAINT fk_review_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_review_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id),
    CONSTRAINT fk_review_reserva FOREIGN KEY (reserva_id) REFERENCES reserva(id)
);

CREATE INDEX idx_review_hotel ON review(hotel_id, creado_en DESC) WHERE eliminado_en IS NULL;
CREATE INDEX idx_review_usuario ON review(usuario_id) WHERE eliminado_en IS NULL;
CREATE UNIQUE INDEX ux_review_reserva ON review(reserva_id) WHERE reserva_id IS NOT NULL AND eliminado_en IS NULL;

-- Tabla HotelImagen
CREATE TABLE IF NOT EXISTS hotel_imagen (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(300),
    tipo VARCHAR(50) DEFAULT 'general',
    orden INTEGER DEFAULT 0,
    es_principal BOOLEAN DEFAULT FALSE,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hotel_imagen_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id) ON DELETE CASCADE
);

CREATE INDEX idx_hotel_imagen_hotel ON hotel_imagen(hotel_id, orden);

-- Tabla HabitacionImagen
CREATE TABLE IF NOT EXISTS habitacion_imagen (
    id BIGSERIAL PRIMARY KEY,
    habitacion_id BIGINT NOT NULL,
    url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(300),
    orden INTEGER DEFAULT 0,
    es_principal BOOLEAN DEFAULT FALSE,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_habitacion_imagen_habitacion FOREIGN KEY (habitacion_id) REFERENCES habitacion(id) ON DELETE CASCADE
);

CREATE INDEX idx_habitacion_imagen_habitacion ON habitacion_imagen(habitacion_id, orden);

-- =============================================
-- TABLAS ADICIONALES
-- =============================================

-- Tabla de Logs de Auditoría
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT,
    tabla VARCHAR(100) NOT NULL,
    registro_id BIGINT NOT NULL,
    accion VARCHAR(50) NOT NULL,
    valores_antiguos TEXT,
    valores_nuevos TEXT,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_log_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE INDEX idx_audit_log_tabla_registro ON audit_log(tabla, registro_id, creado_en DESC);

-- Tabla de Configuración del Sistema
CREATE TABLE IF NOT EXISTS configuracion_sistema (
    id BIGSERIAL PRIMARY KEY,
    clave VARCHAR(100) NOT NULL UNIQUE,
    valor TEXT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    descripcion VARCHAR(500),
    categoria VARCHAR(100),
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Cupones
CREATE TABLE IF NOT EXISTS cupon (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT,
    tipo_descuento VARCHAR(20) NOT NULL CHECK (tipo_descuento IN ('porcentaje', 'monto_fijo')),
    valor_descuento DECIMAL(10,2) NOT NULL,
    monto_minimo DECIMAL(10,2),
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    usos_maximos INTEGER,
    usos_actuales INTEGER DEFAULT 0,
    activo BOOLEAN DEFAULT TRUE,
    hotel_id BIGINT,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cupon_hotel FOREIGN KEY (hotel_id) REFERENCES hotel(id)
);

CREATE INDEX idx_cupon_codigo ON cupon(codigo) WHERE activo = TRUE;
CREATE INDEX idx_cupon_fechas ON cupon(fecha_inicio, fecha_fin) WHERE activo = TRUE;

-- Tabla de Notificaciones
CREATE TABLE IF NOT EXISTS notificacion_usuario (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    mensaje TEXT NOT NULL,
    url VARCHAR(500),
    leido BOOLEAN DEFAULT FALSE,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    leido_en TIMESTAMP,
    CONSTRAINT fk_notificacion_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE INDEX idx_notificacion_usuario ON notificacion_usuario(usuario_id, leido, creado_en DESC);

-- =============================================
-- VISTA PARA BÚSQUEDAS OPTIMIZADAS
-- =============================================

CREATE OR REPLACE VIEW vw_hotel_busqueda AS
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
    (SELECT COUNT(*) FROM habitacion hab WHERE hab.hotel_id = h.id AND hab.eliminado_en IS NULL) AS total_habitaciones,
    (SELECT url FROM hotel_imagen hi WHERE hi.hotel_id = h.id AND hi.es_principal = TRUE LIMIT 1) AS imagen_principal,
    (SELECT STRING_AGG(a.nombre, ', ') 
     FROM hotel_amenity ha 
     INNER JOIN amenity a ON ha.amenity_id = a.id 
     WHERE ha.hotel_id = h.id) AS amenidades
FROM hotel h
LEFT JOIN direccion d ON h.direccion_id = d.id
LEFT JOIN usuario u ON h.propietario_id = u.id
WHERE h.eliminado_en IS NULL AND h.estado = 'aprobado';

-- =============================================
-- DATOS DEMO Y CONFIGURACIÓN INICIAL
-- =============================================

-- Usuarios de prueba (passwords: "password123" - debes hashearlos en la aplicación)
INSERT INTO usuario (nombre, email, telefono, password_hash, rol, verificado) VALUES 
('Admin Sistema', 'admin@hotel.com', '999-111-111', '$2a$10$DummyHashForDemo', 'admin', TRUE),
('Juan Propietario', 'propietario@hotel.com', '999-222-222', '$2a$10$DummyHashForDemo', 'host', TRUE),
('María Cliente', 'cliente@hotel.com', '999-333-333', '$2a$10$DummyHashForDemo', 'guest', TRUE)
ON CONFLICT (email) DO NOTHING;

-- Tipos de Habitación
INSERT INTO tipo_habitacion (nombre, descripcion, icono) VALUES 
('Individual', 'Habitación para una persona', 'single-bed'),
('Doble', 'Habitación con cama doble o dos camas', 'double-bed'),
('Suite', 'Suite premium con sala', 'star'),
('Suite Ejecutiva', 'Suite con área de trabajo', 'briefcase'),
('Familiar', 'Habitación amplia para familias', 'users')
ON CONFLICT (nombre) DO NOTHING;

-- Amenidades
INSERT INTO amenity (nombre, descripcion, icono, categoria) VALUES 
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
('Lavandería', 'Servicio de lavandería', 'tshirt', 'basico')
ON CONFLICT (nombre) DO NOTHING;

-- Direcciones
INSERT INTO direccion (calle, ciudad, estado_provincia, pais, codigo_postal, latitud, longitud) VALUES 
('Av. Larco 1234', 'Lima', 'Lima', 'Perú', '15074', -12.1186, -77.0289),
('Jr. Pizarro 567', 'Trujillo', 'La Libertad', 'Perú', '13001', -8.1116, -79.0288),
('Av. El Sol 890', 'Cusco', 'Cusco', 'Perú', '08000', -13.5320, -71.9675),
('Malecón Cisneros 1456', 'Lima', 'Lima', 'Perú', '15074', -12.1317, -77.0210);

-- Hoteles Demo
INSERT INTO hotel (propietario_id, nombre, descripcion, direccion_id, telefono, email_contacto, estrellas, precio_minimo, precio_maximo, estado, destacado, puntuacion_promedio, total_reviews) VALUES 
(2, 'Hotel Miraflores Grand', 'Hotel de lujo en el corazón de Miraflores con vista al mar', 1, '01-445-5678', 'info@mirafgrand.com', 5, 150.00, 450.00, 'aprobado', TRUE, 4.5, 127),
(2, 'Hotel Centro Trujillo', 'Hotel céntrico ideal para negocios y turismo', 2, '044-234-567', 'contacto@centrotrujillo.com', 4, 80.00, 220.00, 'aprobado', FALSE, 4.2, 89),
(2, 'Cusco Plaza Hotel', 'Hotel colonial cerca de la Plaza de Armas', 3, '084-123-456', 'reservas@cuscoplaza.com', 4, 100.00, 280.00, 'aprobado', TRUE, 4.7, 203);

-- Habitaciones
INSERT INTO habitacion (hotel_id, numero, room_type_id, nombre_corto, precio_base, capacidad, num_camas, metros_cuadrados, estado) VALUES 
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

-- Amenidades de Hoteles
INSERT INTO hotel_amenity (hotel_id, amenity_id, detalle, es_gratuito) VALUES
-- Hotel Miraflores Grand
(1, 1, 'Alta velocidad en todas las áreas', TRUE),
(1, 2, 'Piscina infinity con vista al mar', TRUE),
(1, 3, 'Gimnasio 24 horas', TRUE),
(1, 4, 'Estacionamiento con valet', FALSE),
(1, 5, 'Restaurant gourmet', FALSE),
(1, 6, 'Disponible 24/7', FALSE),
(1, 7, 'Spa premium', FALSE),
(1, 8, 'Con equipamiento completo', TRUE),
-- Hotel Centro Trujillo
(2, 1, 'WiFi gratuito', TRUE),
(2, 3, 'Gimnasio básico', TRUE),
(2, 4, 'Estacionamiento gratuito', TRUE),
(2, 5, 'Desayuno buffet', FALSE),
(2, 8, 'Sala de reuniones', FALSE),
-- Cusco Plaza Hotel
(3, 1, 'WiFi en áreas comunes', TRUE),
(3, 4, 'Estacionamiento limitado', TRUE),
(3, 5, 'Restaurant típico', FALSE),
(3, 11, 'Bar con tragos típicos', FALSE);

-- Imágenes de Hoteles (URLs demo)
INSERT INTO hotel_imagen (hotel_id, url, alt_text, tipo, orden, es_principal) VALUES
(1, '/images/hotels/miraflores-1.jpg', 'Fachada Hotel Miraflores Grand', 'portada', 0, TRUE),
(1, '/images/hotels/miraflores-2.jpg', 'Piscina infinity', 'general', 1, FALSE),
(1, '/images/hotels/miraflores-3.jpg', 'Lobby principal', 'lobby', 2, FALSE),
(2, '/images/hotels/trujillo-1.jpg', 'Hotel Centro Trujillo', 'portada', 0, TRUE),
(3, '/images/hotels/cusco-1.jpg', 'Cusco Plaza Hotel colonial', 'portada', 0, TRUE);

-- Configuración del Sistema
INSERT INTO configuracion_sistema (clave, valor, tipo, descripcion, categoria) VALUES
('impuesto_igv', '18', 'number', 'Porcentaje de IGV para Perú', 'finanzas'),
('moneda_default', 'PEN', 'string', 'Moneda por defecto del sistema', 'finanzas'),
('dias_cancelacion_gratis', '3', 'number', 'Días antes del check-in para cancelación gratuita', 'reservas'),
('max_huespedes_habitacion', '6', 'number', 'Máximo de huéspedes por habitación', 'reservas'),
('email_notificaciones', 'notificaciones@hotel.com', 'string', 'Email para notificaciones del sistema', 'sistema'),
('requiere_verificacion_email', 'true', 'boolean', 'Requiere verificar email al registrarse', 'seguridad'),
('dias_review_disponible', '30', 'number', 'Días después del checkout para dejar review', 'reviews')
ON CONFLICT (clave) DO NOTHING;

-- Cupones
INSERT INTO cupon (codigo, descripcion, tipo_descuento, valor_descuento, monto_minimo, fecha_inicio, fecha_fin, usos_maximos, activo) VALUES
('BIENVENIDA10', 'Descuento de bienvenida del 10%', 'porcentaje', 10.00, 100.00, '2025-01-01', '2025-12-31', 100, TRUE),
('VERANO2025', 'Descuento especial de verano', 'porcentaje', 15.00, 200.00, '2025-12-01', '2026-03-31', 500, TRUE),
('50SOLES', 'Descuento fijo de 50 soles', 'monto_fijo', 50.00, 300.00, '2025-01-01', '2025-12-31', NULL, TRUE)
ON CONFLICT (codigo) DO NOTHING;

-- =============================================
-- FUNCIÓN PARA ACTUALIZAR TIMESTAMP
-- =============================================

CREATE OR REPLACE FUNCTION actualizar_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.actualizado_en = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers para actualizar automáticamente actualizado_en
CREATE TRIGGER trigger_usuario_actualizado BEFORE UPDATE ON usuario FOR EACH ROW EXECUTE FUNCTION actualizar_timestamp();
CREATE TRIGGER trigger_hotel_actualizado BEFORE UPDATE ON hotel FOR EACH ROW EXECUTE FUNCTION actualizar_timestamp();
CREATE TRIGGER trigger_habitacion_actualizado BEFORE UPDATE ON habitacion FOR EACH ROW EXECUTE FUNCTION actualizar_timestamp();
CREATE TRIGGER trigger_reserva_actualizado BEFORE UPDATE ON reserva FOR EACH ROW EXECUTE FUNCTION actualizar_timestamp();
CREATE TRIGGER trigger_pago_actualizado BEFORE UPDATE ON pago FOR EACH ROW EXECUTE FUNCTION actualizar_timestamp();
CREATE TRIGGER trigger_review_actualizado BEFORE UPDATE ON review FOR EACH ROW EXECUTE FUNCTION actualizar_timestamp();

-- =============================================
-- MENSAJE FINAL
-- =============================================

DO $$
BEGIN
    RAISE NOTICE 'Base de datos PostgreSQL creada exitosamente con datos demo';
    RAISE NOTICE '==============================================';
    RAISE NOTICE 'Usuarios creados:';
    RAISE NOTICE '  Admin: admin@hotel.com';
    RAISE NOTICE '  Propietario: propietario@hotel.com';
    RAISE NOTICE '  Cliente: cliente@hotel.com';
    RAISE NOTICE '  Password para todos: password123 (debe hashearse)';
    RAISE NOTICE '==============================================';
END $$;
