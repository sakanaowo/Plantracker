package com.example.tralalero.domain.model;

/**
 * Response after creating a meeting event
 */
public class MeetingResponse {
    private String eventId;
    private String meetLink;    // Google Meet link
    private String htmlLink;    // Google Calendar event link

    public MeetingResponse() {
    }

    public MeetingResponse(String eventId, String meetLink, String htmlLink) {
        this.eventId = eventId;
        this.meetLink = meetLink;
        this.htmlLink = htmlLink;
    }

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getMeetLink() {
        return meetLink;
    }

    public void setMeetLink(String meetLink) {
        this.meetLink = meetLink;
    }

    public String getHtmlLink() {
        return htmlLink;
    }

    public void setHtmlLink(String htmlLink) {
        this.htmlLink = htmlLink;
    }

    /**
     * Check if Google Meet link was generated
     */
    public boolean hasMeetLink() {
        return meetLink != null && !meetLink.isEmpty();
    }
}
