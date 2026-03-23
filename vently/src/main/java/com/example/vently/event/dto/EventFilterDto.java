package com.example.vently.event.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for filtering events
 * Requirements: 4.2, 4.3, 4.6
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFilterDto {
    
    // Date range filter
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Location filter
    private String location;
    
    // Payment range filter
    private BigDecimal minPayment;
    private BigDecimal maxPayment;
    
    // Category filter
    private String category;
    
    // Full-text search
    private String searchQuery;
    
    // Sort options: "relevance", "date", "payment"
    private String sortBy;
}
