package com.example.tralalero.data.remote.dto.task;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO for updating task calendar sync settings
 */
public class TaskCalendarSyncRequest {
    @SerializedName("calendarReminderEnabled")
    private boolean calendarReminderEnabled;

    @SerializedName("calendarReminderTime")
    private Integer calendarReminderTime;  // minutes before due date

    @SerializedName("title")
    private String title;

    @SerializedName("dueAt")
    private String dueAt;  // ISO 8601 format

    public TaskCalendarSyncRequest() {
    }

    public TaskCalendarSyncRequest(boolean calendarReminderEnabled, Integer calendarReminderTime, String title, String dueAt) {
        this.calendarReminderEnabled = calendarReminderEnabled;
        this.calendarReminderTime = calendarReminderTime;
        this.title = title;
        this.dueAt = dueAt;
    }

    public boolean isCalendarReminderEnabled() {
        return calendarReminderEnabled;
    }

    public void setCalendarReminderEnabled(boolean calendarReminderEnabled) {
        this.calendarReminderEnabled = calendarReminderEnabled;
    }

    public Integer getCalendarReminderTime() {
        return calendarReminderTime;
    }

    public void setCalendarReminderTime(Integer calendarReminderTime) {
        this.calendarReminderTime = calendarReminderTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDueAt() {
        return dueAt;
    }

    public void setDueAt(String dueAt) {
        this.dueAt = dueAt;
    }
}
