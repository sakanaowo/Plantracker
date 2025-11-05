package com.example.tralalero.domain.model;

import java.util.List;

/**
 * Domain model for calendar sync result
 */
public class CalendarSyncResult {
    private boolean success;
    private int syncedCount;
    private int failedCount;
    private List<EventSyncResult> results;

    public CalendarSyncResult() {
    }

    public CalendarSyncResult(boolean success, int syncedCount, int failedCount, List<EventSyncResult> results) {
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

    public List<EventSyncResult> getResults() {
        return results;
    }

    public void setResults(List<EventSyncResult> results) {
        this.results = results;
    }

    /**
     * Result for individual event sync
     */
    public static class EventSyncResult {
        private String eventId;
        private boolean success;
        private String googleEventId;
        private String error;

        public EventSyncResult() {
        }

        public EventSyncResult(String eventId, boolean success, String googleEventId, String error) {
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
