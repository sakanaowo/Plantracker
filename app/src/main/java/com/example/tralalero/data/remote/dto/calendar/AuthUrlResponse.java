package com.example.tralalero.data.remote.dto.calendar;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for Google OAuth authorization URL
 */
public class AuthUrlResponse {
    @SerializedName("authUrl")
    private String authUrl;

    @SerializedName("state")
    private String state;

    public AuthUrlResponse() {
    }

    public AuthUrlResponse(String authUrl, String state) {
        this.authUrl = authUrl;
        this.state = state;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
