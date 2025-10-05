package com.example.tralalero.network.api;

import com.example.tralalero.model.Workspace.Workspace;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface WorkspaceApiService {
    @GET("workspaces")
    Call<List<Workspace>> getWorkspaces();
}
