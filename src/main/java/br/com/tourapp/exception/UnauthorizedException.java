package br.com.tourapp.exception;

/**
 * Exceção para problemas de autenticação
 * Exemplo: "Token inválido", "Credenciais incorretas", etc.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
