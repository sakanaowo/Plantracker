package com.example.tralalero.network.api;

import com.example.tralalero.model.Workspace;
import com.example.tralalero.model.Project;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface WorkspaceApiService {
    // GET danh sách projects theo workspaceId (Query Parameter)
    @GET("projects")
    Call<List<Project>> getProjects(@Query("workspaceId") String workspaceId);

    // POST tạo project mới
    @POST("projects")
    Call<Project> createProject(@Body Project project);
}
