package com.example.vently.dispute.dto;

import java.time.LocalDateTime;

import com.example.vently.dispute.DisputeStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisputeResponseDto {
    
    private Long id;
    
    private Long eventId;
    private String eventTitle;
    
    private Long raisedById;
    private String raisedByName;
    
    private Long againstUserId;
    private String againstUserName;
    
    private DisputeStatus status;
    
    private String description;
    
    private String[] evidenceUrls;
    
    private String resolution;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime resolvedAt;
    
    private Long resolvedById;
    private String resolvedByName;
    
    private LocalDateTime updatedAt;
}