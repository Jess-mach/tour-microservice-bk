package br.com.tourapp.exception;

/**
 * Exceção para regras de negócio violadas
 * Exemplo: "Excursão já está lotada", "Cliente já inscrito", etc.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

