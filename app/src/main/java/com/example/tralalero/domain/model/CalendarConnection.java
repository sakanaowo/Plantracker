package com.example.tralalero.domain.model;

/**
 * Domain model for Google Calendar connection status
 */
public class CalendarConnection {
    private boolean connected;
    private String email;
    private String lastSyncAt;
    private String provider;

    public CalendarConnection() {
    }

    public CalendarConnection(boolean connected, String email, String lastSyncAt, String provider) {
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
