package com.example.vently.event.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new event
 * Requirements: 3.1, 3.2, 26.1, 26.5, 26.6
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventCreateDto {
    
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters")
    private String description;
    
    @NotBlank(message = "Location is required")
    @Size(min = 3, max = 200, message = "Location must be between 3 and 200 characters")
    private String location;
    
    @NotNull(message = "Date is required")
    @Future(message = "Event date must be in the future")
    private LocalDate date;
    
    @NotNull(message = "Time is required")
    private LocalTime time;
    
    @NotNull(message = "Required volunteers count is required")
    @Min(value = 1, message = "At least 1 volunteer is required")
    private Integer requiredVolunteers;

    @Min(value = 0, message = "Cannot be negative")
    @Builder.Default
    private Integer requiredMaleVolunteers = 0;

    @Min(value = 0, message = "Cannot be negative")
    @Builder.Default
    private Integer requiredFemaleVolunteers = 0;
    
    @NotNull(message = "Payment per volunteer is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Payment must be greater than 0")
    private BigDecimal paymentPerVolunteer;

    @DecimalMin(value = "0.0", inclusive = true, message = "Payment cannot be negative")
    private BigDecimal paymentPerMaleVolunteer;

    @DecimalMin(value = "0.0", inclusive = true, message = "Payment cannot be negative")
    private BigDecimal paymentPerFemaleVolunteer;
    
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;
    
    @NotNull(message = "Application deadline is required")
    @Future(message = "Application deadline must be in the future")
    private LocalDate applicationDeadline;

    @Size(max = 10000000, message = "Image data must not exceed 10MB")
    private String imageUrl;
}
