package com.example.tralalero.auth.remote;

import com.example.tralalero.auth.remote.dto.FirebaseAuthDto;
import com.example.tralalero.auth.remote.dto.FirebaseAuthResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Public API interface for Firebase authentication
 * This interface is used without authentication headers
 */
public interface PublicAuthApi {
    @POST("users/firebase/auth")
    Call<FirebaseAuthResponse> firebaseAuth(@Body FirebaseAuthDto request);
}
