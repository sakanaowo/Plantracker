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

/**
 * ViewModel for managing Project-related UI state and business logic
 *
 * Features:
 * - CRUD operations for projects
 * - Switch board type (Kanban/Scrum)
 * - Update project key
 *
 * @author Người 2
 * @date 14/10/2025
 */
public class ProjectViewModel extends ViewModel {

    // ========== Dependencies (UseCases) ==========
    private final GetProjectByIdUseCase getProjectByIdUseCase;
    private final CreateProjectUseCase createProjectUseCase;
    private final UpdateProjectUseCase updateProjectUseCase;
    private final DeleteProjectUseCase deleteProjectUseCase;
    private final SwitchBoardTypeUseCase switchBoardTypeUseCase;
    private final UpdateProjectKeyUseCase updateProjectKeyUseCase;

    // ========== LiveData (UI State) ==========
    private final MutableLiveData<Project> selectedProjectLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> projectDeletedLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> projectCreatedLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> projectUpdatedLiveData = new MutableLiveData<>(false);

    // ========== Constructor ==========
    /**
     * Constructor with dependency injection
     *
     * @param getProjectByIdUseCase UseCase to get project by ID
     * @param createProjectUseCase UseCase to create new project
     * @param updateProjectUseCase UseCase to update project
     * @param deleteProjectUseCase UseCase to delete project
     * @param switchBoardTypeUseCase UseCase to switch board type
     * @param updateProjectKeyUseCase UseCase to update project key
     */
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

    // ========== Getters (Public API) ==========

    /**
     * Get the currently selected project
     * @return LiveData containing the selected project
     */
    public LiveData<Project> getSelectedProject() {
        return selectedProjectLiveData;
    }

    /**
     * Get loading state
     * @return LiveData containing loading state
     */
    public LiveData<Boolean> isLoading() {
        return loadingLiveData;
    }

    /**
     * Get error message
     * @return LiveData containing error message
     */
    public LiveData<String> getError() {
        return errorLiveData;
    }

    /**
     * Get project deleted state
     * @return LiveData indicating if project was deleted
     */
    public LiveData<Boolean> isProjectDeleted() {
        return projectDeletedLiveData;
    }

    /**
     * Get project created state
     * @return LiveData indicating if project was created
     */
    public LiveData<Boolean> isProjectCreated() {
        return projectCreatedLiveData;
    }

    /**
     * Get project updated state
     * @return LiveData indicating if project was updated
     */
    public LiveData<Boolean> isProjectUpdated() {
        return projectUpdatedLiveData;
    }

    // ========== Public Methods ==========

    /**
     * Load a project by its ID
     * @param projectId The ID of the project to load
     */
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

    /**
     * Create a new project
     * @param workspaceId The workspace ID to create project in
     * @param project The project object to create
     */
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

    /**
     * Update an existing project
     * @param projectId The ID of the project to update
     * @param project The updated project object
     */
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

    /**
     * Delete a project
     * @param projectId The ID of the project to delete
     */
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

    /**
     * Switch the board type of a project (Kanban/Scrum)
     * @param projectId The ID of the project
     * @param newBoardType The new board type ("KANBAN" or "SCRUM")
     */
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

    /**
     * Update the project key (e.g., PROJ-123)
     * @param projectId The ID of the project
     * @param newKey The new project key
     */
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

    /**
     * Clear any error message
     */
    public void clearError() {
        errorLiveData.setValue(null);
    }

    /**
     * Reset all success flags
     */
    public void resetFlags() {
        projectCreatedLiveData.setValue(false);
        projectUpdatedLiveData.setValue(false);
        projectDeletedLiveData.setValue(false);
    }

    // ========== Lifecycle ==========
    @Override
    protected void onCleared() {
        super.onCleared();
        // Cleanup if needed (e.g., cancel ongoing operations)
    }
}