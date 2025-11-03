package com.example.tralalero.data.remote.dto.websocket;

import com.google.gson.annotations.SerializedName;

/**
 * WebSocket message wrapper
 * Represents the top-level structure of messages received from the WebSocket server
 */
public class WebSocketMessage {
    
    @SerializedName("type")
    private String type;  // "notification", "ping", "pong", "error", "subscribe-success"
    
    @SerializedName("payload")
    private NotificationPayload payload;
    
    @SerializedName("message")
    private String message;  // For error messages or simple responses
    
    @SerializedName("timestamp")
    private String timestamp;
    
    // Constructors
    public WebSocketMessage() {}
    
    public WebSocketMessage(String type, NotificationPayload payload, String message, String timestamp) {
        this.type = type;
        this.payload = payload;
        this.message = message;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public NotificationPayload getPayload() {
        return payload;
    }
    
    public void setPayload(NotificationPayload payload) {
        this.payload = payload;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "WebSocketMessage{" +
                "type='" + type + '\'' +
                ", payload=" + payload +
                ", message='" + message + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
