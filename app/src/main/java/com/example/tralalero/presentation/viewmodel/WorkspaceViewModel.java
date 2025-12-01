package com.example.tralalero.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tralalero.domain.model.Board;
import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.model.Workspace;
import com.example.tralalero.domain.usecase.project.CreateProjectUseCase;
import com.example.tralalero.domain.usecase.project.DeleteProjectUseCase;
import com.example.tralalero.domain.usecase.workspace.CreateWorkspaceUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspaceBoardsUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspaceByIdUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspaceProjectsUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspacesUseCase;

import java.util.ArrayList;
import java.util.List;

public class WorkspaceViewModel extends ViewModel {
    private final GetWorkspacesUseCase getWorkspacesUseCase;
    private final GetWorkspaceByIdUseCase getWorkspaceByIdUseCase;
    private final CreateWorkspaceUseCase createWorkspaceUseCase;
    private final GetWorkspaceProjectsUseCase getWorkspaceProjectsUseCase;
    private final GetWorkspaceBoardsUseCase getWorkspaceBoardsUseCase;
    private final CreateProjectUseCase createProjectUseCase;
    private final DeleteProjectUseCase deleteProjectUseCase;
    
    private final MutableLiveData<List<Workspace>> workspacesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Workspace> selectedWorkspaceLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Project>> projectsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Board>> boardsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> selectedWorkspaceIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public WorkspaceViewModel(
            GetWorkspacesUseCase getWorkspacesUseCase,
            GetWorkspaceByIdUseCase getWorkspaceByIdUseCase,
            CreateWorkspaceUseCase createWorkspaceUseCase,
            GetWorkspaceProjectsUseCase getWorkspaceProjectsUseCase,
            GetWorkspaceBoardsUseCase getWorkspaceBoardsUseCase,
            CreateProjectUseCase createProjectUseCase,
            DeleteProjectUseCase deleteProjectUseCase
    ) {
        this.getWorkspacesUseCase = getWorkspacesUseCase;
        this.getWorkspaceByIdUseCase = getWorkspaceByIdUseCase;
        this.createWorkspaceUseCase = createWorkspaceUseCase;
        this.getWorkspaceProjectsUseCase = getWorkspaceProjectsUseCase;
        this.getWorkspaceBoardsUseCase = getWorkspaceBoardsUseCase;
        this.createProjectUseCase = createProjectUseCase;
        this.deleteProjectUseCase = deleteProjectUseCase;
    }

    public LiveData<List<Workspace>> getWorkspaces() {
        return workspacesLiveData;
    }

    public LiveData<Workspace> getSelectedWorkspace() {
        return selectedWorkspaceLiveData;
    }

    public LiveData<List<Project>> getProjects() {
        return projectsLiveData;
    }

    public LiveData<List<Board>> getBoards() {
        return boardsLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return loadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    // ✅ Auto-load when workspace selected
    public void selectWorkspace(String workspaceId) {
        if (workspaceId == null) return;
        
        selectedWorkspaceIdLiveData.setValue(workspaceId);
        
        // Load workspace details
        loadWorkspaceById(workspaceId);
        
        // Auto-load projects for this workspace
        loadProjectsForWorkspace(workspaceId);
    }
    
    private void loadProjectsForWorkspace(String workspaceId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getWorkspaceProjectsUseCase.execute(workspaceId, new GetWorkspaceProjectsUseCase.Callback<List<Project>>() {
            @Override
            public void onSuccess(List<Project> result) {
                loadingLiveData.setValue(false);
                projectsLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void loadWorkspaces() {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getWorkspacesUseCase.execute(new GetWorkspacesUseCase.Callback<List<Workspace>>() {
            @Override
            public void onSuccess(List<Workspace> result) {
                loadingLiveData.setValue(false);
                workspacesLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void loadWorkspaceById(String workspaceId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getWorkspaceByIdUseCase.execute(workspaceId, new GetWorkspaceByIdUseCase.Callback<Workspace>() {
            @Override
            public void onSuccess(Workspace result) {
                loadingLiveData.setValue(false);
                selectedWorkspaceLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void createWorkspace(Workspace workspace) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        createWorkspaceUseCase.execute(workspace, new CreateWorkspaceUseCase.Callback<Workspace>() {
            @Override
            public void onSuccess(Workspace result) {
                loadingLiveData.setValue(false);
                selectedWorkspaceLiveData.setValue(result);
                // Reload workspaces to include the new one
                loadWorkspaces();
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    // ✅ Create project with optimistic update
    public void createProject(String workspaceId, String name, String description) {
        // Optimistic update - add temp project immediately
        List<Project> currentProjects = projectsLiveData.getValue();
        final List<Project> originalList = currentProjects != null ? new ArrayList<>(currentProjects) : new ArrayList<>();
        
        Project tempProject = new Project(
            "temp_" + System.currentTimeMillis(),  // Temp ID
            workspaceId,
            name,
            description,
            "",  // Empty key - will be set by server
            "KANBAN",  // Default board type
            null  // Type will be set by server (default PERSONAL)
        );
        
        List<Project> updated = new ArrayList<>(originalList);
        updated.add(0, tempProject);  // Add to beginning
        projectsLiveData.setValue(updated);
        
        // Background API call - pass the Project object
        createProjectUseCase.execute(workspaceId, tempProject, new CreateProjectUseCase.Callback<Project>() {
            @Override
            public void onSuccess(Project result) {
                // Reload to get real data from server
                loadProjectsForWorkspace(workspaceId);
            }

            @Override
            public void onError(String error) {
                // Rollback optimistic update on error
                projectsLiveData.setValue(originalList);
                errorLiveData.setValue(error);
            }
        });
    }
    
    // ✅ Delete project with optimistic update (RACE CONDITION FIXED)
    public void deleteProject(String projectId) {
        List<Project> current = projectsLiveData.getValue();
        if (current == null) return;
        
        // Create a final copy for rollback
        final List<Project> originalList = new ArrayList<>(current);
        
        // Find the project to delete
        Project toDelete = null;
        for (Project p : current) {
            if (p.getId() != null && p.getId().equals(projectId)) {
                toDelete = p;
                break;
            }
        }
        
        if (toDelete == null) {
            errorLiveData.setValue("Project not found");
            return;
        }
        
        // Optimistic update - remove immediately
        List<Project> updated = new ArrayList<>(current);
        updated.removeIf(p -> p.getId() != null && p.getId().equals(projectId));
        projectsLiveData.setValue(updated);
        
        // Background API call
        final Project deletedProject = toDelete;
        deleteProjectUseCase.execute(projectId, new DeleteProjectUseCase.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Already updated UI, nothing more to do
            }

            @Override
            public void onError(String error) {
                // ✅ FIX: Smart rollback - restore deleted project to current list
                List<Project> currentList = projectsLiveData.getValue();
                if (currentList != null) {
                    List<Project> restoredList = new ArrayList<>(currentList);
                    // Add back the deleted project at original position
                    int originalIndex = originalList.indexOf(deletedProject);
                    if (originalIndex >= 0 && originalIndex <= restoredList.size()) {
                        restoredList.add(originalIndex, deletedProject);
                    } else {
                        restoredList.add(0, deletedProject); // Add at beginning if position unknown
                    }
                    projectsLiveData.setValue(restoredList);
                } else {
                    // Fallback to complete restore
                    projectsLiveData.setValue(originalList);
                }
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