package com.example.tralalero.data.repository;

import com.example.tralalero.data.mapper.LabelMapper;
import com.example.tralalero.data.remote.api.LabelApiService;
import com.example.tralalero.data.remote.dto.label.AssignLabelRequest;
import com.example.tralalero.data.remote.dto.label.CreateLabelRequest;
import com.example.tralalero.data.remote.dto.label.LabelDTO;
import com.example.tralalero.data.remote.dto.label.UpdateLabelRequest;
import com.example.tralalero.domain.model.Label;
import com.example.tralalero.domain.repository.ILabelRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LabelRepositoryImpl implements ILabelRepository {
    private final LabelApiService apiService;

    public LabelRepositoryImpl(LabelApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void getLabelsByWorkspace(String workspaceId, RepositoryCallback<List<Label>> callback) {
        apiService.getLabelsByWorkspace(workspaceId).enqueue(new Callback<List<LabelDTO>>() {
            @Override
            public void onResponse(Call<List<LabelDTO>> call, Response<List<LabelDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(LabelMapper.toDomainList(response.body()));
                } else {
                    callback.onError("Failed to fetch labels: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<LabelDTO>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void getLabelsByProject(String projectId, RepositoryCallback<List<Label>> callback) {
        android.util.Log.d("LabelRepository", "Fetching labels for projectId: " + projectId);
        apiService.getLabelsByProject(projectId).enqueue(new Callback<List<LabelDTO>>() {
            @Override
            public void onResponse(Call<List<LabelDTO>> call, Response<List<LabelDTO>> response) {
                android.util.Log.d("LabelRepository", "Response code: " + response.code() + ", URL: " + call.request().url());
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(LabelMapper.toDomainList(response.body()));
                } else {
                    callback.onError("Failed to fetch project labels: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<LabelDTO>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void getLabelById(String labelId, RepositoryCallback<Label> callback) {
        apiService.getLabelById(labelId).enqueue(new Callback<LabelDTO>() {
            @Override
            public void onResponse(Call<LabelDTO> call, Response<LabelDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(LabelMapper.toDomain(response.body()));
                } else {
                    callback.onError("Label not found: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LabelDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void createLabel(String workspaceId, Label label, RepositoryCallback<Label> callback) {
        LabelDTO dto = LabelMapper.toDto(label);
        dto.setWorkspaceId(workspaceId);

        apiService.createLabel(dto).enqueue(new Callback<LabelDTO>() {
            @Override
            public void onResponse(Call<LabelDTO> call, Response<LabelDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(LabelMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to create label: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LabelDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void createLabelInProject(String projectId, Label label, RepositoryCallback<Label> callback) {
        CreateLabelRequest request = new CreateLabelRequest(label.getName(), label.getColor());
        android.util.Log.d("LabelRepository", "Creating label in project: " + projectId + ", name: " + label.getName() + ", color: " + label.getColor());

        apiService.createLabelInProject(projectId, request).enqueue(new Callback<LabelDTO>() {
            @Override
            public void onResponse(Call<LabelDTO> call, Response<LabelDTO> response) {
                android.util.Log.d("LabelRepository", "Create label response code: " + response.code() + ", URL: " + call.request().url());
                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d("LabelRepository", "Label created successfully: " + response.body().getId());
                    callback.onSuccess(LabelMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to create label in project: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LabelDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void updateLabel(String labelId, Label label, RepositoryCallback<Label> callback) {
        UpdateLabelRequest request = new UpdateLabelRequest(label.getName(), label.getColor());
        android.util.Log.d("LabelRepository", "Updating label: " + labelId + ", name: " + label.getName() + ", color: " + label.getColor());

        apiService.updateLabelNew(labelId, request).enqueue(new Callback<LabelDTO>() {
            @Override
            public void onResponse(Call<LabelDTO> call, Response<LabelDTO> response) {
                android.util.Log.d("LabelRepository", "Update label response code: " + response.code() + ", URL: " + call.request().url());
                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d("LabelRepository", "Label updated successfully: " + response.body().getId());
                    callback.onSuccess(LabelMapper.toDomain(response.body()));
                } else {
                    callback.onError("Failed to update label: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LabelDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void deleteLabel(String labelId, RepositoryCallback<Void> callback) {
        android.util.Log.d("LabelRepository", "Deleting label: " + labelId);
        
        apiService.deleteLabelNew(labelId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                android.util.Log.d("LabelRepository", "Delete label response code: " + response.code() + ", URL: " + call.request().url());
                if (response.isSuccessful()) {
                    android.util.Log.d("LabelRepository", "Label deleted successfully");
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to delete label: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // ========== Task Label Operations ==========

    @Override
    public void getTaskLabels(String taskId, RepositoryCallback<List<Label>> callback) {
        apiService.getTaskLabels(taskId).enqueue(new Callback<List<LabelDTO>>() {
            @Override
            public void onResponse(Call<List<LabelDTO>> call, Response<List<LabelDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(LabelMapper.toDomainList(response.body()));
                } else {
                    callback.onError("Failed to fetch task labels: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<LabelDTO>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void assignLabelToTask(String taskId, String labelId, RepositoryCallback<Void> callback) {
        AssignLabelRequest request = new AssignLabelRequest(labelId);

        apiService.assignLabelToTask(taskId, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to assign label to task: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void removeLabelFromTask(String taskId, String labelId, RepositoryCallback<Void> callback) {
        apiService.removeLabelFromTask(taskId, labelId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to remove label from task: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}

