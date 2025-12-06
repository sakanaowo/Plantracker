package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.task.TaskCommentDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.List;

public interface CommentApiService {

    @GET("tasks/{taskId}/comments")
    Call<List<TaskCommentDTO>> getTaskComments(@Path("taskId") String taskId);

    @POST("tasks/{taskId}/comments")
    Call<TaskCommentDTO> addTaskComment(@Path("taskId") String taskId, @Body TaskCommentDTO comment);

    @PATCH("comments/{commentId}")
    Call<TaskCommentDTO> updateComment(@Path("commentId") String commentId, @Body TaskCommentDTO body);

    @DELETE("comments/{commentId}")
    Call<Void> deleteComment(@Path("commentId") String commentId);
}
