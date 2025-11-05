package com.example.tralalero.data.remote.dto.calendar;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for OAuth callback handling
 */
public class CallbackResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("connectedAt")
    private String connectedAt;

    public CallbackResponse() {
    }

    public CallbackResponse(boolean success, String message, String connectedAt) {
        this.success = success;
        this.message = message;
        this.connectedAt = connectedAt;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(String connectedAt) {
        this.connectedAt = connectedAt;
    }
}
