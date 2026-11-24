package com.hackathonbuddy.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private UserResponse user;

    public AuthResponse() {
    }

    public AuthResponse(String token, String tokenType, UserResponse user) {
        this.token = token;
        this.tokenType = tokenType;
        this.user = user;
    }
}
