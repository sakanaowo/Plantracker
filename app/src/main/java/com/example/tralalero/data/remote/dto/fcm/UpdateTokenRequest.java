package com.example.tralalero.data.remote.dto.fcm;

import com.google.gson.annotations.SerializedName;

public class UpdateTokenRequest {
    @SerializedName("deviceId")
    private String deviceId;

    @SerializedName("newFcmToken")
    private String newFcmToken;

    public UpdateTokenRequest(String deviceId, String newFcmToken) {
        this.deviceId = deviceId;
        this.newFcmToken = newFcmToken;
    }

    // Getters
    public String getDeviceId() {
        return deviceId;
    }

    public String getNewFcmToken() {
        return newFcmToken;
    }

    // Setters
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setNewFcmToken(String newFcmToken) {
        this.newFcmToken = newFcmToken;
    }
}
