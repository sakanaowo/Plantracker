package com.example.tralalero.domain.model;

import java.util.List;

/**
 * Response containing suggested meeting times
 */
public class MeetingTimeSuggestion {
    private List<TimeSlot> suggestions;
    private int totalUsersChecked;
    private DateRange checkedRange;

    public MeetingTimeSuggestion() {
    }

    public MeetingTimeSuggestion(List<TimeSlot> suggestions, int totalUsersChecked, DateRange checkedRange) {
        this.suggestions = suggestions;
        this.totalUsersChecked = totalUsersChecked;
        this.checkedRange = checkedRange;
    }

    // Getters and Setters
    public List<TimeSlot> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<TimeSlot> suggestions) {
        this.suggestions = suggestions;
    }

    public int getTotalUsersChecked() {
        return totalUsersChecked;
    }

    public void setTotalUsersChecked(int totalUsersChecked) {
        this.totalUsersChecked = totalUsersChecked;
    }

    public DateRange getCheckedRange() {
        return checkedRange;
    }

    public void setCheckedRange(DateRange checkedRange) {
        this.checkedRange = checkedRange;
    }

    /**
     * Check if any suggestions were found
     */
    public boolean hasSuggestions() {
        return suggestions != null && !suggestions.isEmpty();
    }

    /**
     * Inner class for date range
     */
    public static class DateRange {
        private String start;
        private String end;

        public DateRange() {
        }

        public DateRange(String start, String end) {
            this.start = start;
            this.end = end;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }
    }
}
