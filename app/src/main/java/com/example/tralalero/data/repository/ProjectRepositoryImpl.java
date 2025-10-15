package com.example.tralalero.data.repository;

import com.example.tralalero.data.mapper.ProjectMapper;
import com.example.tralalero.data.remote.api.ProjectApiService;
import com.example.tralalero.data.remote.dto.project.ProjectDTO;
import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.repository.IProjectRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectRepositoryImpl implements IProjectRepository {
    private final ProjectApiService apiService;

    public ProjectRepositoryImpl(ProjectApiService apiService) {
        this.apiService = apiService;
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
        ProjectDTO dto = ProjectMapper.toDTO(project);
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
        ProjectDTO dto = ProjectMapper.toDTO(project);

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
}
