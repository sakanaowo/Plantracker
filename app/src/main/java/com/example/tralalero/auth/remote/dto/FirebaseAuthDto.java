package com.example.tralalero.auth.remote.dto;

/**
 * Data Transfer Object for Firebase Authentication request
 * Maps to backend FirebaseAuthDto
 */
public class FirebaseAuthDto {
    /**
     * Firebase ID token from Google Sign-In
     */
    public String idToken;

    public FirebaseAuthDto() {
    }

    public FirebaseAuthDto(String idToken) {
        this.idToken = idToken;
    }
}