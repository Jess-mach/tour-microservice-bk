package br.com.tourapp.repository;

import br.com.tourapp.entity.TourEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface TourRepository extends JpaRepository<TourEntity, UUID> {

    // Find tours by status
    Page<TourEntity> findByStatus(TourEntity.TourStatus status, Pageable pageable);

    // Find tours by destination (case insensitive)
    Page<TourEntity> findByDestinationContainingIgnoreCase(String destination, Pageable pageable);

    // Find tours by price range
    Page<TourEntity> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // Find tours by duration range
    Page<TourEntity> findByDurationDaysBetween(Integer minDays, Integer maxDays, Pageable pageable);

    // Find tours by name containing (case insensitive)
    Page<TourEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Complex search query
    @Query("SELECT t FROM TourEntity t WHERE " +
            "(:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:destination IS NULL OR LOWER(t.destination) LIKE LOWER(CONCAT('%', :destination, '%'))) AND " +
            "(:minPrice IS NULL OR t.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR t.price <= :maxPrice) AND " +
            "(:minDays IS NULL OR t.durationDays >= :minDays) AND " +
            "(:maxDays IS NULL OR t.durationDays <= :maxDays) AND " +
            "(:status IS NULL OR t.status = :status)")
    Page<TourEntity> findToursWithFilters(
            @Param("name") String name,
            @Param("destination") String destination,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minDays") Integer minDays,
            @Param("maxDays") Integer maxDays,
            @Param("status") TourEntity.TourStatus status,
            Pageable pageable
    );

    // Find popular destinations
    @Query("SELECT t.destination, COUNT(t) as tourCount FROM TourEntity t " +
            "WHERE t.status = 'ACTIVE' " +
            "GROUP BY t.destination " +
            "ORDER BY COUNT(t) DESC")
    List<Object[]> findPopularDestinations();

    // Check if tour name exists (for validation)
    boolean existsByNameIgnoreCase(String name);

    // Find tours by status (list version)
    List<TourEntity> findByStatusOrderByCreatedAtDesc(TourEntity.TourStatus status);
}