package com.example.tralalero.data.remote.api;

import com.example.tralalero.domain.model.GoogleCalendarStatusResponse;
import com.example.tralalero.domain.model.AuthUrlResponse;

import retrofit2.Call;
import retrofit2.http.*;

public interface GoogleAuthApiService {
    
    @GET("auth/google/status")
    Call<GoogleCalendarStatusResponse> getIntegrationStatus();
    
    @GET("auth/google/auth-url")
    Call<AuthUrlResponse> getAuthUrl();
    
    @POST("auth/google/disconnect")
    Call<Void> disconnect();
}
