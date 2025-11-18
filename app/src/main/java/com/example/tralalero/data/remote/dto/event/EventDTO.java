package com.example.tralalero.data.remote.dto.event;

import com.google.gson.annotations.SerializedName;

public class EventDTO {
    @SerializedName("id")
    private String id;

    @SerializedName("projectId")  // ✅ Changed from project_id to projectId for backend DTO
    private String projectId;

    @SerializedName("title")
    private String title;


    @SerializedName("startAt")  // ✅ Changed from start_at to startAt
    private String startAt;

    @SerializedName("endAt")  // ✅ Changed from end_at to endAt
    private String endAt;

    @SerializedName("location")
    private String location;

    @SerializedName("meetLink")  // ✅ Changed from meet_link to meetLink
    private String meetLink;

    @SerializedName("createdAt")  // ✅ Changed from created_at to createdAt
    private String createdAt;

    @SerializedName("updatedAt")  // ✅ Changed from updated_at to updatedAt
    private String updatedAt;
    
    @SerializedName("createdBy")  // ✅ Changed from created_by to createdBy
    private String createdBy;
    
    @SerializedName("participants")
    private java.util.List<ParticipantDTO> participants;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public String getEndAt() {
        return endAt;
    }

    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMeetLink() {
        return meetLink;
    }

    public void setMeetLink(String meetLink) {
        this.meetLink = meetLink;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public java.util.List<ParticipantDTO> getParticipants() {
        return participants;
    }
    
    public void setParticipants(java.util.List<ParticipantDTO> participants) {
        this.participants = participants;
    }
    
    /**
     * Nested DTO for participant data
     */
    public static class ParticipantDTO {
        @SerializedName("userId")
        private String userId;
        
        @SerializedName("email")
        private String email;
        
        @SerializedName("status")
        private String status;
        
        public String getUserId() {
            return userId;
        }
        
        public void setUserId(String userId) {
            this.userId = userId;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
    }
}
