package com.example.tralalero.data.dto.calendar;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Response DTO for meeting creation
 */
public class MeetingResponse {
    
    @SerializedName("id")
    private String id;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("startAt")
    private String startAt;
    
    @SerializedName("endAt")
    private String endAt;
    
    @SerializedName("meetLink")
    private String meetLink;
    
    @SerializedName("participants")
    private List<Participant> participants;
    
    public MeetingResponse() {}
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
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
    
    public String getMeetLink() {
        return meetLink;
    }
    
    public void setMeetLink(String meetLink) {
        this.meetLink = meetLink;
    }
    
    public List<Participant> getParticipants() {
        return participants;
    }
    
    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }
    
    /**
     * Participant info
     */
    public static class Participant {
        @SerializedName("id")
        private String id;
        
        @SerializedName("email")
        private String email;
        
        @SerializedName("displayName")
        private String displayName;
        
        @SerializedName("status")
        private String status;
        
        public Participant() {}
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
    }
}
