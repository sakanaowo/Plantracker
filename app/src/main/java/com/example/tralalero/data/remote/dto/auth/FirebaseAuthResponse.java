package com.example.tralalero.data.remote.dto.auth;

public class FirebaseAuthResponse {

    public UserDto user;
    

    public String message;


    public boolean success;

    public FirebaseAuthResponse() {
    }

    public UserDto getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }
}