package com.example.vently.user.dto;

import com.example.vently.user.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailPreferencesResponse {

    private Boolean emailNotificationsEnabled;
    private Boolean notifyOnApplicationStatus;
    private Boolean notifyOnEventCancellation;
    private Boolean notifyOnPayment;
    private Boolean notifyOnDisputeResolution;

    public static EmailPreferencesResponse fromUser(User user) {
        return EmailPreferencesResponse.builder()
                .emailNotificationsEnabled(user.getEmailNotificationsEnabled())
                .notifyOnApplicationStatus(user.getNotifyOnApplicationStatus())
                .notifyOnEventCancellation(user.getNotifyOnEventCancellation())
                .notifyOnPayment(user.getNotifyOnPayment())
                .notifyOnDisputeResolution(user.getNotifyOnDisputeResolution())
                .build();
    }
}
