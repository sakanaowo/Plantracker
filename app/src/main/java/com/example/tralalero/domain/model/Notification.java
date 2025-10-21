package com.example.tralalero.domain.model;

import java.util.Date;

public class Notification {
    private final String id;
    private final String userId;
    private final NotificationType type;
    private final String title;
    private final String body;
    private final String deeplink;
    private final Date readAt;
    private final Date createdAt;

    public Notification(String id, String userId, NotificationType type, String title,
                       String body, String deeplink, Date readAt, Date createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.body = body;
        this.deeplink = deeplink;
        this.readAt = readAt;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getDeeplink() {
        return deeplink;
    }

    public Date getReadAt() {
        return readAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return readAt != null;
    }

    public boolean isUnread() {
        return readAt == null;
    }

    public boolean hasDeeplink() {
        return deeplink != null && !deeplink.trim().isEmpty();
    }

    public boolean hasBody() {
        return body != null && !body.trim().isEmpty();
    }

    public String getTypeDisplay() {
        switch (type) {
            case TASK_ASSIGNED:
                return "Task Assigned";
            case TASK_MOVED:
                return "Task Moved";
            case TIME_REMINDER:
                return "Time Reminder";
            case EVENT_INVITE:
                return "Event Invite";
            case EVENT_UPDATED:
                return "Event Updated";
            case MEETING_REMINDER:
                return "Meeting Reminder";
            case SYSTEM:
                return "System";
            default:
                return "Unknown";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Notification{id='" + id + "', type=" + type + ", title='" + title + "'}";
    }

    public enum NotificationType {
        TASK_ASSIGNED, TASK_MOVED, TIME_REMINDER,
        EVENT_INVITE, EVENT_UPDATED, MEETING_REMINDER, SYSTEM
    }
}
