package br.com.tourapp.controller;

import br.com.tourapp.dto.TourDTO;
import br.com.tourapp.entity.TourEntity;
import br.com.tourapp.entity.SecurityUser;
import br.com.tourapp.service.TourService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tours")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tours", description = "Tour management API")
@SecurityRequirement(name = "bearerAuth")
public class TourController {

    private final TourService tourService;

    @PostMapping
    @Operation(summary = "Create a new tour", description = "Creates a new tour with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tour created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Tour with same name already exists")
    })
    public ResponseEntity<TourDTO.Response> createTour(@Valid @RequestBody TourDTO.Request request) {
        log.info("POST /api/v1/tours - Creating tour: {}", request.getName());

        TourDTO.Response response = tourService.createTour(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tour by ID", description = "Retrieves a specific tour by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tour found"),
            @ApiResponse(responseCode = "404", description = "Tour not found")
    })
    public ResponseEntity<TourDTO.Response> getTourById(
            @Parameter(description = "Tour ID") @PathVariable UUID id) {
        log.info("GET /api/v1/tours/{} - Fetching tour", id);

        TourDTO.Response response = tourService.getTourById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all tours", description = "Retrieves a paginated list of all tours")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tours retrieved successfully")
    })
    public ResponseEntity<Page<TourDTO.Summary>> getAllTours(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal SecurityUser securityUser ) {

        log.info("GET /api/v1/tours - Fetching tours, page: {}, size: {}", page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TourDTO.Summary> tours = tourService.getAllTours(pageable);
        return ResponseEntity.ok(tours);
    }

    @GetMapping("/search")
    @Operation(summary = "Search tours", description = "Search tours with various filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<Page<TourDTO.Summary>> searchTours(
            @Parameter(description = "Tour name filter") @RequestParam(required = false) String name,
            @Parameter(description = "Destination filter") @RequestParam(required = false) String destination,
            @Parameter(description = "Minimum price") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Minimum duration days") @RequestParam(required = false) Integer minDays,
            @Parameter(description = "Maximum duration days") @RequestParam(required = false) Integer maxDays,
            @Parameter(description = "Tour status") @RequestParam(required = false) TourEntity.TourStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("GET /api/v1/tours/search - Searching tours with filters");

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TourDTO.Summary> tours = tourService.searchTours(
                name, destination, minPrice, maxPrice, minDays, maxDays, status, pageable);

        return ResponseEntity.ok(tours);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update tour", description = "Updates an existing tour")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tour updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Tour not found"),
            @ApiResponse(responseCode = "409", description = "Tour with same name already exists")
    })
    public ResponseEntity<TourDTO.Response> updateTour(
            @Parameter(description = "Tour ID") @PathVariable UUID id,
            @Valid @RequestBody TourDTO.Request request) {
        log.info("PUT /api/v1/tours/{} - Updating tour", id);

        TourDTO.Response response = tourService.updateTour(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete tour", description = "Deletes a tour by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tour deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Tour not found")
    })
    public ResponseEntity<Void> deleteTour(
            @Parameter(description = "Tour ID") @PathVariable UUID id) {
        log.info("DELETE /api/v1/tours/{} - Deleting tour", id);

        tourService.deleteTour(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get tours by status", description = "Retrieves tours filtered by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tours retrieved successfully")
    })
    public ResponseEntity<Page<TourDTO.Summary>> getToursByStatus(
            @Parameter(description = "Tour status") @PathVariable TourEntity.TourStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("GET /api/v1/tours/status/{} - Fetching tours by status", status);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TourDTO.Summary> tours = tourService.getToursByStatus(status, pageable);
        return ResponseEntity.ok(tours);
    }

    @GetMapping("/destinations/popular")
    @Operation(summary = "Get popular destinations", description = "Retrieves list of popular tour destinations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Popular destinations retrieved successfully")
    })
    public ResponseEntity<List<String>> getPopularDestinations() {
        log.info("GET /api/v1/tours/destinations/popular - Fetching popular destinations");

        List<String> destinations = tourService.getPopularDestinations();
        return ResponseEntity.ok(destinations);
    }
}