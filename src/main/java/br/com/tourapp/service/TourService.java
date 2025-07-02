package br.com.tourapp.service;


import br.com.tourapp.dto.TourDTO;
import br.com.tourapp.entity.TourEntity;
import br.com.tourapp.exception.DuplicateResourceException;
import br.com.tourapp.exception.ResourceNotFoundException;
import br.com.tourapp.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TourService {

    private final TourRepository tourRepository;

    public TourDTO.Response createTour(TourDTO.Request request) {
        log.info("Creating new tour with name: {}", request.getName());

        // Check if tour name already exists
        if (tourRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Tour with name '" + request.getName() + "' already exists");
        }

        TourEntity tour = mapToEntity(request);
        TourEntity savedTour = tourRepository.save(tour);

        log.info("Tour created successfully with ID: {}", savedTour.getId());
        return mapToResponse(savedTour);
    }

    @Transactional(readOnly = true)
    public TourDTO.Response getTourById(UUID id) {
        log.info("Fetching tour with ID: {}", id);

        TourEntity tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with ID: " + id));

        return mapToResponse(tour);
    }

    @Transactional(readOnly = true)
    public Page<TourDTO.Summary> getAllTours(Pageable pageable) {
        log.info("Fetching all tours with pagination: {}", pageable);

        Page<TourEntity> tours = tourRepository.findAll(pageable);
        return tours.map(this::mapToSummary);
    }

    @Transactional(readOnly = true)
    public Page<TourDTO.Summary> searchTours(
            String name,
            String destination,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minDays,
            Integer maxDays,
            TourEntity.TourStatus status,
            Pageable pageable) {

        log.info("Searching tours with filters - name: {}, destination: {}, status: {}",
                name, destination, status);

        Page<TourEntity> tours = tourRepository.findToursWithFilters(
                name, destination, minPrice, maxPrice, minDays, maxDays, status, pageable);

        return tours.map(this::mapToSummary);
    }

    public TourDTO.Response updateTour(UUID id, TourDTO.Request request) {
        log.info("Updating tour with ID: {}", id);

        TourEntity existingTour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with ID: " + id));

        // Check if the new name conflicts with existing tours (excluding current tour)
        if (!existingTour.getName().equalsIgnoreCase(request.getName()) &&
                tourRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Tour with name '" + request.getName() + "' already exists");
        }

        updateTourFields(existingTour, request);
        TourEntity updatedTour = tourRepository.save(existingTour);

        log.info("Tour updated successfully with ID: {}", updatedTour.getId());
        return mapToResponse(updatedTour);
    }

    public void deleteTour(UUID id) {
        log.info("Deleting tour with ID: {}", id);

        if (!tourRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tour not found with ID: " + id);
        }

        tourRepository.deleteById(id);
        log.info("Tour deleted successfully with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public Page<TourDTO.Summary> getToursByStatus(TourEntity.TourStatus status, Pageable pageable) {
        log.info("Fetching tours with status: {}", status);

        Page<TourEntity> tours = tourRepository.findByStatus(status, pageable);
        return tours.map(this::mapToSummary);
    }

    @Transactional(readOnly = true)
    public List<String> getPopularDestinations() {
        log.info("Fetching popular destinations");

        return tourRepository.findPopularDestinations()
                .stream()
                .map(result -> (String) result[0])
                .toList();
    }

    // Mapping methods
    private TourEntity mapToEntity(TourDTO.Request request) {
        TourEntity tour = new TourEntity();
        tour.setName(request.getName());
        tour.setDescription(request.getDescription());
        tour.setDestination(request.getDestination());
        tour.setPrice(request.getPrice());
        tour.setDurationDays(request.getDurationDays());
        tour.setMaxParticipants(request.getMaxParticipants());
        tour.setStatus(request.getStatus());
        tour.setImageUrl(request.getImageUrl());
        return tour;
    }

    private TourDTO.Response mapToResponse(TourEntity tour) {
        return new TourDTO.Response(
                tour.getId(),
                tour.getName(),
                tour.getDescription(),
                tour.getDestination(),
                tour.getPrice(),
                tour.getDurationDays(),
                tour.getMaxParticipants(),
                tour.getStatus(),
                tour.getImageUrl(),
                tour.getCreatedAt(),
                tour.getUpdatedAt()
        );
    }

    private TourDTO.Summary mapToSummary(TourEntity tour) {
        return new TourDTO.Summary(
                tour.getId(),
                tour.getName(),
                tour.getDestination(),
                tour.getPrice(),
                tour.getDurationDays(),
                tour.getStatus(),
                tour.getImageUrl()
        );
    }

    private void updateTourFields(TourEntity tour, TourDTO.Request request) {
        tour.setName(request.getName());
        tour.setDescription(request.getDescription());
        tour.setDestination(request.getDestination());
        tour.setPrice(request.getPrice());
        tour.setDurationDays(request.getDurationDays());
        tour.setMaxParticipants(request.getMaxParticipants());
        tour.setStatus(request.getStatus());
        tour.setImageUrl(request.getImageUrl());
    }
}
