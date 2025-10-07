package com.example.tralalero.network.api;

import com.example.tralalero.model.Workspace;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface HomeApiService {
    @GET("workspaces")
    Call<List<Workspace>> getWorkspaces();
}
