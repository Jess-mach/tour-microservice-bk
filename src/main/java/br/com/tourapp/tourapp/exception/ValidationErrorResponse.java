package br.com.tourapp.tourapp.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Classe para erros de validação com detalhes dos campos
 */
public class ValidationErrorResponse extends ErrorResponse {

    private Map<String, String> errors;

    public ValidationErrorResponse() {}

    public ValidationErrorResponse(int status, String message, String path,
                                   LocalDateTime timestamp, Map<String, String> errors) {
        super(status, message, path, timestamp);
        this.errors = errors;
    }

    public Map<String, String> getErrors() { return errors; }
    public void setErrors(Map<String, String> errors) { this.errors = errors; }
}
