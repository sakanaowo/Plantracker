package com.example.tralalero.data.remote.api;

import com.example.tralalero.domain.model.Workspace;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface HomeApiService {
    @GET("workspaces")
    Call<List<Workspace>> getWorkspaces();
}
