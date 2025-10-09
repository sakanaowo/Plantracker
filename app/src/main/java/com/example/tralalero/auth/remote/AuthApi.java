package com.example.tralalero.auth.remote;

import com.example.tralalero.auth.remote.dto.FirebaseAuthDto;
import com.example.tralalero.auth.remote.dto.FirebaseAuthResponse;
import com.example.tralalero.auth.remote.dto.LoginRequest;
import com.example.tralalero.auth.remote.dto.LoginResponse;
import com.example.tralalero.auth.remote.dto.UpdateProfileRequest;
import com.example.tralalero.auth.remote.dto.UserDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("users/local/signin")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("users/local/signup")
    Call<LoginResponse> register(@Body LoginRequest request);

    @POST("users/firebase/auth")
    Call<FirebaseAuthResponse> firebaseAuth(@Body FirebaseAuthDto request);

    @GET("users/me")
    Call<UserDto> getMe();

    @PATCH("users/me")
    Call<UserDto> updateProfile(@Body UpdateProfileRequest request);

    @DELETE("users/me")
    Call<Void> deleteAccount();

//    @POST("user/firebase/sync")
//    Call<Void> syncFirebaseToken(@Body SyncFirebaseTokenRequest request);
}
