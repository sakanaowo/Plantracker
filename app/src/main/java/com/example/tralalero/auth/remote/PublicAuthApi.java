package com.example.tralalero.auth.remote;

import com.example.tralalero.auth.remote.dto.FirebaseAuthDto;
import com.example.tralalero.data.remote.dto.auth.FirebaseAuthResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PublicAuthApi {
    @POST("users/firebase/auth")
    Call<FirebaseAuthResponse> firebaseAuth(@Body FirebaseAuthDto request);
}
