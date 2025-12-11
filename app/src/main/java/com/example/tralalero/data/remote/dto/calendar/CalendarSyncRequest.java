package com.example.tralalero.data.remote.dto.calendar;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Request DTO for syncing events to Google Calendar
 */
public class CalendarSyncRequest {
    @SerializedName("eventIds")
    private List<String> eventIds;

    @SerializedName("projectId")
    private String projectId;

    public CalendarSyncRequest() {
    }

    public CalendarSyncRequest(List<String> eventIds, String projectId) {
        this.eventIds = eventIds;
        this.projectId = projectId;
    }

    public List<String> getEventIds() {
        return eventIds;
    }

    public void setEventIds(List<String> eventIds) {
        this.eventIds = eventIds;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}
