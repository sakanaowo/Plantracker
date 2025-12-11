package com.example.tralalero.domain.model;

public class AuthUrlResponse {
    private String authUrl;
    
    public AuthUrlResponse() {
    }
    
    public AuthUrlResponse(String authUrl) {
        this.authUrl = authUrl;
    }
    
    public String getAuthUrl() {
        return authUrl;
    }
    
    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }
}
