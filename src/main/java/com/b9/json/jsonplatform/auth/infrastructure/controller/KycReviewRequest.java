package com.b9.json.jsonplatform.auth.infrastructure.controller;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class KycReviewRequest {
    private String email;
    private boolean approved;
}