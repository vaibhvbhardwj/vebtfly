package com.example.vently.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for submitting a new application
 * Requirements: 5.1, 26.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDto {
    
    @NotNull(message = "Event ID is required")
    private Long eventId;
}
