package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.task.AttachmentDTO;
import com.example.tralalero.data.remote.dto.task.UploadUrlResponseDTO;
import com.example.tralalero.data.remote.dto.task.ViewUrlResponseDTO;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Url;

import java.util.List;

public interface AttachmentApiService {

    @POST("tasks/{taskId}/attachments/upload-url")
    Call<UploadUrlResponseDTO> requestUploadUrl(
        @Path("taskId") String taskId,
        @Body java.util.Map<String, Object> dto
    );

    @GET("tasks/{taskId}/attachments")
    Call<List<AttachmentDTO>> listAttachments(@Path("taskId") String taskId);

    @GET("attachments/{attachmentId}/view")
    Call<ViewUrlResponseDTO> getViewUrl(@Path("attachmentId") String attachmentId);

    @DELETE("attachments/{attachmentId}")
    Call<Void> deleteAttachment(@Path("attachmentId") String attachmentId);

    // Upload to signed URL (full URL passed via @Url)
    @PUT
    Call<Void> uploadToSignedUrl(
        @Url String url,
        @Body RequestBody file
    );
}
