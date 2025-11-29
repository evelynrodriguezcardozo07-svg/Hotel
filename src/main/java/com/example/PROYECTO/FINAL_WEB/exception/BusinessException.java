package com.example.PROYECTO.FINAL_WEB.exception;

/**
 * Excepción para errores de negocio
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
