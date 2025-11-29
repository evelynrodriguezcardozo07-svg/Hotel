package com.example.PROYECTO.FINAL_WEB.exception;

/**
 * Excepción para conflictos (ej: email duplicado)
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
