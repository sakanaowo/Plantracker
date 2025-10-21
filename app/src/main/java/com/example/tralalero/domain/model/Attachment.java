package com.example.tralalero.domain.model;

import java.util.Date;

public class Attachment {
    private final String id;
    private final String taskId;
    private final String url;
    private final String mimeType;
    private final Integer size;
    private final String uploadedBy; 
    private final Date createdAt;

    public Attachment(String id, String taskId, String url, String mimeType,
                      Integer size, String uploadedBy, Date createdAt) {
        this.id = id;
        this.taskId = taskId;
        this.url = url;
        this.mimeType = mimeType;
        this.size = size;
        this.uploadedBy = uploadedBy;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getTaskId() { return taskId; }
    public String getUrl() { return url; }
    public String getMimeType() { return mimeType; }
    public Integer getSize() { return size; }
    public String getUploadedBy() { return uploadedBy; }
    public Date getCreatedAt() { return createdAt; }

    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }

    public boolean isPdf() {
        return "application/pdf".equals(mimeType);
    }

    public boolean isDocument() {
        if (mimeType == null) return false;
        return mimeType.contains("document") ||
                mimeType.contains("word") ||
                mimeType.contains("excel") ||
                mimeType.contains("powerpoint");
    }

    public String getFileSizeFormatted() {
        if (size == null) return "Unknown";

        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    public String getFileExtension() {
        if (url == null) return "";
        int lastDot = url.lastIndexOf('.');
        if (lastDot > 0 && lastDot < url.length() - 1) {
            return url.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attachment that = (Attachment) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
