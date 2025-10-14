package com.example.tralalero.data.repository;

import com.example.tralalero.data.mapper.BoardMapper;
import com.example.tralalero.data.mapper.ProjectMapper;
import com.example.tralalero.data.mapper.WorkspaceMapper;
import com.example.tralalero.data.remote.dto.board.BoardDTO;
import com.example.tralalero.data.remote.dto.project.ProjectDTO;
import com.example.tralalero.data.remote.dto.workspace.WorkspaceDTO;
import com.example.tralalero.domain.model.Board;
import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.model.Workspace;
import com.example.tralalero.domain.repository.IWorkspaceRepository;
import com.example.tralalero.data.remote.api.WorkspaceApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkspaceRepositoryImpl implements IWorkspaceRepository {
    private final WorkspaceApiService apiService;

    public WorkspaceRepositoryImpl(WorkspaceApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void getWorkspaces(IWorkspaceRepository.RepositoryCallback<List<Workspace>> callback) {
        apiService.getWorkspaces().enqueue(new Callback<List<WorkspaceDTO>>() {
            @Override
            public void onResponse(Call<List<WorkspaceDTO>> call, Response<List<WorkspaceDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Workspace> workspaces = WorkspaceMapper.toDomainList(response.body());
                    callback.onSuccess(workspaces);
                } else {
                    callback.onError("Failed to fetch workspaces: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<WorkspaceDTO>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void getWorkspaceById(String workspaceId, IWorkspaceRepository.RepositoryCallback<Workspace> callback) {
        apiService.getWorkspaceById(workspaceId).enqueue(new Callback<WorkspaceDTO>() {
            @Override
            public void onResponse(Call<WorkspaceDTO> call, Response<WorkspaceDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Workspace workspace = WorkspaceMapper.toDomain(response.body());
                    callback.onSuccess(workspace);
                } else {
                    callback.onError("Workspace not found");
                }
            }

            @Override
            public void onFailure(Call<WorkspaceDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void createWorkspace(Workspace workspace, IWorkspaceRepository.RepositoryCallback<Workspace> callback) {
        WorkspaceDTO dto = WorkspaceMapper.toDTO(workspace);
        apiService.createWorkspace(dto).enqueue(new Callback<WorkspaceDTO>() {
            @Override
            public void onResponse(Call<WorkspaceDTO> call, Response<WorkspaceDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Workspace createdWorkspace = WorkspaceMapper.toDomain(response.body());
                    callback.onSuccess(createdWorkspace);
                } else {
                    callback.onError("Failed to create workspace");
                }
            }

            @Override
            public void onFailure(Call<WorkspaceDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void updateWorkspace(String workspaceId, Workspace workspace, IWorkspaceRepository.RepositoryCallback<Workspace> callback) {
        WorkspaceDTO dto = WorkspaceMapper.toDTO(workspace);
        apiService.updateWorkspace(workspaceId, dto).enqueue(new Callback<WorkspaceDTO>() {
            @Override
            public void onResponse(Call<WorkspaceDTO> call, Response<WorkspaceDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Workspace updatedWorkspace = WorkspaceMapper.toDomain(response.body());
                    callback.onSuccess(updatedWorkspace);
                } else {
                    callback.onError("Failed to update workspace: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<WorkspaceDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void deleteWorkspace(String workspaceId, IWorkspaceRepository.RepositoryCallback<Void> callback) {
        apiService.deleteWorkspace(workspaceId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to delete workspace: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void getProjects(String workspaceId, IWorkspaceRepository.RepositoryCallback<List<Project>> callback) {
        apiService.getProjects(workspaceId).enqueue(new Callback<List<ProjectDTO>>() {
            @Override
            public void onResponse(Call<List<ProjectDTO>> call, Response<List<ProjectDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Project> projects = ProjectMapper.toDomainList(response.body());
                    callback.onSuccess(projects);
                } else {
                    callback.onError("Failed to fetch projects");
                }
            }

            @Override
            public void onFailure(Call<List<ProjectDTO>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void getBoards(String projectId, IWorkspaceRepository.RepositoryCallback<List<Board>> callback) {
        apiService.getBoards(projectId).enqueue(new Callback<List<BoardDTO>>() {
            @Override
            public void onResponse(Call<List<BoardDTO>> call, Response<List<BoardDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Board> boards = BoardMapper.toDomainList(response.body());
                    callback.onSuccess(boards);
                } else {
                    callback.onError("Failed to fetch boards");
                }
            }

            @Override
            public void onFailure(Call<List<BoardDTO>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

}
