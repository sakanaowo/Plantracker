package com.example.tralalero.data.remote.dto.fcm;

import com.google.gson.annotations.SerializedName;

public class DeviceResponse {
    @SerializedName("id")
    private String id;

    @SerializedName("userId")
    private String userId;

    @SerializedName("fcmToken")
    private String fcmToken;

    @SerializedName("platform")
    private String platform;

    @SerializedName("deviceModel")
    private String deviceModel;

    @SerializedName("appVersion")
    private String appVersion;

    @SerializedName("locale")
    private String locale;

    @SerializedName("timezone")
    private String timezone;

    @SerializedName("isActive")
    private boolean isActive;

    @SerializedName("lastActiveAt")
    private String lastActiveAt;

    // Getters
    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public String getPlatform() {
        return platform;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getLocale() {
        return locale;
    }

    public String getTimezone() {
        return timezone;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getLastActiveAt() {
        return lastActiveAt;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setLastActiveAt(String lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }
}
