package br.com.tourapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tours")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TourEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Tour name is required")
    @Size(min = 3, max = 100, message = "Tour name must be between 3 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    @Column(nullable = false, length = 1000)
    private String description;

    @NotBlank(message = "Destination is required")
    @Size(min = 2, max = 100, message = "Destination must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String destination;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    @Max(value = 365, message = "Duration cannot exceed 365 days")
    @Column(nullable = false)
    private Integer durationDays;

    @NotNull(message = "Maximum participants is required")
    @Min(value = 1, message = "Maximum participants must be at least 1")
    @Max(value = 100, message = "Maximum participants cannot exceed 100")
    @Column(nullable = false)
    private Integer maxParticipants;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TourStatus status = TourStatus.ACTIVE;

    @Size(max = 255, message = "Image URL cannot exceed 255 characters")
    private String imageUrl;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum TourStatus {
        ACTIVE, INACTIVE, CANCELLED, FULL
    }
}