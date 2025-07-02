package br.com.tourapp.exception;

/**
 * Exceção para recursos não encontrados
 * Exemplo: "Excursão não encontrada", "Cliente não encontrado", etc.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}