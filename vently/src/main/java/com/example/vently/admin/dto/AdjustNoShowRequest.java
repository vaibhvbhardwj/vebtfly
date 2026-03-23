package com.example.vently.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustNoShowRequest {
    
    @NotNull(message = "New no-show count is required")
    @Min(value = 0, message = "No-show count cannot be negative")
    private Integer newCount;
    
    private String reason;
}
