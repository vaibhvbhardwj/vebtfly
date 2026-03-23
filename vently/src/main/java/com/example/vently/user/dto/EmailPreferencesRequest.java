package com.example.vently.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailPreferencesRequest {

    @NotNull(message = "Email notifications enabled flag is required")
    private Boolean emailNotificationsEnabled;

    @NotNull(message = "Application status notification preference is required")
    private Boolean notifyOnApplicationStatus;

    @NotNull(message = "Event cancellation notification preference is required")
    private Boolean notifyOnEventCancellation;

    @NotNull(message = "Payment notification preference is required")
    private Boolean notifyOnPayment;

    @NotNull(message = "Dispute resolution notification preference is required")
    private Boolean notifyOnDisputeResolution;
}
