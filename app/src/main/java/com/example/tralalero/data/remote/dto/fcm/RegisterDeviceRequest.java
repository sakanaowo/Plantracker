package com.example.tralalero.data.remote.dto.fcm;

import com.google.gson.annotations.SerializedName;

public class RegisterDeviceRequest {
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

    public RegisterDeviceRequest(String fcmToken, String platform, String deviceModel,
                                 String appVersion, String locale, String timezone) {
        this.fcmToken = fcmToken;
        this.platform = platform;
        this.deviceModel = deviceModel;
        this.appVersion = appVersion;
        this.locale = locale;
        this.timezone = timezone;
    }

    // Getters
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

    // Setters
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
}
