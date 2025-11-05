package com.example.tralalero.data.remote.dto.calendar;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Response DTO for calendar sync operations
 */
public class CalendarSyncResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("syncedCount")
    private int syncedCount;

    @SerializedName("failedCount")
    private int failedCount;

    @SerializedName("results")
    private List<SyncResult> results;

    public CalendarSyncResponse() {
    }

    public CalendarSyncResponse(boolean success, int syncedCount, int failedCount, List<SyncResult> results) {
        this.success = success;
        this.syncedCount = syncedCount;
        this.failedCount = failedCount;
        this.results = results;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getSyncedCount() {
        return syncedCount;
    }

    public void setSyncedCount(int syncedCount) {
        this.syncedCount = syncedCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public List<SyncResult> getResults() {
        return results;
    }

    public void setResults(List<SyncResult> results) {
        this.results = results;
    }

    /**
     * Individual sync result for each event
     */
    public static class SyncResult {
        @SerializedName("eventId")
        private String eventId;

        @SerializedName("success")
        private boolean success;

        @SerializedName("googleEventId")
        private String googleEventId;

        @SerializedName("error")
        private String error;

        public SyncResult() {
        }

        public SyncResult(String eventId, boolean success, String googleEventId, String error) {
            this.eventId = eventId;
            this.success = success;
            this.googleEventId = googleEventId;
            this.error = error;
        }

        public String getEventId() {
            return eventId;
        }

        public void setEventId(String eventId) {
            this.eventId = eventId;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getGoogleEventId() {
            return googleEventId;
        }

        public void setGoogleEventId(String googleEventId) {
            this.googleEventId = googleEventId;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
