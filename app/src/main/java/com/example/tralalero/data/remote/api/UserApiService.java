package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.user.UserDTO;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface UserApiService {
    
    @GET("users/{id}")
    Call<UserDTO> getUserById(@Path("id") String userId);
}