package com.example.tralalero.feature.home.api;

import com.example.tralalero.feature.home.model.Workspace;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface WorkspaceApiService {
    @GET("api/workspaces")
    Call<List<Workspace>> getWorkspaces();
}