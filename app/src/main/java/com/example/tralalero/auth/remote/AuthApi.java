package com.example.tralalero.auth.remote;

import com.example.tralalero.auth.remote.dto.LoginRequest;
import com.example.tralalero.auth.remote.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
}
