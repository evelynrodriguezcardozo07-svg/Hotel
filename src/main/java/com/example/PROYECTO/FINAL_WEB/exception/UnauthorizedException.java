package com.example.PROYECTO.FINAL_WEB.exception;

/**
 * Excepción para errores de autenticación
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
