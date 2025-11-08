package com.example.tralalero.domain.model;

/**
 * Enum for event types in the application
 */
public enum EventType {
    /**
     * Meeting event type
     */
    MEETING,
    
    /**
     * Milestone event type
     */
    MILESTONE,
    
    /**
     * Other/generic event type
     */
    OTHER;
    
    /**
     * Convert string to EventType enum
     * @param value String value
     * @return EventType enum or OTHER as default
     */
    public static EventType fromString(String value) {
        if (value == null) {
            return OTHER;
        }
        
        try {
            return EventType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }
    
    /**
     * Get display name for the event type
     * @return Display name
     */
    public String getDisplayName() {
        switch (this) {
            case MEETING:
                return "Meeting";
            case MILESTONE:
                return "Milestone";
            case OTHER:
            default:
                return "Other";
        }
    }
}
