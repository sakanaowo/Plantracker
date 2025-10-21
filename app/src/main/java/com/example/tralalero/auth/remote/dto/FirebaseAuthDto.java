package com.example.tralalero.auth.remote.dto;


public class FirebaseAuthDto {

    public String idToken;

    public FirebaseAuthDto() {
    }

    public FirebaseAuthDto(String idToken) {
        this.idToken = idToken;
    }
}