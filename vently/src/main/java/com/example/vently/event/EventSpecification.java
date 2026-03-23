package com.example.vently.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.example.vently.event.dto.EventFilterDto;

import jakarta.persistence.criteria.Predicate;

/**
 * Specification builder for Event filtering
 * Requirements: 4.2, 4.3, 29.5
 */
public class EventSpecification {

    /**
     * Build specification from filter DTO
     * Combines multiple filter criteria using AND logic
     */
    public static Specification<Event> buildSpecification(EventFilterDto filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Always filter for PUBLISHED events only
            predicates.add(criteriaBuilder.equal(root.get("status"), EventStatus.PUBLISHED));
            
            // Date range filter
            if (filters.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), filters.getStartDate()));
            }
            if (filters.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), filters.getEndDate()));
            }
            
            // Location filter (case-insensitive partial match)
            if (filters.getLocation() != null && !filters.getLocation().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("location")),
                    "%" + filters.getLocation().toLowerCase() + "%"
                ));
            }
            
            // Payment range filter
            if (filters.getMinPayment() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("paymentPerVolunteer"), 
                    filters.getMinPayment()
                ));
            }
            if (filters.getMaxPayment() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("paymentPerVolunteer"), 
                    filters.getMaxPayment()
                ));
            }
            
            // Category filter (exact match, case-insensitive)
            if (filters.getCategory() != null && !filters.getCategory().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("category")),
                    filters.getCategory().toLowerCase()
                ));
            }
            
            // Full-text search on title and description
            if (filters.getSearchQuery() != null && !filters.getSearchQuery().trim().isEmpty()) {
                String searchPattern = "%" + filters.getSearchQuery().toLowerCase() + "%";
                Predicate titleMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")),
                    searchPattern
                );
                Predicate descriptionMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    searchPattern
                );
                predicates.add(criteriaBuilder.or(titleMatch, descriptionMatch));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
