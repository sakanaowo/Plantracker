package com.example.tralalero.auth.remote.dto;

public class UploadUrlResponse {
    public String path;
    public String signedUrl;
    public String token;
    
    public UploadUrlResponse(String path, String signedUrl, String token) {
        this.path = path;
        this.signedUrl = signedUrl;
        this.token = token;
    }
}
