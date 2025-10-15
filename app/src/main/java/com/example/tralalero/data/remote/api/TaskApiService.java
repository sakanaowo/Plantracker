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

    // ===== TASK ENDPOINTS =====

    /**
     * Get tasks by board
     * GET /tasks/by-board/{boardId}
     */
    @GET("tasks/by-board/{boardId}")
    Call<List<TaskDTO>> getTasksByBoard(@Path("boardId") String boardId);

    /**
     * Get task by ID
     * GET /tasks/{id}
     */
    @GET("tasks/{id}")
    Call<TaskDTO> getTaskById(@Path("id") String taskId);

    /**
     * Create a new task
     * POST /tasks
     */
    @POST("tasks")
    Call<TaskDTO> createTask(@Body TaskDTO task);

    /**
     * Update task
     * PATCH /tasks/{id}
     */
    @PATCH("tasks/{id}")
    Call<TaskDTO> updateTask(
        @Path("id") String taskId,
        @Body TaskDTO task
    );

    /**
     * Delete task
     * DELETE /tasks/{id}
     */
    @DELETE("tasks/{id}")
    Call<Void> deleteTask(@Path("id") String taskId);

    // ===== TASK COMMENTS =====

    /**
     * Get comments for a task
     * GET /tasks/{id}/comments
     */
    @GET("tasks/{id}/comments")
    Call<List<TaskCommentDTO>> getTaskComments(@Path("id") String taskId);

    /**
     * Add comment to task
     * POST /tasks/{id}/comments
     */
    @POST("tasks/{id}/comments")
    Call<TaskCommentDTO> addTaskComment(
        @Path("id") String taskId,
        @Body TaskCommentDTO comment
    );

    // ===== ATTACHMENTS =====

    /**
     * Get attachments for a task
     * GET /tasks/{id}/attachments
     */
    @GET("tasks/{id}/attachments")
    Call<List<AttachmentDTO>> getTaskAttachments(@Path("id") String taskId);

    /**
     * Add attachment to task
     * POST /tasks/{id}/attachments
     */
    @POST("tasks/{id}/attachments")
    Call<AttachmentDTO> addTaskAttachment(
        @Path("id") String taskId,
        @Body AttachmentDTO attachment
    );

    // ===== CHECKLISTS =====

    /**
     * Get checklists for a task
     * GET /tasks/{id}/checklists
     */
    @GET("tasks/{id}/checklists")
    Call<List<CheckListDTO>> getTaskChecklists(@Path("id") String taskId);

    /**
     * Add checklist to task
     * POST /tasks/{id}/checklists
     */
    @POST("tasks/{id}/checklists")
    Call<CheckListDTO> addTaskChecklist(
        @Path("id") String taskId,
        @Body CheckListDTO checklist
    );

    /**
     * Update checklist item
     * PATCH /checklists/{checklistId}/items/{itemId}
     */
    @PATCH("checklists/{checklistId}/items/{itemId}")
    Call<CheckListItemDTO> updateChecklistItem(
        @Path("checklistId") String checklistId,
        @Path("itemId") String itemId,
        @Body CheckListItemDTO item
    );
}
