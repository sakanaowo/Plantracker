package com.example.tralalero.auth.remote;

import com.example.tralalero.auth.remote.dto.FirebaseAuthDto;
import com.example.tralalero.auth.remote.dto.UploadUrlRequest;
import com.example.tralalero.auth.remote.dto.UploadUrlResponse;
import com.example.tralalero.auth.remote.dto.ViewUrlResponse;
import com.example.tralalero.data.remote.dto.auth.FirebaseAuthResponse;
import com.example.tralalero.data.remote.dto.auth.LoginRequest;
import com.example.tralalero.data.remote.dto.auth.LoginResponse;
import com.example.tralalero.auth.remote.dto.UpdateProfileRequest;
import com.example.tralalero.data.remote.dto.auth.UserDto;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface AuthApi {
    @POST("users/local/signin")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("users/local/signup")
    Call<LoginResponse> register(@Body LoginRequest request);

    @POST("users/firebase/auth")
    Call<FirebaseAuthResponse> firebaseAuth(@Body FirebaseAuthDto request);

    @GET("users/me")
    Call<UserDto> getMe();

    @PUT("users/me")
    Call<UserDto> updateProfile(@Body UpdateProfileRequest request);

    @DELETE("users/me")
    Call<Void> deleteAccount();
    
    @DELETE("users/devices/{deviceId}")
    Call<Void> unregisterDevice(@Path("deviceId") String deviceId);
    
    // ========== Storage APIs ==========
    
    /**
     * Step 1: Request signed upload URL from backend
     * POST /api/storage/upload-url
     */
    @POST("storage/upload-url")
    Call<UploadUrlResponse> requestUploadUrl(@Body UploadUrlRequest request);
    
    /**
     * Step 2: Upload file directly to Supabase using signed URL
     * PUT {signedUrl}
     * Note: This is a dynamic URL call, not a fixed endpoint
     */
    @PUT
    Call<ResponseBody> uploadToSupabase(@Url String signedUrl, @Body RequestBody file);
    
    /**
     * Get signed view URL for a file (public endpoint)
     * GET /api/storage/view-url?path={path}
     */
    @GET("storage/view-url")
    Call<ViewUrlResponse> getViewUrl(@Query("path") String path);
}
