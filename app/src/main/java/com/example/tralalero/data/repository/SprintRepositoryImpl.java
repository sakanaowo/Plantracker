package com.example.tralalero.data.repository;

import com.example.tralalero.data.mapper.SprintMapper;
import com.example.tralalero.data.remote.api.SprintApiService;
import com.example.tralalero.data.remote.dto.sprint.SprintDTO;
import com.example.tralalero.domain.model.Sprint;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.domain.repository.ISprintRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SprintRepositoryImpl implements ISprintRepository {
    private final SprintApiService apiService;

    public SprintRepositoryImpl(SprintApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void getSprintsByProject(String projectId, RepositoryCallback<List<Sprint>> callback) {
        apiService.getSprintsByProject(projectId).enqueue(new Callback<List<SprintDTO>>() {
            @Override
            public void onResponse(Call<List<SprintDTO>> call, Response<List<SprintDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(SprintMapper.toDomainList(response.body()));
                } else {
                    callback.onError("Failed to fetch sprints: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<SprintDTO>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void getSprintById(String sprintId, RepositoryCallback<Sprint> callback) {
        apiService.getSprintById(sprintId).enqueue(new Callback<SprintDTO>() {
            @Override
            public void onResponse(Call<SprintDTO> call, Response<SprintDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(SprintMapper.toDomain(response.body()));
                } else {
                    callback.onError("Sprint not found: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<SprintDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void getActiveSprint(String projectId, RepositoryCallback<Sprint> callback) {
        apiService.getActiveSprint(projectId).enqueue(new Callback<SprintDTO>() {
            @Override
            public void onResponse(Call<SprintDTO> call, Response<SprintDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(SprintMapper.toDomain(response.body()));
                } else {
                    callback.onError("No active sprint found: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<SprintDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void createSprint(String projectId, Sprint sprint, RepositoryCallback<Sprint> callback) {
        SprintDTO dto = SprintMapper.toDTO(sprint);
        dto.setProjectId(projectId);

        apiService.createSprint(dto).enqueue(new Callback<SprintDTO>() {
            @Override
            public void onResponse(Call<SprintDTO> call, Response<SprintDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(SprintMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to create sprint: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<SprintDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void updateSprint(String sprintId, Sprint sprint, RepositoryCallback<Sprint> callback) {
        SprintDTO dto = SprintMapper.toDTO(sprint);

        apiService.updateSprint(sprintId, dto).enqueue(new Callback<SprintDTO>() {
            @Override
            public void onResponse(Call<SprintDTO> call, Response<SprintDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(SprintMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to update sprint: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<SprintDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void deleteSprint(String sprintId, RepositoryCallback<Void> callback) {
        apiService.deleteSprint(sprintId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to delete sprint: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void startSprint(String sprintId, RepositoryCallback<Sprint> callback) {
        apiService.startSprint(sprintId).enqueue(new Callback<SprintDTO>() {
            @Override
            public void onResponse(Call<SprintDTO> call, Response<SprintDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(SprintMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to start sprint: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<SprintDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void completeSprint(String sprintId, RepositoryCallback<Sprint> callback) {
        apiService.completeSprint(sprintId).enqueue(new Callback<SprintDTO>() {
            @Override
            public void onResponse(Call<SprintDTO> call, Response<SprintDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(SprintMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to complete sprint: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<SprintDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void addTaskToSprint(String sprintId, String taskId, RepositoryCallback<Void> callback) {
        callback.onError("Add task to sprint not yet implemented in API");
    }

    @Override
    public void removeTaskFromSprint(String taskId, RepositoryCallback<Void> callback) {
        callback.onError("Remove task from sprint not yet implemented in API");
    }

    @Override
    public void getSprintTasks(String sprintId, RepositoryCallback<List<Task>> callback) {
        callback.onError("Get sprint tasks not yet implemented in API");
    }
}

