package br.com.tourapp.dto;

import br.com.tourapp.entity.TourEntity;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TourDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "Tour name is required")
        @Size(min = 3, max = 100, message = "Tour name must be between 3 and 100 characters")
        private String name;

        @NotBlank(message = "Description is required")
        @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
        private String description;

        @NotBlank(message = "Destination is required")
        @Size(min = 2, max = 100, message = "Destination must be between 2 and 100 characters")
        private String destination;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        private BigDecimal price;

        @NotNull(message = "Duration is required")
        @Min(value = 1, message = "Duration must be at least 1 day")
        @Max(value = 365, message = "Duration cannot exceed 365 days")
        private Integer durationDays;

        @NotNull(message = "Maximum participants is required")
        @Min(value = 1, message = "Maximum participants must be at least 1")
        @Max(value = 100, message = "Maximum participants cannot exceed 100")
        private Integer maxParticipants;

        private TourEntity.TourStatus status = TourEntity.TourStatus.ACTIVE;

        @Size(max = 255, message = "Image URL cannot exceed 255 characters")
        private String imageUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private String name;
        private String description;
        private String destination;
        private BigDecimal price;
        private Integer durationDays;
        private Integer maxParticipants;
        private TourEntity.TourStatus status;
        private String imageUrl;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private UUID id;
        private String name;
        private String destination;
        private BigDecimal price;
        private Integer durationDays;
        private TourEntity.TourStatus status;
        private String imageUrl;
    }
}