package com.example.tralalero.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tralalero.domain.model.Label;

import com.example.tralalero.domain.usecase.label.GetLabelsByWorkspaceUseCase;
import com.example.tralalero.domain.usecase.label.GetLabelByIdUseCase;
import com.example.tralalero.domain.usecase.label.CreateLabelUseCase;
import com.example.tralalero.domain.usecase.label.UpdateLabelUseCase;
import com.example.tralalero.domain.usecase.label.DeleteLabelUseCase;

import java.util.List;

public class LabelViewModel extends ViewModel {

    private final GetLabelsByWorkspaceUseCase getLabelsByWorkspaceUseCase;
    private final GetLabelByIdUseCase getLabelByIdUseCase;
    private final CreateLabelUseCase createLabelUseCase;
    private final UpdateLabelUseCase updateLabelUseCase;
    private final DeleteLabelUseCase deleteLabelUseCase;

    private final MutableLiveData<List<Label>> labelsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Label> selectedLabelLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public LabelViewModel(
            GetLabelsByWorkspaceUseCase getLabelsByWorkspaceUseCase,
            GetLabelByIdUseCase getLabelByIdUseCase,
            CreateLabelUseCase createLabelUseCase,
            UpdateLabelUseCase updateLabelUseCase,
            DeleteLabelUseCase deleteLabelUseCase
    ) {
        this.getLabelsByWorkspaceUseCase = getLabelsByWorkspaceUseCase;
        this.getLabelByIdUseCase = getLabelByIdUseCase;
        this.createLabelUseCase = createLabelUseCase;
        this.updateLabelUseCase = updateLabelUseCase;
        this.deleteLabelUseCase = deleteLabelUseCase;
    }

    public LiveData<List<Label>> getLabels() {
        return labelsLiveData;
    }

    public LiveData<Label> getSelectedLabel() {
        return selectedLabelLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return loadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }


    public void loadLabelsByWorkspace(String workspaceId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getLabelsByWorkspaceUseCase.execute(workspaceId, new GetLabelsByWorkspaceUseCase.Callback<List<Label>>() {
            @Override
            public void onSuccess(List<Label> result) {
                loadingLiveData.setValue(false);
                labelsLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void loadLabelById(String labelId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getLabelByIdUseCase.execute(labelId, new GetLabelByIdUseCase.Callback<Label>() {
            @Override
            public void onSuccess(Label result) {
                loadingLiveData.setValue(false);
                selectedLabelLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void createLabel(String workspaceId, Label label) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        createLabelUseCase.execute(workspaceId, label, new CreateLabelUseCase.Callback<Label>() {
            @Override
            public void onSuccess(Label result) {
                loadingLiveData.setValue(false);
                selectedLabelLiveData.setValue(result);
                // Reload labels
                loadLabelsByWorkspace(workspaceId);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void updateLabel(String labelId, Label label, String workspaceId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        updateLabelUseCase.execute(labelId, label, new UpdateLabelUseCase.Callback<Label>() {
            @Override
            public void onSuccess(Label result) {
                loadingLiveData.setValue(false);
                selectedLabelLiveData.setValue(result);
                // Reload labels
                loadLabelsByWorkspace(workspaceId);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void deleteLabel(String labelId, String workspaceId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        deleteLabelUseCase.execute(labelId, new DeleteLabelUseCase.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadingLiveData.setValue(false);
                selectedLabelLiveData.setValue(null);
                // Reload labels
                loadLabelsByWorkspace(workspaceId);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void clearError() {
        errorLiveData.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
