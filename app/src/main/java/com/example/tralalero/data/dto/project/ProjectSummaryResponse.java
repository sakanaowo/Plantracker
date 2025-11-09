package com.example.tralalero.data.dto.project;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for project summary statistics
 */
public class ProjectSummaryResponse {
    
    @SerializedName("done")
    private int done;
    
    @SerializedName("updated")
    private int updated;
    
    @SerializedName("created")
    private int created;
    
    @SerializedName("due")
    private int due;
    
    @SerializedName("statusOverview")
    private StatusOverview statusOverview;
    
    public ProjectSummaryResponse() {}
    
    // Getters and Setters
    public int getDone() {
        return done;
    }
    
    public void setDone(int done) {
        this.done = done;
    }
    
    public int getUpdated() {
        return updated;
    }
    
    public void setUpdated(int updated) {
        this.updated = updated;
    }
    
    public int getCreated() {
        return created;
    }
    
    public void setCreated(int created) {
        this.created = created;
    }
    
    public int getDue() {
        return due;
    }
    
    public void setDue(int due) {
        this.due = due;
    }
    
    public StatusOverview getStatusOverview() {
        return statusOverview;
    }
    
    public void setStatusOverview(StatusOverview statusOverview) {
        this.statusOverview = statusOverview;
    }
    
    /**
     * Status breakdown for the project
     */
    public static class StatusOverview {
        @SerializedName("period")
        private String period;
        
        @SerializedName("total")
        private int total;
        
        @SerializedName("toDo")
        private int toDo;
        
        @SerializedName("inProgress")
        private int inProgress;
        
        @SerializedName("inReview")
        private int inReview;
        
        @SerializedName("done")
        private int done;
        
        public StatusOverview() {}
        
        // Getters and Setters
        public String getPeriod() {
            return period;
        }
        
        public void setPeriod(String period) {
            this.period = period;
        }
        
        public int getTotal() {
            return total;
        }
        
        public void setTotal(int total) {
            this.total = total;
        }
        
        public int getToDo() {
            return toDo;
        }
        
        public void setToDo(int toDo) {
            this.toDo = toDo;
        }
        
        public int getInProgress() {
            return inProgress;
        }
        
        public void setInProgress(int inProgress) {
            this.inProgress = inProgress;
        }
        
        public int getInReview() {
            return inReview;
        }
        
        public void setInReview(int inReview) {
            this.inReview = inReview;
        }
        
        public int getDone() {
            return done;
        }
        
        public void setDone(int done) {
            this.done = done;
        }
    }
}
