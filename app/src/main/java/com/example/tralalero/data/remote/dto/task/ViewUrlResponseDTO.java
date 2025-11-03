package com.example.tralalero.data.remote.dto.task;

import com.google.gson.annotations.SerializedName;

public class ViewUrlResponseDTO {
    @SerializedName("url")
    private String url;

    @SerializedName("expiresAt")
    private String expiresAt;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
}
