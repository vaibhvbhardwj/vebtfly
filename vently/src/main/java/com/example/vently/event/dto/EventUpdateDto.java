package com.example.vently.event.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing event
 * Requirements: 3.4, 26.1, 26.5, 26.6
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventUpdateDto {
    
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;
    
    @Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters")
    private String description;
    
    @Size(min = 3, max = 200, message = "Location must be between 3 and 200 characters")
    private String location;
    
    @FutureOrPresent(message = "Event date must be today or in the future")
    private LocalDate date;
    
    private LocalTime time;
    
    @Min(value = 1, message = "At least 1 volunteer is required")
    private Integer requiredVolunteers;
    
    // Payment is read-only and cannot be updated
    // @DecimalMin(value = "0.0", inclusive = false, message = "Payment must be greater than 0")
    // private BigDecimal paymentPerVolunteer;
    
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;
    
    @FutureOrPresent(message = "Application deadline must be today or in the future")
    private LocalDate applicationDeadline;
    
    @Size(max = 10000000, message = "Image data must not exceed 10MB")
    private String imageUrl;
}
