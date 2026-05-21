package com.b9.json.jsonplatform.auth.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInternalResponse {
    private String username;
    private String fullName;
    private String phoneNumber;

    public UserInternalResponse(String username, String fullName, String phoneNumber) {
        this.username = username;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }
}