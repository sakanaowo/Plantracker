package com.example.tralalero.data.remote.dto.websocket;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * Notification payload received from WebSocket
 * Contains the actual notification data
 */
public class NotificationPayload {
    
    @SerializedName("id")
    private String id;
    
    @SerializedName("userId")
    private String userId;
    
    @SerializedName("type")
    private String type;  // TASK_ASSIGNED, TASK_UPDATED, MEETING_REMINDER, etc.
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("body")
    private String body;
    
    @SerializedName("data")
    private Map<String, String> data;  // Additional data like taskId, eventId, etc.
    
    @SerializedName("isRead")
    private boolean isRead;
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("readAt")
    private String readAt;
    
    // Constructors
    public NotificationPayload() {}
    
    public NotificationPayload(String id, String userId, String type, String title, 
                               String body, Map<String, String> data, boolean isRead, 
                               String createdAt, String readAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.body = body;
        this.data = data;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    
    public Map<String, String> getData() {
        return data;
    }
    
    public void setData(Map<String, String> data) {
        this.data = data;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void setRead(boolean read) {
        isRead = read;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getReadAt() {
        return readAt;
    }
    
    public void setReadAt(String readAt) {
        this.readAt = readAt;
    }
    
    @Override
    public String toString() {
        return "NotificationPayload{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", data=" + data +
                ", isRead=" + isRead +
                ", createdAt='" + createdAt + '\'' +
                ", readAt='" + readAt + '\'' +
                '}';
    }
}
