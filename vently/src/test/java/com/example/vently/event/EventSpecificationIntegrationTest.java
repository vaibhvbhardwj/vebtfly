package com.example.vently.event;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import com.example.vently.event.dto.EventFilterDto;

/**
 * Unit tests for EventSpecification
 * Requirements: 4.2, 4.3, 29.5
 */
class EventSpecificationIntegrationTest {

    @Test
    void testBuildSpecification_WithAllFilters() {
        // Arrange
        EventFilterDto filters = EventFilterDto.builder()
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .location("New York")
            .minPayment(new BigDecimal("50.00"))
            .maxPayment(new BigDecimal("100.00"))
            .category("Music")
            .searchQuery("festival")
            .build();

        // Act
        Specification<Event> spec = EventSpecification.buildSpecification(filters);

        // Assert
        assertNotNull(spec);
    }

    @Test
    void testBuildSpecification_WithNoFilters() {
        // Arrange
        EventFilterDto filters = new EventFilterDto();

        // Act
        Specification<Event> spec = EventSpecification.buildSpecification(filters);

        // Assert
        assertNotNull(spec);
    }

    @Test
    void testBuildSpecification_WithOnlyLocationFilter() {
        // Arrange
        EventFilterDto filters = EventFilterDto.builder()
            .location("San Francisco")
            .build();

        // Act
        Specification<Event> spec = EventSpecification.buildSpecification(filters);

        // Assert
        assertNotNull(spec);
    }

    @Test
    void testBuildSpecification_WithOnlyPaymentFilter() {
        // Arrange
        EventFilterDto filters = EventFilterDto.builder()
            .minPayment(new BigDecimal("50.00"))
            .maxPayment(new BigDecimal("100.00"))
            .build();

        // Act
        Specification<Event> spec = EventSpecification.buildSpecification(filters);

        // Assert
        assertNotNull(spec);
    }

    @Test
    void testBuildSpecification_WithOnlySearchQuery() {
        // Arrange
        EventFilterDto filters = EventFilterDto.builder()
            .searchQuery("technology")
            .build();

        // Act
        Specification<Event> spec = EventSpecification.buildSpecification(filters);

        // Assert
        assertNotNull(spec);
    }

    @Test
    void testBuildSpecification_WithEmptyStrings() {
        // Arrange
        EventFilterDto filters = EventFilterDto.builder()
            .location("")
            .category("")
            .searchQuery("")
            .build();

        // Act
        Specification<Event> spec = EventSpecification.buildSpecification(filters);

        // Assert
        assertNotNull(spec);
    }
}
