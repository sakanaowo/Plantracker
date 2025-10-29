package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.task.TaskDTO;
import com.example.tralalero.data.remote.dto.task.TaskCommentDTO;
import com.example.tralalero.data.remote.dto.task.AttachmentDTO;
import com.example.tralalero.data.remote.dto.task.CheckListDTO;
import com.example.tralalero.data.remote.dto.task.CheckListItemDTO;
import com.example.tralalero.data.remote.dto.ChecklistDTO;
import com.example.tralalero.data.remote.dto.ChecklistItemDTO;
import com.example.tralalero.data.remote.dto.CreateChecklistItemDTO;
import com.example.tralalero.data.remote.dto.UpdateChecklistItemDTO;

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

    /**
     * Move task to another board
     * Backend: POST /api/tasks/:id/move
     * 
     * @param taskId Task ID to move
     * @param moveData Map with "toBoardId", optional "beforeId", "afterId"
     * @return Updated task
     */
    @POST("tasks/{id}/move")
    Call<TaskDTO> moveTaskToBoard(
        @Path("id") String taskId,
        @Body java.util.Map<String, Object> moveData
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
    Call<List<ChecklistDTO>> getTaskChecklists(@Path("id") String taskId);

    /**
     * Create a new checklist for a task
     * POST /tasks/:taskId/checklists
     */
    @POST("tasks/{id}/checklists")
    Call<ChecklistDTO> addTaskChecklist(
        @Path("id") String taskId,
        @Body CheckListDTO checklist
    );

    /**
     * Create a new checklist item
     * POST /checklists/:id/items
     */
    @POST("checklists/{id}/items")
    Call<ChecklistItemDTO> createChecklistItem(
        @Path("id") String checklistId,
        @Body CreateChecklistItemDTO itemDto
    );

    /**
     * Update checklist item content
     * PATCH /checklist-items/:id
     */
    @PATCH("checklist-items/{id}")
    Call<ChecklistItemDTO> updateChecklistItem(
        @Path("id") String itemId,
        @Body UpdateChecklistItemDTO itemDto
    );

    /**
     * Toggle checklist item done/undone
     * PATCH /checklist-items/:id/toggle
     */
    @PATCH("checklist-items/{id}/toggle")
    Call<ChecklistItemDTO> toggleChecklistItem(
        @Path("id") String itemId
    );

    /**
     * Delete a checklist item
     * DELETE /checklist-items/:id
     */
    @DELETE("checklist-items/{id}")
    Call<Void> deleteChecklistItem(
        @Path("id") String itemId
    );
}
