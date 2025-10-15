package com.example.tralalero.data.remote.dto.task;

import com.google.gson.annotations.SerializedName;

public class AttachmentDTO {
    public String id;
    public String taskId;

    @SerializedName("url")
    public String url;

    @SerializedName("mime_type")
    public String mimeType; 

    @SerializedName("size")
    public Integer size; 

    @SerializedName("uploaded_by")
    public String uploadedBy; 

    @SerializedName("created_at")
    public String createdAt;
}
