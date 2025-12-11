package com.example.tralalero.data.remote.dto.task;

import com.google.gson.annotations.SerializedName;

public class AttachmentDTO {
    public String id;
    public String taskId;

    @SerializedName("url")
    public String url;

    @SerializedName("fileName")  // ✅ Backend uses camelCase
    public String fileName;

    @SerializedName("mimeType")  // ✅ Backend uses camelCase
    public String mimeType; 

    @SerializedName("size")
    public Integer size; 

    @SerializedName("uploadedBy")  // ✅ Backend uses camelCase
    public String uploadedBy; 

    @SerializedName("createdAt")  // ✅ Backend uses camelCase
    public String createdAt;
}
