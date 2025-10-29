package com.example.tralalero.feature.task.attachments;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import com.example.tralalero.data.remote.api.AttachmentApiService;
import com.example.tralalero.data.remote.dto.task.UploadUrlResponseDTO;
import com.example.tralalero.data.remote.dto.task.AttachmentDTO;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AttachmentUploader {

    private final AttachmentApiService apiService;
    private final Context context;

    public interface UploadCallback {
        void onProgress(int percent);
        void onSuccess(AttachmentDTO attachment);
        void onError(String error);
    }

    public AttachmentUploader(AttachmentApiService apiService, Context context) {
        this.apiService = apiService;
        this.context = context;
    }

    public void uploadFile(String taskId, Uri fileUri, UploadCallback callback) {
        try {
            String fileName = getFileName(fileUri);
            long fileSize = getFileSize(fileUri);
            String mimeType = getMimeType(fileUri);

            android.util.Log.d("AttachmentUploader", "Starting upload - File: " + fileName + 
                ", Size: " + fileSize + ", Type: " + mimeType);

            callback.onProgress(10);

            // Backend expects: fileName (string), mimeType (string), size (integer)
            java.util.Map<String, Object> req = new java.util.HashMap<>();
            req.put("fileName", fileName);
            req.put("mimeType", mimeType);
            req.put("size", (int) fileSize);  // ✅ Changed from "fileSize" to "size", cast to int

            android.util.Log.d("AttachmentUploader", "Requesting upload URL for task: " + taskId);

            apiService.requestUploadUrl(taskId, req).enqueue(new Callback<UploadUrlResponseDTO>() {
                @Override
                public void onResponse(Call<UploadUrlResponseDTO> call, Response<UploadUrlResponseDTO> response) {
                    android.util.Log.d("AttachmentUploader", "Upload URL response code: " + response.code());
                    
                    if (response.isSuccessful() && response.body() != null) {
                        callback.onProgress(30);
                        UploadUrlResponseDTO body = response.body();
                        String uploadUrl = body.getUploadUrl();
                        String attachmentId = body.getAttachmentId();

                        android.util.Log.d("AttachmentUploader", "Got upload URL, attachment ID: " + attachmentId);

                        // Read bytes
                        try {
                            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
                            byte[] bytes = readBytes(inputStream);

                            android.util.Log.d("AttachmentUploader", "Read " + bytes.length + " bytes");
                            callback.onProgress(50);

                            RequestBody rb = RequestBody.create(MediaType.parse(mimeType), bytes);

                            android.util.Log.d("AttachmentUploader", "Uploading to signed URL...");

                            // Upload to signed URL
                            apiService.uploadToSignedUrl(uploadUrl, rb).enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    android.util.Log.d("AttachmentUploader", "Upload to storage response: " + response.code());
                                    
                                    if (response.isSuccessful()) {
                                        callback.onProgress(100);
                                        android.util.Log.d("AttachmentUploader", "Upload completed successfully!");
                                        
                                        // Build attachment DTO with minimal fields (public fields, not setters)
                                        AttachmentDTO attachment = new AttachmentDTO();
                                        attachment.id = attachmentId;
                                        attachment.fileName = fileName;
                                        attachment.size = bytes.length;
                                        attachment.mimeType = mimeType;
                                        callback.onSuccess(attachment);
                                    } else {
                                        String errorMsg = "Upload failed: " + response.code();
                                        try {
                                            if (response.errorBody() != null) {
                                                errorMsg += " - " + response.errorBody().string();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        android.util.Log.e("AttachmentUploader", errorMsg);
                                        callback.onError(errorMsg);
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    String errorMsg = "Upload failed: " + t.getMessage();
                                    android.util.Log.e("AttachmentUploader", errorMsg, t);
                                    callback.onError(errorMsg);
                                }
                            });

                        } catch (Exception e) {
                            String errorMsg = "Error reading file: " + e.getMessage();
                            android.util.Log.e("AttachmentUploader", errorMsg, e);
                            callback.onError(errorMsg);
                        }

                    } else {
                        String errorMsg = "Failed to get upload URL: " + response.code();
                        try {
                            if (response.errorBody() != null) {
                                errorMsg += " - " + response.errorBody().string();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        android.util.Log.e("AttachmentUploader", errorMsg);
                        callback.onError(errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<UploadUrlResponseDTO> call, Throwable t) {
                    String errorMsg = "Network error: " + t.getMessage();
                    android.util.Log.e("AttachmentUploader", errorMsg, t);
                    callback.onError(errorMsg);
                }
            });

        } catch (Exception e) {
            callback.onError("Error preparing upload: " + e.getMessage());
        }
    }

    private String getFileName(Uri uri) {
        // Try to get filename from ContentResolver first
        android.database.Cursor cursor = context.getContentResolver().query(
            uri, null, null, null, null
        );
        
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1 && !cursor.isNull(nameIndex)) {
                        String name = cursor.getString(nameIndex);
                        if (name != null && !name.isEmpty()) {
                            android.util.Log.d("AttachmentUploader", "File name from cursor: " + name);
                            return name;
                        }
                    }
                }
            } finally {
                cursor.close();
            }
        }
        
        // Fallback: Parse from URI path
        String path = uri.getPath();
        if (path != null) {
            int idx = path.lastIndexOf('/');
            if (idx >= 0 && idx + 1 < path.length()) {
                String name = path.substring(idx + 1);
                android.util.Log.d("AttachmentUploader", "File name from path: " + name);
                return name;
            }
        }
        
        android.util.Log.w("AttachmentUploader", "Cannot determine filename, using 'file'");
        return "file";
    }

    private long getFileSize(Uri uri) throws IOException {
        // Use ContentResolver to get accurate file size
        android.database.Cursor cursor = context.getContentResolver().query(
            uri, null, null, null, null
        );
        
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE);
                    if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                        long size = cursor.getLong(sizeIndex);
                        android.util.Log.d("AttachmentUploader", "File size from cursor: " + size);
                        return size;
                    }
                }
            } finally {
                cursor.close();
            }
        }
        
        // Fallback: Read actual bytes (accurate but slower)
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            android.util.Log.w("AttachmentUploader", "Cannot open input stream, defaulting to 0");
            return 0;
        }
        
        try {
            long size = 0;
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                size += bytesRead;
            }
            android.util.Log.d("AttachmentUploader", "File size from reading stream: " + size);
            return size;
        } finally {
            inputStream.close();
        }
    }

    private String getMimeType(Uri uri) {
        // Method 1: Get MIME type from ContentResolver (most accurate for content:// URIs)
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType != null && !mimeType.isEmpty()) {
            android.util.Log.d("AttachmentUploader", "MIME type from ContentResolver: " + mimeType);
            return mimeType;
        }
        
        // Method 2: Get from file extension
        String extension = null;
        
        // Try to get filename first
        android.database.Cursor cursor = context.getContentResolver().query(
            uri, null, null, null, null
        );
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1 && !cursor.isNull(nameIndex)) {
                        String fileName = cursor.getString(nameIndex);
                        if (fileName != null && fileName.contains(".")) {
                            extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
                        }
                    }
                }
            } finally {
                cursor.close();
            }
        }
        
        // Fallback: Parse extension from URI
        if (extension == null) {
            extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        }
        
        // Get MIME type from extension
        if (extension != null && !extension.isEmpty()) {
            String mime = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                android.util.Log.d("AttachmentUploader", "MIME type from extension '" + extension + "': " + mime);
                return mime;
            }
        }
        
        // Default fallback
        android.util.Log.w("AttachmentUploader", "Cannot determine MIME type, using application/octet-stream");
        return "application/octet-stream";
    }

    private byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }
}
