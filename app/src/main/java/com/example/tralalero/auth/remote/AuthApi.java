package com.example.tralalero.auth.remote;

import com.example.tralalero.auth.remote.dto.LoginRequest;
import com.example.tralalero.auth.remote.dto.LoginResponse;
import com.example.tralalero.auth.remote.dto.UserDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("users/local/signin")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("users/local/signup")
    Call<LoginResponse> register(@Body LoginRequest request);

    @GET("users/me")
    Call<UserDto> getMe();

//    @POST("user/firebase/sync")
//    Call<Void> syncFirebaseToken(@Body SyncFirebaseTokenRequest request);
}
