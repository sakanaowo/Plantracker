package com.example.tralalero.data.remote.dto.task;

import com.google.gson.annotations.SerializedName;

public class UploadUrlResponseDTO {
    @SerializedName("uploadUrl")
    private String uploadUrl;

    @SerializedName("attachmentId")
    private String attachmentId;

    @SerializedName("storagePath")
    private String storagePath;

    public String getUploadUrl() { return uploadUrl; }
    public void setUploadUrl(String uploadUrl) { this.uploadUrl = uploadUrl; }
    public String getAttachmentId() { return attachmentId; }
    public void setAttachmentId(String attachmentId) { this.attachmentId = attachmentId; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
}
