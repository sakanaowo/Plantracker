package com.example.tralalero.feature.home.ui.Home.project;

import com.example.tralalero.data.remote.dto.task.TaskDTO;
import com.example.tralalero.data.remote.dto.event.EventDTO;

/**
 * Wrapper class for combined task/event list in calendar
 */
public class CalendarItem {
    public enum Type {
        TASK, EVENT
    }
    
    private Type type;
    private TaskDTO task;
    private EventDTO event;
    
    public static CalendarItem fromTask(TaskDTO task) {
        CalendarItem item = new CalendarItem();
        item.type = Type.TASK;
        item.task = task;
        return item;
    }
    
    public static CalendarItem fromEvent(EventDTO event) {
        CalendarItem item = new CalendarItem();
        item.type = Type.EVENT;
        item.event = event;
        return item;
    }
    
    public Type getType() {
        return type;
    }
    
    public TaskDTO getTask() {
        return task;
    }
    
    public EventDTO getEvent() {
        return event;
    }
    
    public String getId() {
        return type == Type.TASK ? task.getId() : event.getId();
    }
    
    public String getTitle() {
        return type == Type.TASK ? task.getTitle() : event.getTitle();
    }
}
