package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.fcm.DeviceResponse;
import com.example.tralalero.data.remote.dto.fcm.RegisterDeviceRequest;
import com.example.tralalero.data.remote.dto.fcm.UpdateTokenRequest;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface FcmApiService {

    @POST("users/devices/register")
    Call<DeviceResponse> registerDevice(@Body RegisterDeviceRequest request);

    @PATCH("users/devices/token")
    Call<DeviceResponse> updateToken(@Body UpdateTokenRequest request);

    @DELETE("users/devices/{deviceId}")
    Call<Map<String, String>> unregisterDevice(@Path("deviceId") String deviceId);

    @GET("users/devices")
    Call<List<DeviceResponse>> getDevices();
}
