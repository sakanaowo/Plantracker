package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.task.TaskDTO;
import com.example.tralalero.data.remote.dto.task.TaskCommentDTO;
import com.example.tralalero.data.remote.dto.task.AttachmentDTO;
import com.example.tralalero.data.remote.dto.task.CheckListDTO;
import com.example.tralalero.data.remote.dto.task.CheckListItemDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface TaskApiService {

 
    @GET("tasks/by-board/{boardId}")
    Call<List<TaskDTO>> getTasksByBoard(@Path("boardId") String boardId);


    @GET("tasks/{id}")
    Call<TaskDTO> getTaskById(@Path("id") String taskId);


    @POST("tasks")
    Call<TaskDTO> createTask(@Body TaskDTO task);

    /**
     * Get all quick tasks from user's default board (To Do board)
     * Backend will find user's personal workspace, default project, and "To Do" board
     * 
     * @return List of quick tasks
     */
    @GET("tasks/quick/defaults")
    Call<List<TaskDTO>> getQuickTasks();

    /**
     * Create a quick task - automatically assigns to default project/board
     * Backend will find user's personal workspace, default project, and "To Do" board
     * 
     * @param quickTaskData Map with "title" (required) and "description" (optional)
     * @return Created task
     */
    @POST("tasks/quick")
    Call<TaskDTO> createQuickTask(@Body java.util.Map<String, String> quickTaskData);

 
    @PATCH("tasks/{id}")
    Call<TaskDTO> updateTask(
        @Path("id") String taskId,
        @Body TaskDTO task
    );

 
    @DELETE("tasks/{id}")
    Call<Void> deleteTask(@Path("id") String taskId);


    @GET("tasks/{id}/comments")
    Call<List<TaskCommentDTO>> getTaskComments(@Path("id") String taskId);


    @POST("tasks/{id}/comments")
    Call<TaskCommentDTO> addTaskComment(
        @Path("id") String taskId,
        @Body TaskCommentDTO comment
    );


    @GET("tasks/{id}/attachments")
    Call<List<AttachmentDTO>> getTaskAttachments(@Path("id") String taskId);

 
    @POST("tasks/{id}/attachments")
    Call<AttachmentDTO> addTaskAttachment(
        @Path("id") String taskId,
        @Body AttachmentDTO attachment
    );


    @GET("tasks/{id}/checklists")
    Call<List<CheckListDTO>> getTaskChecklists(@Path("id") String taskId);

 
    @POST("tasks/{id}/checklists")
    Call<CheckListDTO> addTaskChecklist(
        @Path("id") String taskId,
        @Body CheckListDTO checklist
    );


    @PATCH("checklists/{checklistId}/items/{itemId}")
    Call<CheckListItemDTO> updateChecklistItem(
        @Path("checklistId") String checklistId,
        @Path("itemId") String itemId,
        @Body CheckListItemDTO item
    );
}
