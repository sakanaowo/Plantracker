package com.example.tralalero.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tralalero.domain.model.Label;

import com.example.tralalero.domain.usecase.label.AssignLabelToTaskUseCase;
import com.example.tralalero.domain.usecase.label.CreateLabelInProjectUseCase;
import com.example.tralalero.domain.usecase.label.GetLabelsByProjectUseCase;
import com.example.tralalero.domain.usecase.label.GetTaskLabelsUseCase;
import com.example.tralalero.domain.usecase.label.RemoveLabelFromTaskUseCase;
import com.example.tralalero.domain.usecase.label.GetLabelsByWorkspaceUseCase;
import com.example.tralalero.domain.usecase.label.GetLabelByIdUseCase;
import com.example.tralalero.domain.usecase.label.CreateLabelUseCase;
import com.example.tralalero.domain.usecase.label.UpdateLabelUseCase;
import com.example.tralalero.domain.usecase.label.DeleteLabelUseCase;

import java.util.List;

public class LabelViewModel extends ViewModel {

    private final GetLabelsByWorkspaceUseCase getLabelsByWorkspaceUseCase;
    private final GetLabelsByProjectUseCase getLabelsByProjectUseCase;
    private final GetLabelByIdUseCase getLabelByIdUseCase;
    private final CreateLabelUseCase createLabelUseCase;
    private final CreateLabelInProjectUseCase createLabelInProjectUseCase;
    private final UpdateLabelUseCase updateLabelUseCase;
    private final DeleteLabelUseCase deleteLabelUseCase;
    private final GetTaskLabelsUseCase getTaskLabelsUseCase;
    private final AssignLabelToTaskUseCase assignLabelToTaskUseCase;
    private final RemoveLabelFromTaskUseCase removeLabelFromTaskUseCase;

    private final MutableLiveData<List<Label>> labelsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Label>> taskLabelsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Label> selectedLabelLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccessLiveData = new MutableLiveData<>();

    public LabelViewModel(
            GetLabelsByWorkspaceUseCase getLabelsByWorkspaceUseCase,
            GetLabelsByProjectUseCase getLabelsByProjectUseCase,
            GetLabelByIdUseCase getLabelByIdUseCase,
            CreateLabelUseCase createLabelUseCase,
            CreateLabelInProjectUseCase createLabelInProjectUseCase,
            UpdateLabelUseCase updateLabelUseCase,
            DeleteLabelUseCase deleteLabelUseCase,
            GetTaskLabelsUseCase getTaskLabelsUseCase,
            AssignLabelToTaskUseCase assignLabelToTaskUseCase,
            RemoveLabelFromTaskUseCase removeLabelFromTaskUseCase
    ) {
        this.getLabelsByWorkspaceUseCase = getLabelsByWorkspaceUseCase;
        this.getLabelsByProjectUseCase = getLabelsByProjectUseCase;
        this.getLabelByIdUseCase = getLabelByIdUseCase;
        this.createLabelUseCase = createLabelUseCase;
        this.createLabelInProjectUseCase = createLabelInProjectUseCase;
        this.updateLabelUseCase = updateLabelUseCase;
        this.deleteLabelUseCase = deleteLabelUseCase;
        this.getTaskLabelsUseCase = getTaskLabelsUseCase;
        this.assignLabelToTaskUseCase = assignLabelToTaskUseCase;
        this.removeLabelFromTaskUseCase = removeLabelFromTaskUseCase;
    }

    public LiveData<List<Label>> getLabels() {
        return labelsLiveData;
    }

    public LiveData<List<Label>> getTaskLabels() {
        return taskLabelsLiveData;
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

    public LiveData<Boolean> getOperationSuccess() {
        return operationSuccessLiveData;
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
                loadLabelsByWorkspace(workspaceId);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    /**
     * Update a label (project-based)
     */
    public void updateLabel(String labelId, Label label, String projectId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        updateLabelUseCase.execute(labelId, label, new UpdateLabelUseCase.Callback<Label>() {
            @Override
            public void onSuccess(Label result) {
                loadingLiveData.setValue(false);
                selectedLabelLiveData.setValue(result);
                operationSuccessLiveData.setValue(true);
                // Refresh labels list
                loadLabelsByProject(projectId);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                operationSuccessLiveData.setValue(false);
            }
        });
    }

    /**
     * Delete a label (project-based)
     */
    public void deleteLabel(String labelId, String projectId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        deleteLabelUseCase.execute(labelId, new DeleteLabelUseCase.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadingLiveData.setValue(false);
                selectedLabelLiveData.setValue(null);
                operationSuccessLiveData.setValue(true);
                // Refresh labels list
                loadLabelsByProject(projectId);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                operationSuccessLiveData.setValue(false);
            }
        });
    }

    public void clearError() {
        errorLiveData.setValue(null);
    }

    // ========== New Project-based Methods ==========

    /**
     * Load all labels for a specific project
     */
    public void loadLabelsByProject(String projectId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getLabelsByProjectUseCase.execute(projectId, new GetLabelsByProjectUseCase.Callback<List<Label>>() {
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

    /**
     * Create a new label in a project
     */
    public void createLabelInProject(String projectId, Label label) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        createLabelInProjectUseCase.execute(projectId, label, new CreateLabelInProjectUseCase.Callback<Label>() {
            @Override
            public void onSuccess(Label result) {
                loadingLiveData.setValue(false);
                selectedLabelLiveData.setValue(result);
                operationSuccessLiveData.setValue(true);
                // Refresh labels list
                loadLabelsByProject(projectId);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                operationSuccessLiveData.setValue(false);
            }
        });
    }

    // ========== Task Label Methods ==========

    /**
     * Load all labels assigned to a task
     */
    public void loadTaskLabels(String taskId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getTaskLabelsUseCase.execute(taskId, new GetTaskLabelsUseCase.Callback<List<Label>>() {
            @Override
            public void onSuccess(List<Label> result) {
                loadingLiveData.setValue(false);
                taskLabelsLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    /**
     * Assign a label to a task
     */
    public void assignLabelToTask(String taskId, String labelId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        assignLabelToTaskUseCase.execute(taskId, labelId, new AssignLabelToTaskUseCase.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadingLiveData.setValue(false);
                operationSuccessLiveData.setValue(true);
                // DON'T auto reload - let UI decide when to reload
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                operationSuccessLiveData.setValue(false);
            }
        });
    }

    /**
     * Remove a label from a task
     */
    public void removeLabelFromTask(String taskId, String labelId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        removeLabelFromTaskUseCase.execute(taskId, labelId, new RemoveLabelFromTaskUseCase.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadingLiveData.setValue(false);
                operationSuccessLiveData.setValue(true);
                // DON'T auto reload - let UI decide when to reload
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                operationSuccessLiveData.setValue(false);
            }
        });
    }

    /**
     * Assign multiple labels to a task at once
     */
    public void assignMultipleLabelsToTask(String taskId, List<String> labelIds) {
        if (labelIds == null || labelIds.isEmpty()) {
            operationSuccessLiveData.setValue(true);
            return;
        }

        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        final int[] completed = {0};
        final int total = labelIds.size();

        for (String labelId : labelIds) {
            assignLabelToTaskUseCase.execute(taskId, labelId, new AssignLabelToTaskUseCase.Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    completed[0]++;
                    if (completed[0] == total) {
                        loadingLiveData.setValue(false);
                        operationSuccessLiveData.setValue(true);
                        loadTaskLabels(taskId);
                    }
                }

                @Override
                public void onError(String error) {
                    completed[0]++;
                    if (completed[0] == total) {
                        loadingLiveData.setValue(false);
                        errorLiveData.setValue("Some labels failed to assign: " + error);
                        loadTaskLabels(taskId);
                    }
                }
            });
        }
    }

    public void resetOperationSuccess() {
        operationSuccessLiveData.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
