package com.example.tralalero.domain.model;

/**
 * Enum for event recurrence types
 */
public enum RecurrenceType {
    /**
     * No recurrence - one-time event
     */
    NONE,
    
    /**
     * Daily recurrence
     */
    DAILY,
    
    /**
     * Weekly recurrence
     */
    WEEKLY,
    
    /**
     * Bi-weekly (every 2 weeks) recurrence
     */
    BIWEEKLY,
    
    /**
     * Monthly recurrence
     */
    MONTHLY;
    
    /**
     * Convert string to RecurrenceType enum
     * @param value String value
     * @return RecurrenceType enum or NONE as default
     */
    public static RecurrenceType fromString(String value) {
        if (value == null) {
            return NONE;
        }
        
        try {
            return RecurrenceType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }
    
    /**
     * Get display name for the recurrence type
     * @return Display name
     */
    public String getDisplayName() {
        switch (this) {
            case NONE:
                return "None";
            case DAILY:
                return "Daily";
            case WEEKLY:
                return "Weekly";
            case BIWEEKLY:
                return "Bi-weekly";
            case MONTHLY:
                return "Monthly";
            default:
                return "None";
        }
    }
    
    /**
     * Get interval in days for the recurrence type
     * @return Number of days, or 0 for NONE
     */
    public int getIntervalDays() {
        switch (this) {
            case DAILY:
                return 1;
            case WEEKLY:
                return 7;
            case BIWEEKLY:
                return 14;
            case MONTHLY:
                return 30; // Approximate
            case NONE:
            default:
                return 0;
        }
    }
}
