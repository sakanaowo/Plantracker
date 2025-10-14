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

/**
 * ViewModel quản lý workspace state và operations.
 * Xử lý operations cho workspaces, projects và boards.
 * 
 * @author Người 1 - Phase 4
 * @date 14/10/2025
 */
public class WorkspaceViewModel extends ViewModel {
    
    // ========== Dependencies ==========
    private final GetWorkspacesUseCase getWorkspacesUseCase;
    private final GetWorkspaceByIdUseCase getWorkspaceByIdUseCase;
    private final CreateWorkspaceUseCase createWorkspaceUseCase;
    private final GetWorkspaceProjectsUseCase getWorkspaceProjectsUseCase;
    private final GetWorkspaceBoardsUseCase getWorkspaceBoardsUseCase;
    
    // ========== LiveData (UI State) ==========
    private final MutableLiveData<List<Workspace>> workspacesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Workspace> selectedWorkspaceLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Project>> projectsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Board>> boardsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> workspaceCreatedLiveData = new MutableLiveData<>(false);
    
    // ========== Constructor ==========
    /**
     * Constructor inject các UseCase dependencies.
     * 
     * @param getWorkspacesUseCase UseCase lấy danh sách workspaces
     * @param getWorkspaceByIdUseCase UseCase lấy workspace theo ID
     * @param createWorkspaceUseCase UseCase tạo workspace mới
     * @param getWorkspaceProjectsUseCase UseCase lấy projects trong workspace
     * @param getWorkspaceBoardsUseCase UseCase lấy boards trong workspace
     */
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
    
    // ========== Getters (Public API) ==========
    
    /**
     * @return LiveData chứa danh sách workspaces
     */
    public LiveData<List<Workspace>> getWorkspaces() {
        return workspacesLiveData;
    }
    
    /**
     * @return LiveData chứa workspace đang được chọn
     */
    public LiveData<Workspace> getSelectedWorkspace() {
        return selectedWorkspaceLiveData;
    }
    
    /**
     * @return LiveData chứa danh sách projects trong workspace
     */
    public LiveData<List<Project>> getProjects() {
        return projectsLiveData;
    }
    
    /**
     * @return LiveData chứa danh sách boards trong project
     */
    public LiveData<List<Board>> getBoards() {
        return boardsLiveData;
    }
    
    /**
     * @return LiveData trạng thái loading
     */
    public LiveData<Boolean> isLoading() {
        return loadingLiveData;
    }
    
    /**
     * @return LiveData chứa error message
     */
    public LiveData<String> getError() {
        return errorLiveData;
    }
    
    /**
     * @return LiveData trạng thái workspace đã được tạo
     */
    public LiveData<Boolean> isWorkspaceCreated() {
        return workspaceCreatedLiveData;
    }
    
    // ========== Public Methods ==========
    
    /**
     * Load tất cả workspaces của user hiện tại.
     */
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
    
    /**
     * Load workspace theo ID.
     * 
     * @param workspaceId ID của workspace cần load
     */
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
    
    /**
     * Tạo workspace mới.
     * 
     * @param workspace Workspace cần tạo
     */
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
                // Reload danh sách workspaces
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
    
    /**
     * Load tất cả projects trong workspace.
     * 
     * @param workspaceId ID của workspace
     */
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
    
    /**
     * Load tất cả boards trong project.
     * 
     * @param projectId ID của project
     */
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
    
    /**
     * Clear error message.
     * Dùng sau khi UI đã hiển thị error.
     */
    public void clearError() {
        errorLiveData.setValue(null);
    }
    
    /**
     * Reset workspace created flag.
     */
    public void resetCreatedFlag() {
        workspaceCreatedLiveData.setValue(false);
    }
    
    // ========== Lifecycle ==========
    @Override
    protected void onCleared() {
        super.onCleared();
        // Cleanup nếu cần (hiện tại không cần)
    }
}