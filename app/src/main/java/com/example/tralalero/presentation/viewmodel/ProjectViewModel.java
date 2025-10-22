package com.example.tralalero.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.usecase.project.GetProjectByIdUseCase;
import com.example.tralalero.domain.usecase.project.CreateProjectUseCase;
import com.example.tralalero.domain.usecase.project.UpdateProjectUseCase;
import com.example.tralalero.domain.usecase.project.DeleteProjectUseCase;
import com.example.tralalero.domain.usecase.project.SwitchBoardTypeUseCase;
import com.example.tralalero.domain.usecase.project.UpdateProjectKeyUseCase;

public class ProjectViewModel extends ViewModel {
    private final GetProjectByIdUseCase getProjectByIdUseCase;
    private final CreateProjectUseCase createProjectUseCase;
    private final UpdateProjectUseCase updateProjectUseCase;
    private final DeleteProjectUseCase deleteProjectUseCase;
    private final SwitchBoardTypeUseCase switchBoardTypeUseCase;
    private final UpdateProjectKeyUseCase updateProjectKeyUseCase;
    private final MutableLiveData<Project> selectedProjectLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> projectDeletedLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> projectCreatedLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> projectUpdatedLiveData = new MutableLiveData<>(false);

    public ProjectViewModel(
            GetProjectByIdUseCase getProjectByIdUseCase,
            CreateProjectUseCase createProjectUseCase,
            UpdateProjectUseCase updateProjectUseCase,
            DeleteProjectUseCase deleteProjectUseCase,
            SwitchBoardTypeUseCase switchBoardTypeUseCase,
            UpdateProjectKeyUseCase updateProjectKeyUseCase
    ) {
        this.getProjectByIdUseCase = getProjectByIdUseCase;
        this.createProjectUseCase = createProjectUseCase;
        this.updateProjectUseCase = updateProjectUseCase;
        this.deleteProjectUseCase = deleteProjectUseCase;
        this.switchBoardTypeUseCase = switchBoardTypeUseCase;
        this.updateProjectKeyUseCase = updateProjectKeyUseCase;
    }

    public LiveData<Project> getSelectedProject() {
        return selectedProjectLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return loadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<Boolean> isProjectDeleted() {
        return projectDeletedLiveData;
    }

    public LiveData<Boolean> isProjectCreated() {
        return projectCreatedLiveData;
    }

    public LiveData<Boolean> isProjectUpdated() {
        return projectUpdatedLiveData;
    }

    public void loadProjectById(String projectId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getProjectByIdUseCase.execute(projectId, new GetProjectByIdUseCase.Callback<Project>() {
            @Override
            public void onSuccess(Project result) {
                loadingLiveData.setValue(false);
                selectedProjectLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void createProject(String workspaceId, Project project) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);
        projectCreatedLiveData.setValue(false);

        createProjectUseCase.execute(workspaceId, project, new CreateProjectUseCase.Callback<Project>() {
            @Override
            public void onSuccess(Project result) {
                loadingLiveData.setValue(false);
                selectedProjectLiveData.setValue(result);
                projectCreatedLiveData.setValue(true);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                projectCreatedLiveData.setValue(false);
            }
        });
    }

    public void updateProject(String projectId, Project project) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);
        projectUpdatedLiveData.setValue(false);

        updateProjectUseCase.execute(projectId, project, new UpdateProjectUseCase.Callback<Project>() {
            @Override
            public void onSuccess(Project result) {
                loadingLiveData.setValue(false);
                selectedProjectLiveData.setValue(result);
                projectUpdatedLiveData.setValue(true);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                projectUpdatedLiveData.setValue(false);
            }
        });
    }

    public void deleteProject(String projectId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);
        projectDeletedLiveData.setValue(false);

        deleteProjectUseCase.execute(projectId, new DeleteProjectUseCase.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadingLiveData.setValue(false);
                selectedProjectLiveData.setValue(null);
                projectDeletedLiveData.setValue(true);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                projectDeletedLiveData.setValue(false);
            }
        });
    }

    public void switchBoardType(String projectId, String newBoardType) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        switchBoardTypeUseCase.execute(projectId, newBoardType, new SwitchBoardTypeUseCase.Callback<Project>() {
            @Override
            public void onSuccess(Project result) {
                loadingLiveData.setValue(false);
                selectedProjectLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void updateProjectKey(String projectId, String newKey) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        updateProjectKeyUseCase.execute(projectId, newKey, new UpdateProjectKeyUseCase.Callback<Project>() {
            @Override
            public void onSuccess(Project result) {
                loadingLiveData.setValue(false);
                selectedProjectLiveData.setValue(result);
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

    public void resetFlags() {
        projectCreatedLiveData.setValue(false);
        projectUpdatedLiveData.setValue(false);
        projectDeletedLiveData.setValue(false);
    }
    @Override
    protected void onCleared() {
        super.onCleared();
    }
}