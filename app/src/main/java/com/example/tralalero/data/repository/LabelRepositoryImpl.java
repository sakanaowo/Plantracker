package com.example.tralalero.data.repository;

import com.example.tralalero.data.mapper.LabelMapper;
import com.example.tralalero.data.remote.api.LabelApiService;
import com.example.tralalero.data.remote.dto.label.LabelDTO;
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
        LabelDTO dto = LabelMapper.toDTO(label);
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
    public void updateLabel(String labelId, Label label, RepositoryCallback<Label> callback) {
        LabelDTO dto = LabelMapper.toDTO(label);

        apiService.updateLabel(labelId, dto).enqueue(new Callback<LabelDTO>() {
            @Override
            public void onResponse(Call<LabelDTO> call, Response<LabelDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
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
        apiService.deleteLabel(labelId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
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
}

