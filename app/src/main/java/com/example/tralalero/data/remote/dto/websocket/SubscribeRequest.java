package com.example.tralalero.data.remote.dto.websocket;

import com.google.gson.annotations.SerializedName;

/**
 * Subscribe request sent to WebSocket server
 * Sent after connection to subscribe to user's notification channel
 */
public class SubscribeRequest {
    
    @SerializedName("type")
    private String type = "subscribe";
    
    @SerializedName("userId")
    private String userId;
    
    // Constructors
    public SubscribeRequest() {}
    
    public SubscribeRequest(String userId) {
        this.userId = userId;
    }
    
    // Getters and Setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    @Override
    public String toString() {
        return "SubscribeRequest{" +
                "type='" + type + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
