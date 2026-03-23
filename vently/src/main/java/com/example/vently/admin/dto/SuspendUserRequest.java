package com.example.vently.admin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuspendUserRequest {
    
    @NotNull(message = "Suspension duration in days is required")
    @Positive(message = "Suspension duration must be positive")
    private Integer durationInDays;
    
    private String reason;
}
