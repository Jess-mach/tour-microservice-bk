package br.com.tourapp.service;

import br.com.tourapp.dto.TourDTO;
import br.com.tourapp.entity.TourEntity;
import br.com.tourapp.exception.DuplicateResourceException;
import br.com.tourapp.exception.ResourceNotFoundException;
import br.com.tourapp.repository.TourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourServiceTest {

    @Mock
    private TourRepository tourRepository;

    @InjectMocks
    private TourService tourService;

    private TourEntity tour;
    private TourDTO.Request tourRequest;
    private UUID tourId;

    @BeforeEach
    void setUp() {
        tourId = UUID.randomUUID();

        tour = new TourEntity();
        tour.setId(tourId);
        tour.setName("Amazing Paris Tour");
        tour.setDescription("Explore the beautiful city of Paris");
        tour.setDestination("Paris, France");
        tour.setPrice(new BigDecimal("999.99"));
        tour.setDurationDays(7);
        tour.setMaxParticipants(20);
        tour.setStatus(TourEntity.TourStatus.ACTIVE);
        tour.setImageUrl("https://example.com/paris.jpg");
        tour.setCreatedAt(LocalDateTime.now());
        tour.setUpdatedAt(LocalDateTime.now());

        tourRequest = new TourDTO.Request();
        tourRequest.setName("Amazing Paris Tour");
        tourRequest.setDescription("Explore the beautiful city of Paris");
        tourRequest.setDestination("Paris, France");
        tourRequest.setPrice(new BigDecimal("999.99"));
        tourRequest.setDurationDays(7);
        tourRequest.setMaxParticipants(20);
        tourRequest.setStatus(TourEntity.TourStatus.ACTIVE);
        tourRequest.setImageUrl("https://example.com/paris.jpg");
    }

    @Test
    void createTour_Success() {
        // Given
        when(tourRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(tourRepository.save(any(TourEntity.class))).thenReturn(tour);

        // When
        TourDTO.Response result = tourService.createTour(tourRequest);

        // Then
        assertNotNull(result);
        assertEquals(tour.getName(), result.getName());
        assertEquals(tour.getDestination(), result.getDestination());
        assertEquals(tour.getPrice(), result.getPrice());
        verify(tourRepository).existsByNameIgnoreCase(tourRequest.getName());
        verify(tourRepository).save(any(TourEntity.class));
    }

    @Test
    void createTour_DuplicateName_ThrowsException() {
        // Given
        when(tourRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);

        // When & Then
        assertThrows(DuplicateResourceException.class,
                () -> tourService.createTour(tourRequest));
        verify(tourRepository).existsByNameIgnoreCase(tourRequest.getName());
        verify(tourRepository, never()).save(any(TourEntity.class));
    }

    @Test
    void getTourById_Success() {
        // Given
        when(tourRepository.findById(tourId)).thenReturn(Optional.of(tour));

        // When
        TourDTO.Response result = tourService.getTourById(tourId);

        // Then
        assertNotNull(result);
        assertEquals(tour.getId(), result.getId());
        assertEquals(tour.getName(), result.getName());
        verify(tourRepository).findById(tourId);
    }

    @Test
    void getTourById_NotFound_ThrowsException() {
        // Given
        when(tourRepository.findById(tourId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> tourService.getTourById(tourId));
        verify(tourRepository).findById(tourId);
    }

    @Test
    void getAllTours_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<TourEntity> tourPage = new PageImpl<>(Arrays.asList(tour));
        when(tourRepository.findAll(pageable)).thenReturn(tourPage);

        // When
        Page<TourDTO.Summary> result = tourService.getAllTours(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(tour.getName(), result.getContent().get(0).getName());
        verify(tourRepository).findAll(pageable);
    }

    @Test
    void updateTour_Success() {
        // Given
        when(tourRepository.findById(tourId)).thenReturn(Optional.of(tour));
       // when(tourRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(tourRepository.save(any(TourEntity.class))).thenReturn(tour);

        // When
        TourDTO.Response result = tourService.updateTour(tourId, tourRequest);

        // Then
        assertNotNull(result);
        assertEquals(tour.getName(), result.getName());
        verify(tourRepository).findById(tourId);
        verify(tourRepository).save(any(TourEntity.class));
    }

    @Test
    void updateTour_NotFound_ThrowsException() {
        // Given
        when(tourRepository.findById(tourId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> tourService.updateTour(tourId, tourRequest));
        verify(tourRepository).findById(tourId);
        verify(tourRepository, never()).save(any(TourEntity.class));
    }

    @Test
    void deleteTour_Success() {
        // Given
        when(tourRepository.existsById(tourId)).thenReturn(true);

        // When
        tourService.deleteTour(tourId);

        // Then
        verify(tourRepository).existsById(tourId);
        verify(tourRepository).deleteById(tourId);
    }

    @Test
    void deleteTour_NotFound_ThrowsException() {
        // Given
        when(tourRepository.existsById(tourId)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> tourService.deleteTour(tourId));
        verify(tourRepository).existsById(tourId);
        verify(tourRepository, never()).deleteById(any());
    }

    @Test
    void getToursByStatus_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<TourEntity> tourPage = new PageImpl<>(Arrays.asList(tour));
        when(tourRepository.findByStatus(TourEntity.TourStatus.ACTIVE, pageable)).thenReturn(tourPage);

        // When
        Page<TourDTO.Summary> result = tourService.getToursByStatus(TourEntity.TourStatus.ACTIVE, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(tourRepository).findByStatus(TourEntity.TourStatus.ACTIVE, pageable);
    }
}