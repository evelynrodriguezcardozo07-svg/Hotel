package com.example.PROYECTO.FINAL_WEB.util;

/**
 * Constantes del sistema - Centraliza todos los valores constantes
 */
public class AppConstants {

    // ===== ROLES =====
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_HOST = "host";
    public static final String ROLE_GUEST = "guest";

    // ===== ESTADOS DE USUARIO =====
    public static final String USUARIO_ACTIVO = "activo";
    public static final String USUARIO_INACTIVO = "inactivo";
    public static final String USUARIO_BLOQUEADO = "bloqueado";

    // ===== ESTADOS DE HOTEL =====
    public static final String HOTEL_PENDIENTE = "pendiente";
    public static final String HOTEL_APROBADO = "aprobado";
    public static final String HOTEL_RECHAZADO = "rechazado";
    public static final String HOTEL_INACTIVO = "inactivo";

    // ===== ESTADOS DE HABITACION =====
    public static final String HABITACION_DISPONIBLE = "disponible";
    public static final String HABITACION_MANTENIMIENTO = "mantenimiento";
    public static final String HABITACION_INACTIVO = "inactivo";

    // ===== ESTADOS DE RESERVA =====
    public static final String RESERVA_PENDIENTE = "pendiente";
    public static final String RESERVA_CONFIRMADA = "confirmada";
    public static final String RESERVA_CANCELADA = "cancelada";
    public static final String RESERVA_COMPLETADA = "completada";
    public static final String RESERVA_NO_SHOW = "no_show";

    // ===== ESTADOS DE PAGO =====
    public static final String PAGO_PENDIENTE = "pendiente";
    public static final String PAGO_PROCESANDO = "procesando";
    public static final String PAGO_COMPLETADO = "completado";
    public static final String PAGO_FALLIDO = "fallido";
    public static final String PAGO_REEMBOLSADO = "reembolsado";

    // ===== ESTADOS DE DISPONIBILIDAD =====
    public static final String DISPONIBILIDAD_DISPONIBLE = "disponible";
    public static final String DISPONIBILIDAD_BLOQUEADO = "bloqueado";
    public static final String DISPONIBILIDAD_RESERVADO = "reservado";
    public static final String DISPONIBILIDAD_MANTENIMIENTO = "mantenimiento";

    // ===== MÉTODOS DE PAGO =====
    public static final String METODO_TARJETA = "tarjeta";
    public static final String METODO_PAYPAL = "paypal";
    public static final String METODO_TRANSFERENCIA = "transferencia";
    public static final String METODO_EFECTIVO = "efectivo";
    public static final String METODO_YAPE = "yape";
    public static final String METODO_PLIN = "plin";

    // ===== MONEDAS =====
    public static final String MONEDA_PEN = "PEN";
    public static final String MONEDA_USD = "USD";
    public static final String MONEDA_EUR = "EUR";

    // ===== TIPOS DE IMAGEN =====
    public static final String IMAGEN_PORTADA = "portada";
    public static final String IMAGEN_GENERAL = "general";
    public static final String IMAGEN_LOBBY = "lobby";
    public static final String IMAGEN_EXTERIOR = "exterior";

    // ===== CATEGORÍAS DE AMENIDAD =====
    public static final String AMENIDAD_BASICO = "basico";
    public static final String AMENIDAD_PREMIUM = "premium";
    public static final String AMENIDAD_ENTRETENIMIENTO = "entretenimiento";
    public static final String AMENIDAD_NEGOCIOS = "negocios";

    // ===== TIPOS DE TARIFA =====
    public static final String TARIFA_TEMPORADA = "temporada";
    public static final String TARIFA_PROMOCION = "promocion";
    public static final String TARIFA_EVENTO = "evento";

    // ===== ACCIONES DE AUDITORÍA =====
    public static final String AUDIT_INSERT = "INSERT";
    public static final String AUDIT_UPDATE = "UPDATE";
    public static final String AUDIT_DELETE = "DELETE";

    // ===== PAGINACIÓN =====
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_BY = "id";
    public static final String DEFAULT_SORT_DIRECTION = "ASC";

    // ===== VALIDACIONES =====
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 50;
    public static final int MIN_NOMBRE_LENGTH = 3;
    public static final int MAX_NOMBRE_LENGTH = 150;
    public static final int MIN_HOTEL_NOMBRE_LENGTH = 5;
    public static final int MAX_HOTEL_NOMBRE_LENGTH = 200;

    // ===== PUNTUACIONES =====
    public static final int MIN_PUNTUACION = 1;
    public static final int MAX_PUNTUACION = 5;

    // ===== RESERVAS =====
    public static final int MIN_DIAS_RESERVA = 1;
    public static final int MAX_DIAS_RESERVA = 30;
    public static final int DIAS_CANCELACION_GRATIS = 3;
    public static final int DIAS_REVIEW_DISPONIBLE = 30;

    // ===== FORMATOS DE FECHA =====
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_FORMAT = "HH:mm:ss";

    // ===== MENSAJES DE ERROR =====
    public static final String ERROR_USUARIO_NO_ENCONTRADO = "Usuario no encontrado";
    public static final String ERROR_HOTEL_NO_ENCONTRADO = "Hotel no encontrado";
    public static final String ERROR_HABITACION_NO_ENCONTRADA = "Habitación no encontrada";
    public static final String ERROR_RESERVA_NO_ENCONTRADA = "Reserva no encontrada";
    public static final String ERROR_EMAIL_YA_EXISTE = "El email ya está registrado";
    public static final String ERROR_CREDENCIALES_INVALIDAS = "Credenciales inválidas";
    public static final String ERROR_ACCESO_DENEGADO = "Acceso denegado";
    public static final String ERROR_TOKEN_INVALIDO = "Token inválido o expirado";
    public static final String ERROR_HABITACION_NO_DISPONIBLE = "Habitación no disponible para las fechas seleccionadas";
    public static final String ERROR_RESERVA_NO_CANCELABLE = "La reserva no puede ser cancelada";

    // ===== MENSAJES DE ÉXITO =====
    public static final String SUCCESS_REGISTRO = "Registro exitoso";
    public static final String SUCCESS_LOGIN = "Login exitoso";
    public static final String SUCCESS_HOTEL_CREADO = "Hotel creado exitosamente";
    public static final String SUCCESS_RESERVA_CREADA = "Reserva creada exitosamente";
    public static final String SUCCESS_PAGO_COMPLETADO = "Pago completado exitosamente";
    public static final String SUCCESS_REVIEW_CREADA = "Reseña creada exitosamente";

    // ===== PREFIJOS =====
    public static final String PREFIX_CODIGO_RESERVA = "RES";
    public static final String PREFIX_CODIGO_PAGO = "PAG";

    private AppConstants() {
        throw new IllegalStateException("Utility class");
    }
}
