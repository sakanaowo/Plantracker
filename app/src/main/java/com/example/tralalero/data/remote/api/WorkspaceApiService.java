package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.workspace.WorkspaceDTO;
import com.example.tralalero.data.remote.dto.workspace.MembershipDTO;
import com.example.tralalero.data.remote.dto.project.ProjectDTO;
import com.example.tralalero.data.remote.dto.board.BoardDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface WorkspaceApiService {

    // ===== WORKSPACE ENDPOINTS =====

    /**
     * Create or ensure personal workspace for a user
     * POST /workspaces/users/{userId}/personal
     */
    @POST("workspaces/users/{userId}/personal")
    Call<WorkspaceDTO> createPersonalWorkspace(
        @Path("userId") String userId,
        @Body WorkspaceDTO workspace
    );

    /**
     * Create a new workspace
     * POST /workspaces
     */
    @POST("workspaces")
    Call<WorkspaceDTO> createWorkspace(@Body WorkspaceDTO workspace);

    /**
     * List workspaces the caller belongs to
     * GET /workspaces
     */
    @GET("workspaces")
    Call<List<WorkspaceDTO>> getWorkspaces();

    /**
     * Fetch workspace details
     * GET /workspaces/{id}
     */
    @GET("workspaces/{id}")
    Call<WorkspaceDTO> getWorkspaceById(@Path("id") String workspaceId);

    /**
     * Update workspace metadata
     * PATCH /workspaces/{id}
     */
    @PATCH("workspaces/{id}")
    Call<WorkspaceDTO> updateWorkspace(
        @Path("id") String workspaceId,
        @Body WorkspaceDTO workspace
    );

    /**
     * Delete a workspace
     * DELETE /workspaces/{id}
     */
    @DELETE("workspaces/{id}")
    Call<Void> deleteWorkspace(@Path("id") String workspaceId);

    // ===== MEMBERS ENDPOINTS =====

    /**
     * List members of the workspace
     * GET /workspaces/{id}/members
     */
    @GET("workspaces/{id}/members")
    Call<List<MembershipDTO>> getWorkspaceMembers(@Path("id") String workspaceId);

    /**
     * Add a member to the workspace
     * POST /workspaces/{id}/members
     */
    @POST("workspaces/{id}/members")
    Call<MembershipDTO> addWorkspaceMember(
        @Path("id") String workspaceId,
        @Body MembershipDTO membership
    );

    /**
     * Remove a member from the workspace
     * DELETE /workspaces/{id}/members/{userId}
     */
    @DELETE("workspaces/{id}/members/{userId}")
    Call<Void> removeWorkspaceMember(
        @Path("id") String workspaceId,
        @Path("userId") String userId
    );

    // ===== PROJECTS ENDPOINTS =====

    /**
     * Get projects by workspace
     * GET /projects
     */
    @GET("projects")
    Call<List<ProjectDTO>> getProjects(@Query("workspaceId") String workspaceId);

    // ===== BOARDS ENDPOINTS =====

    /**
     * Get boards by project
     * GET /boards
     */
    @GET("boards")
    Call<List<BoardDTO>> getBoards(@Query("projectId") String projectId);
}
