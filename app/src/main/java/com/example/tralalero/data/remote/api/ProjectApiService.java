package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.project.ProjectDTO;
import com.example.tralalero.data.remote.dto.project.ProjectMemberDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface ProjectApiService {


    @GET("projects")
    Call<List<ProjectDTO>> getProjectsByWorkspace(@Query("workspaceId") String workspaceId);


    @GET("projects/{id}")
    Call<ProjectDTO> getProjectById(@Path("id") String projectId);

    @GET("projects/{id}/members")
    Call<List<ProjectMemberDTO>> getProjectMembers(@Path("id") String projectId);


    @POST("projects")
    Call<ProjectDTO> createProject(@Body ProjectDTO project);

 
    @PATCH("projects/{id}")
    Call<ProjectDTO> updateProject(
        @Path("id") String projectId,
        @Body ProjectDTO project
    );


    @DELETE("projects/{id}")
    Call<Void> deleteProject(@Path("id") String projectId);
}

