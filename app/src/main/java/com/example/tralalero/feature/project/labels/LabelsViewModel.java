package com.example.tralalero.feature.project.labels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.tralalero.domain.model.Label;
import com.example.tralalero.domain.repository.ILabelRepository;

import java.util.List;

public class LabelsViewModel extends ViewModel {
    
    private ILabelRepository repository;
    private MutableLiveData<List<Label>> labelsLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private MutableLiveData<String> successLiveData = new MutableLiveData<>();

    public LabelsViewModel(ILabelRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Label>> getLabels() {
        return labelsLiveData;
    }

    public LiveData<Boolean> getLoading() {
        return loadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<String> getSuccess() {
        return successLiveData;
    }

    public void loadLabels(String projectId) {
        loadingLiveData.setValue(true);
        
        repository.getLabelsByProject(projectId, new ILabelRepository.RepositoryCallback<List<Label>>() {
            @Override
            public void onSuccess(List<Label> data) {
                loadingLiveData.setValue(false);
                labelsLiveData.setValue(data);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void createLabel(String projectId, String name, String color) {
        loadingLiveData.setValue(true);
        
        // Create Label object
        Label label = new Label(null, projectId, name, color);
        repository.createLabelInProject(projectId, label, 
            new ILabelRepository.RepositoryCallback<Label>() {
                @Override
                public void onSuccess(Label data) {
                    loadingLiveData.setValue(false);
                    successLiveData.setValue("Label created successfully!");
                    // Reload labels
                    loadLabels(projectId);
                }

                @Override
                public void onError(String error) {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(error);
                }
            }
        );
    }

    public void updateLabel(String projectId, String labelId, String name, String color) {
        loadingLiveData.setValue(true);
        
        // Create Label object
        Label label = new Label(labelId, projectId, name, color);
        repository.updateLabel(labelId, label, 
            new ILabelRepository.RepositoryCallback<Label>() {
                @Override
                public void onSuccess(Label data) {
                    loadingLiveData.setValue(false);
                    successLiveData.setValue("Label updated successfully!");
                    // Reload labels
                    loadLabels(projectId);
                }

                @Override
                public void onError(String error) {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(error);
                }
            }
        );
    }

    public void deleteLabel(String projectId, String labelId) {
        loadingLiveData.setValue(true);
        
        repository.deleteLabel(labelId, 
            new ILabelRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    loadingLiveData.setValue(false);
                    successLiveData.setValue("Label deleted successfully!");
                    // Reload labels
                    loadLabels(projectId);
                }

                @Override
                public void onError(String error) {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(error);
                }
            }
        );
    }

    public void assignLabel(String taskId, String labelId) {
        loadingLiveData.setValue(true);
        
        repository.assignLabelToTask(taskId, labelId, 
            new ILabelRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    loadingLiveData.setValue(false);
                    successLiveData.setValue("Label assigned successfully!");
                }

                @Override
                public void onError(String error) {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(error);
                }
            }
        );
    }

    public void removeLabel(String taskId, String labelId) {
        loadingLiveData.setValue(true);
        
        repository.removeLabelFromTask(taskId, labelId, 
            new ILabelRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    loadingLiveData.setValue(false);
                    successLiveData.setValue("Label removed successfully!");
                }

                @Override
                public void onError(String error) {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(error);
                }
            }
        );
    }
}
