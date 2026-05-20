package com.b9.json.jsonplatform.auth.infrastructure.controller;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PublicProfileResponse {
    private String username;
    private String fullName;
    private String email;
    private String role;
    private String kycStatus;
    private boolean isBanned;
    private double rating;
    private int totalReviews;
    private long totalSuccessfulTransactions;
}