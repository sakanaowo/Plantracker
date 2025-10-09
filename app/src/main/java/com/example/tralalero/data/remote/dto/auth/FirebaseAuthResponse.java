package com.example.tralalero.data.remote.dto.auth;

/**
 * Response from Firebase authentication endpoint
 * Since backend uses Firebase ID token directly, no separate JWT is returned
 */
public class FirebaseAuthResponse {
    /**
     * User information from backend
     */
    public UserDto user;
    
    /**
     * Response message
     */
    public String message;

    /**
     * Success status
     */
    public boolean success;

    public FirebaseAuthResponse() {
    }
}