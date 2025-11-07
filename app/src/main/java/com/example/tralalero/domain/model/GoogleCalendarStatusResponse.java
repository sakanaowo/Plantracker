package com.example.tralalero.domain.model;

public class GoogleCalendarStatusResponse {
    private boolean connected;
    private String userEmail;
    
    public GoogleCalendarStatusResponse() {
    }
    
    public GoogleCalendarStatusResponse(boolean connected, String userEmail) {
        this.connected = connected;
        this.userEmail = userEmail;
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public void setConnected(boolean connected) {
        this.connected = connected;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
