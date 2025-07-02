package br.com.tourapp.exception;

/**
 * Exceção para erros de validação customizados
 * Exemplo: validações complexas que não são cobertas por Bean Validation
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}