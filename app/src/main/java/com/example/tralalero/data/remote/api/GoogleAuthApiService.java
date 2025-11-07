package com.example.tralalero.data.remote.api;

import com.example.tralalero.domain.model.GoogleCalendarStatusResponse;
import com.example.tralalero.domain.model.AuthUrlResponse;

import retrofit2.Call;
import retrofit2.http.*;

public interface GoogleAuthApiService {
    
    @GET("google-auth/status")
    Call<GoogleCalendarStatusResponse> getIntegrationStatus();
    
    @GET("google-auth/auth-url")
    Call<AuthUrlResponse> getAuthUrl();
    
    @POST("google-auth/disconnect")
    Call<Void> disconnect();
}
