package com.example.tralalero.data.remote.dto.calendar;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for Google Calendar connection status
 */
public class CalendarConnectionStatusResponse {
    @SerializedName("connected")
    private boolean connected;

    @SerializedName("email")
    private String email;

    @SerializedName("lastSyncAt")
    private String lastSyncAt;

    @SerializedName("provider")
    private String provider;

    public CalendarConnectionStatusResponse() {
    }

    public CalendarConnectionStatusResponse(boolean connected, String email, String lastSyncAt, String provider) {
        this.connected = connected;
        this.email = email;
        this.lastSyncAt = lastSyncAt;
        this.provider = provider;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastSyncAt() {
        return lastSyncAt;
    }

    public void setLastSyncAt(String lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
