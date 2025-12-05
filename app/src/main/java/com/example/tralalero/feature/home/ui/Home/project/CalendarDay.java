package com.example.tralalero.feature.home.ui.Home.project;

/**
 * Data model for a calendar day cell
 */
public class CalendarDay {
    private int dayOfMonth;
    private boolean isCurrentMonth;
    private boolean isToday;
    private boolean hasEvents;
    
    public CalendarDay(int dayOfMonth, boolean isCurrentMonth, boolean isToday, boolean hasEvents) {
        this.dayOfMonth = dayOfMonth;
        this.isCurrentMonth = isCurrentMonth;
        this.isToday = isToday;
        this.hasEvents = hasEvents;
    }
    
    public int getDayOfMonth() {
        return dayOfMonth;
    }
    
    public boolean isCurrentMonth() {
        return isCurrentMonth;
    }
    
    public boolean isOutsideMonth() {
        return !isCurrentMonth;
    }
    
    public boolean isToday() {
        return isToday;
    }
    
    public boolean hasEvents() {
        return hasEvents;
    }
    
    public void setHasEvents(boolean hasEvents) {
        this.hasEvents = hasEvents;
    }
}
