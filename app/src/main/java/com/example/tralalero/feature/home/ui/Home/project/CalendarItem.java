package com.example.tralalero.feature.home.ui.Home.project;

import com.example.tralalero.domain.model.CalendarEvent;
import com.example.tralalero.domain.model.Task;

import java.util.Date;

/**
 * Wrapper class to hold either a Task or Event for unified display in calendar
 */
public class CalendarItem {
    public enum ItemType {
        TASK, EVENT
    }
    
    private final ItemType type;
    private final Task task;
    private final CalendarEvent event;
    
    // Private constructor for Task
    private CalendarItem(Task task) {
        this.type = ItemType.TASK;
        this.task = task;
        this.event = null;
    }
    
    // Private constructor for Event
    private CalendarItem(CalendarEvent event) {
        this.type = ItemType.EVENT;
        this.task = null;
        this.event = event;
    }
    
    // Factory methods
    public static CalendarItem fromTask(Task task) {
        return new CalendarItem(task);
    }
    
    public static CalendarItem fromEvent(CalendarEvent event) {
        return new CalendarItem(event);
    }
    
    // Getters
    public ItemType getType() {
        return type;
    }
    
    public Task getTask() {
        return task;
    }
    
    public CalendarEvent getEvent() {
        return event;
    }
    
    public boolean isTask() {
        return type == ItemType.TASK;
    }
    
    public boolean isEvent() {
        return type == ItemType.EVENT;
    }
    
    // Common properties
    public String getTitle() {
        if (isTask()) {
            return task.getTitle();
        } else {
            return event.getTitle();
        }
    }
    
    public String getDescription() {
        if (isTask()) {
            return task.getDescription();
        } else {
            return event.getDescription();
        }
    }
    
    public Date getStartDate() {
        if (isTask()) {
            return task.getStartAt() != null ? task.getStartAt() : task.getDueAt();
        } else {
            // Parse ISO 8601 date from event
            if (event.getStartAt() != null) {
                try {
                    java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
                    return isoFormat.parse(event.getStartAt());
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        }
    }
    
    public String getTimeText() {
        if (isTask()) {
            if (task.getDueAt() != null) {
                java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat(
                    "HH:mm", java.util.Locale.getDefault());
                return "⏰ " + timeFormat.format(task.getDueAt());
            }
            return "Task";
        } else {
            if (event.getStartAt() != null) {
                try {
                    // Parse ISO time
                    java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
                    Date startDate = isoFormat.parse(event.getStartAt());
                    
                    java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat(
                        "HH:mm", java.util.Locale.getDefault());
                    return "📅 " + timeFormat.format(startDate);
                } catch (Exception e) {
                    return "Event";
                }
            }
            return "Event";
        }
    }
}
