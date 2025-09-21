package com.example.tralalero.auth.remote;

import com.example.tralalero.auth.remote.dto.LoginRequest;
import com.example.tralalero.auth.remote.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("users/local/signin")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("users/local/signup")
    Call<LoginResponse> register(@Body LoginRequest request);

    @POST("user/firebase/sync")
    Call<Void> syncFirebaseToken(@Body SyncFirebaseTokenRequest request);
}
