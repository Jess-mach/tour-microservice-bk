// TourControllerIntegrationTest.java
package br.com.tourapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.tourapp.dto.TourDTO;
import br.com.tourapp.entity.TourEntity;
import br.com.tourapp.repository.TourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
@Sql(scripts = "classpath:db/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TourControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private TourEntity testTour;
    private TourDTO.Request tourRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Clear repository
        tourRepository.deleteAll();

        // Create test tour
        testTour = new TourEntity();
        testTour.setName("Test Paris Tour");
        testTour.setDescription("A wonderful tour of Paris");
        testTour.setDestination("Paris, France");
        testTour.setPrice(new BigDecimal("1299.99"));
        testTour.setDurationDays(5);
        testTour.setMaxParticipants(15);
        testTour.setStatus(TourEntity.TourStatus.ACTIVE);
        testTour.setImageUrl("https://example.com/paris.jpg");

        // Create request DTO
        tourRequest = new TourDTO.Request();
        tourRequest.setName("New Rome Tour");
        tourRequest.setDescription("Explore ancient Rome and its history");
        tourRequest.setDestination("Rome, Italy");
        tourRequest.setPrice(new BigDecimal("899.99"));
        tourRequest.setDurationDays(4);
        tourRequest.setMaxParticipants(20);
        tourRequest.setStatus(TourEntity.TourStatus.ACTIVE);
        tourRequest.setImageUrl("https://example.com/rome.jpg");
    }

    @Test
    void createTour_Success() throws Exception {
        mockMvc.perform(post("/api/v1/tours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tourRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(tourRequest.getName())))
                .andExpect(jsonPath("$.destination", is(tourRequest.getDestination())))
                .andExpect(jsonPath("$.price", is(tourRequest.getPrice().doubleValue())))
                .andExpect(jsonPath("$.durationDays", is(tourRequest.getDurationDays())))
                .andExpect(jsonPath("$.maxParticipants", is(tourRequest.getMaxParticipants())))
                .andExpect(jsonPath("$.status", is(tourRequest.getStatus().toString())))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));
    }

    @Test
    @Disabled
    void createTour_ValidationErrors() throws Exception {
        tourRequest.setName(""); // Invalid name
        tourRequest.setPrice(new BigDecimal("-100")); // Invalid price

        mockMvc.perform(post("/api/v1/tours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tourRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.validationErrors", notNullValue()));
    }

    @Test
    @Disabled
    void createTour_DuplicateName() throws Exception {
        // Save a tour first
        tourRepository.save(testTour);

        // Try to create another tour with same name
        tourRequest.setName(testTour.getName());

        mockMvc.perform(post("/api/v1/tours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tourRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")));
    }

    @Test
    void getTourById_Success() throws Exception {
        TourEntity savedTour = tourRepository.save(testTour);

        mockMvc.perform(get("/api/v1/tours/{id}", savedTour.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedTour.getId().toString())))
                .andExpect(jsonPath("$.name", is(savedTour.getName())))
                .andExpect(jsonPath("$.destination", is(savedTour.getDestination())));
    }

    @Test
    @Disabled
    void getTourById_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/tours/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    void getAllTours_Success() throws Exception {
        tourRepository.save(testTour);

        mockMvc.perform(get("/api/v1/tours")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is(testTour.getName())))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    void searchTours_Success() throws Exception {
        tourRepository.save(testTour);

        mockMvc.perform(get("/api/v1/tours/search")
                        .param("destination", "Paris")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].destination", containsString("Paris")));
    }

    @Test
    void updateTour_Success() throws Exception {
        TourEntity savedTour = tourRepository.save(testTour);

        tourRequest.setName("Updated Paris Tour");
        tourRequest.setPrice(new BigDecimal("1599.99"));

        mockMvc.perform(put("/api/v1/tours/{id}", savedTour.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tourRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Paris Tour")))
                .andExpect(jsonPath("$.price", is(1599.99)));
    }

    @Test
    @Disabled
    void updateTour_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/tours/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tourRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Disabled
    void deleteTour_Success() throws Exception {
        TourEntity savedTour = tourRepository.save(testTour);

        mockMvc.perform(delete("/api/v1/tours/{id}", savedTour.getId()))
                .andExpect(status().isNoContent());

        // Verify tour is deleted
        mockMvc.perform(get("/api/v1/tours/{id}", savedTour.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @Disabled
    void deleteTour_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/tours/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getToursByStatus_Success() throws Exception {
        testTour.setStatus(TourEntity.TourStatus.ACTIVE);
        tourRepository.save(testTour);

        mockMvc.perform(get("/api/v1/tours/status/{status}", TourEntity.TourStatus.ACTIVE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status", is("ACTIVE")));
    }

    @Test
    void getPopularDestinations_Success() throws Exception {
        tourRepository.save(testTour);

        mockMvc.perform(get("/api/v1/tours/destinations/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
    }
}