package com.example.tralalero.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tralalero.domain.model.Board;
import com.example.tralalero.domain.model.Project;
import com.example.tralalero.domain.model.Workspace;
import com.example.tralalero.domain.usecase.workspace.CreateWorkspaceUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspaceBoardsUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspaceByIdUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspaceProjectsUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspacesUseCase;

import java.util.List;

public class WorkspaceViewModel extends ViewModel {
    private final GetWorkspacesUseCase getWorkspacesUseCase;
    private final GetWorkspaceByIdUseCase getWorkspaceByIdUseCase;
    private final CreateWorkspaceUseCase createWorkspaceUseCase;
    private final GetWorkspaceProjectsUseCase getWorkspaceProjectsUseCase;
    private final GetWorkspaceBoardsUseCase getWorkspaceBoardsUseCase;
    private final MutableLiveData<List<Workspace>> workspacesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Workspace> selectedWorkspaceLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Project>> projectsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Board>> boardsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> workspaceCreatedLiveData = new MutableLiveData<>(false);

    public WorkspaceViewModel(
            GetWorkspacesUseCase getWorkspacesUseCase,
            GetWorkspaceByIdUseCase getWorkspaceByIdUseCase,
            CreateWorkspaceUseCase createWorkspaceUseCase,
            GetWorkspaceProjectsUseCase getWorkspaceProjectsUseCase,
            GetWorkspaceBoardsUseCase getWorkspaceBoardsUseCase
    ) {
        this.getWorkspacesUseCase = getWorkspacesUseCase;
        this.getWorkspaceByIdUseCase = getWorkspaceByIdUseCase;
        this.createWorkspaceUseCase = createWorkspaceUseCase;
        this.getWorkspaceProjectsUseCase = getWorkspaceProjectsUseCase;
        this.getWorkspaceBoardsUseCase = getWorkspaceBoardsUseCase;
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

    public LiveData<Boolean> isWorkspaceCreated() {
        return workspaceCreatedLiveData;
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
        workspaceCreatedLiveData.setValue(false);

        createWorkspaceUseCase.execute(workspace, new CreateWorkspaceUseCase.Callback<Workspace>() {
            @Override
            public void onSuccess(Workspace result) {
                loadingLiveData.setValue(false);
                selectedWorkspaceLiveData.setValue(result);
                workspaceCreatedLiveData.setValue(true);
                loadWorkspaces();
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                workspaceCreatedLiveData.setValue(false);
            }
        });
    }

    public void loadWorkspaceProjects(String workspaceId) {
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

    public void loadProjectBoards(String projectId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getWorkspaceBoardsUseCase.execute(projectId, new GetWorkspaceBoardsUseCase.Callback<List<Board>>() {
            @Override
            public void onSuccess(List<Board> result) {
                loadingLiveData.setValue(false);
                boardsLiveData.setValue(result);
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

    public void resetCreatedFlag() {
        workspaceCreatedLiveData.setValue(false);
    }
    @Override
    protected void onCleared() {
        super.onCleared();
    }
}