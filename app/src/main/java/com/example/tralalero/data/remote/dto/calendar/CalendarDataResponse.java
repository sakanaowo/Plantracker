package com.example.tralalero.data.remote.dto.calendar;

import com.example.tralalero.data.remote.dto.task.TaskDTO;
import com.example.tralalero.data.remote.dto.event.EventDTO;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Response DTO for GET /projects/:id/calendar-data
 * Contains tasks and events for a specific month
 */
public class CalendarDataResponse {
    
    @SerializedName("tasks")
    private List<TaskDTO> tasks;
    
    @SerializedName("events")
    private List<EventDTO> events;
    
    @SerializedName("datesWithItems")
    private List<String> datesWithItems; // Array of dates in YYYY-MM-DD format
    
    @SerializedName("month")
    private String month; // YYYY-MM
    
    @SerializedName("summary")
    private Summary summary;
    
    public static class Summary {
        @SerializedName("totalTasks")
        private int totalTasks;
        
        @SerializedName("totalEvents")
        private int totalEvents;
        
        @SerializedName("datesWithItems")
        private int datesWithItems;

        public int getTotalTasks() {
            return totalTasks;
        }

        public int getTotalEvents() {
            return totalEvents;
        }

        public int getDatesWithItems() {
            return datesWithItems;
        }
    }

    public List<TaskDTO> getTasks() {
        return tasks;
    }

    public List<EventDTO> getEvents() {
        return events;
    }

    public List<String> getDatesWithItems() {
        return datesWithItems;
    }

    public String getMonth() {
        return month;
    }

    public Summary getSummary() {
        return summary;
    }
}
