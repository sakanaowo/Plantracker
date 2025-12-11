package com.example.tralalero.domain.model;

public class GoogleCalendarStatusResponse {
    private boolean isConnected;
    private String accountEmail;
    
    public GoogleCalendarStatusResponse() {
    }
    
    public GoogleCalendarStatusResponse(boolean isConnected, String accountEmail) {
        this.isConnected = isConnected;
        this.accountEmail = accountEmail;
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }
    
    public String getAccountEmail() {
        return accountEmail;
    }
    
    public void setAccountEmail(String accountEmail) {
        this.accountEmail = accountEmail;
    }
}
