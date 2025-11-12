package com.example.tralalero.data.repository;

import android.content.Context;

import com.example.tralalero.App.App;
import com.example.tralalero.data.mapper.ProjectMapper;
import com.example.tralalero.data.remote.api.ProjectApiService;
import com.example.tralalero.data.remote.dto.project.ProjectDTO;
import com.example.tralalero.domain.model.Project;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.domain.repository.IProjectRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectRepositoryImpl implements IProjectRepository {
    private final ProjectApiService apiService;

    public ProjectRepositoryImpl(ProjectApiService apiService) {
        this.apiService = apiService;
    }
    
    public ProjectRepositoryImpl(Context context) {
        // ✅ FIX: Use authenticated ApiClient with FirebaseInterceptor
        this.apiService = ApiClient.get(App.authManager).create(ProjectApiService.class);
    }

    @Override
    public void getProjectById(String projectId, RepositoryCallback<Project> callback) {
        apiService.getProjectById(projectId).enqueue(new Callback<ProjectDTO>() {
            @Override
            public void onResponse(Call<ProjectDTO> call, Response<ProjectDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(ProjectMapper.toDomain(response.body()));
                } else {
                    callback.onError("Project not found: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ProjectDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void createProject(String workspaceId, Project project, RepositoryCallback<Project> callback) {
        ProjectDTO dto = ProjectMapper.toDto(project);
        dto.setWorkspaceId(workspaceId);

        apiService.createProject(dto).enqueue(new Callback<ProjectDTO>() {
            @Override
            public void onResponse(Call<ProjectDTO> call, Response<ProjectDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(ProjectMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to create project: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ProjectDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void updateProject(String projectId, Project project, RepositoryCallback<Project> callback) {
        ProjectDTO dto = ProjectMapper.toDto(project);

        apiService.updateProject(projectId, dto).enqueue(new Callback<ProjectDTO>() {
            @Override
            public void onResponse(Call<ProjectDTO> call, Response<ProjectDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(ProjectMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to update project: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ProjectDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void deleteProject(String projectId, RepositoryCallback<Void> callback) {
        apiService.deleteProject(projectId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to delete project: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void updateProjectKey(String projectId, String newKey, RepositoryCallback<Project> callback) {
        ProjectDTO dto = new ProjectDTO();
        dto.setKey(newKey);

        apiService.updateProject(projectId, dto).enqueue(new Callback<ProjectDTO>() {
            @Override
            public void onResponse(Call<ProjectDTO> call, Response<ProjectDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(ProjectMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to update project key: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ProjectDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void updateBoardType(String projectId, String boardType, RepositoryCallback<Project> callback) {
        ProjectDTO dto = new ProjectDTO();
        dto.setBoardType(boardType);

        apiService.updateProject(projectId, dto).enqueue(new Callback<ProjectDTO>() {
            @Override
            public void onResponse(Call<ProjectDTO> call, Response<ProjectDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(ProjectMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to update board type: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ProjectDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    @Override
    public void getProjectSummary(String projectId, RepositoryCallback<com.example.tralalero.data.dto.project.ProjectSummaryResponse> callback) {
        apiService.getProjectSummary(projectId).enqueue(new Callback<com.example.tralalero.data.dto.project.ProjectSummaryResponse>() {
            @Override
            public void onResponse(Call<com.example.tralalero.data.dto.project.ProjectSummaryResponse> call, 
                                 Response<com.example.tralalero.data.dto.project.ProjectSummaryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get project summary: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<com.example.tralalero.data.dto.project.ProjectSummaryResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    @Override
    public void getAllUserProjects(RepositoryCallback<java.util.List<Project>> callback) {
        apiService.getAllUserProjects().enqueue(new Callback<java.util.List<ProjectDTO>>() {
            @Override
            public void onResponse(Call<java.util.List<ProjectDTO>> call, Response<java.util.List<ProjectDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    java.util.List<Project> projects = new java.util.ArrayList<>();
                    for (ProjectDTO dto : response.body()) {
                        projects.add(ProjectMapper.toDomain(dto));
                    }
                    callback.onSuccess(projects);
                } else {
                    callback.onError("Failed to get user projects: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<ProjectDTO>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}
