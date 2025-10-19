package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.workspace.WorkspaceDTO;
import com.example.tralalero.data.remote.dto.workspace.MembershipDTO;
import com.example.tralalero.data.remote.dto.project.ProjectDTO;
import com.example.tralalero.data.remote.dto.board.BoardDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface WorkspaceApiService {


    @POST("workspaces/users/{userId}/personal")
    Call<WorkspaceDTO> createPersonalWorkspace(
        @Path("userId") String userId,
        @Body WorkspaceDTO workspace
    );


    @POST("workspaces")
    Call<WorkspaceDTO> createWorkspace(@Body WorkspaceDTO workspace);


    @GET("workspaces")
    Call<List<WorkspaceDTO>> getWorkspaces();


    @GET("workspaces/{id}")
    Call<WorkspaceDTO> getWorkspaceById(@Path("id") String workspaceId);


    @PATCH("workspaces/{id}")
    Call<WorkspaceDTO> updateWorkspace(
        @Path("id") String workspaceId,
        @Body WorkspaceDTO workspace
    );


    @DELETE("workspaces/{id}")
    Call<Void> deleteWorkspace(@Path("id") String workspaceId);


    @GET("workspaces/{id}/members")
    Call<List<MembershipDTO>> getWorkspaceMembers(@Path("id") String workspaceId);

 
    @POST("workspaces/{id}/members")
    Call<MembershipDTO> addWorkspaceMember(
        @Path("id") String workspaceId,
        @Body MembershipDTO membership
    );


    @DELETE("workspaces/{id}/members/{userId}")
    Call<Void> removeWorkspaceMember(
        @Path("id") String workspaceId,
        @Path("userId") String userId
    );


    @GET("projects")
    Call<List<ProjectDTO>> getProjects(@Query("workspaceId") String workspaceId);


    @GET("boards")
    Call<List<BoardDTO>> getBoards(@Query("projectId") String projectId);
}
