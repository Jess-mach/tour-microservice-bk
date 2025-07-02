package br.com.tourapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API Response wrapper")
public class ApiResponse<T> {

    @Schema(description = "Indicates if the request was successful", example = "true")
    private boolean success;

    @Schema(description = "Response message", example = "Operation completed successfully")
    private String message;

    @Schema(description = "Response data")
    private T data;

    @Schema(description = "Error details (only present when success is false)")
    private ErrorDetail error;

    @Schema(description = "Response timestamp", example = "2025-01-15T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Request path", example = "/api/v1/tours")
    private String path;

    @Schema(description = "HTTP status code", example = "200")
    private int status;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Error details")
    public static class ErrorDetail {

        @Schema(description = "Error code", example = "TOUR_NOT_FOUND")
        private String code;

        @Schema(description = "Error message", example = "Tour not found with ID: 123")
        private String message;

        @Schema(description = "Field-specific validation errors")
        private Object details;
    }

    // Helper methods for creating responses
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Operation completed successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .status(200)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .status(200)
                .build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Resource created successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .status(201)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, int status) {
        return ApiResponse.<T>builder()
                .success(false)
                .message("Operation failed")
                .error(ErrorDetail.builder()
                        .message(message)
                        .build())
                .timestamp(LocalDateTime.now())
                .status(status)
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message, int status) {
        return ApiResponse.<T>builder()
                .success(false)
                .message("Operation failed")
                .error(ErrorDetail.builder()
                        .code(code)
                        .message(message)
                        .build())
                .timestamp(LocalDateTime.now())
                .status(status)
                .build();
    }

    public static <T> ApiResponse<T> validationError(String message, Object details) {
        return ApiResponse.<T>builder()
                .success(false)
                .message("Validation failed")
                .error(ErrorDetail.builder()
                        .code("VALIDATION_ERROR")
                        .message(message)
                        .details(details)
                        .build())
                .timestamp(LocalDateTime.now())
                .status(400)
                .build();
    }
}