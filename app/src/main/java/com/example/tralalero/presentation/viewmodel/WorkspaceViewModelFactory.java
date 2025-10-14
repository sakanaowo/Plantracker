package com.example.tralalero.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.tralalero.domain.usecase.workspace.CreateWorkspaceUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspaceBoardsUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspaceByIdUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspaceProjectsUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspacesUseCase;

/**
 * Factory để tạo WorkspaceViewModel với dependencies injection.
 * 
 * @author Người 1 - Phase 4
 * @date 14/10/2025
 */
public class WorkspaceViewModelFactory implements ViewModelProvider.Factory {
    
    private final GetWorkspacesUseCase getWorkspacesUseCase;
    private final GetWorkspaceByIdUseCase getWorkspaceByIdUseCase;
    private final CreateWorkspaceUseCase createWorkspaceUseCase;
    private final GetWorkspaceProjectsUseCase getWorkspaceProjectsUseCase;
    private final GetWorkspaceBoardsUseCase getWorkspaceBoardsUseCase;
    
    /**
     * Constructor inject các UseCase dependencies.
     * 
     * @param getWorkspacesUseCase UseCase lấy danh sách workspaces
     * @param getWorkspaceByIdUseCase UseCase lấy workspace theo ID
     * @param createWorkspaceUseCase UseCase tạo workspace mới
     * @param getWorkspaceProjectsUseCase UseCase lấy projects trong workspace
     * @param getWorkspaceBoardsUseCase UseCase lấy boards trong project
     */
    public WorkspaceViewModelFactory(
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
    
    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(WorkspaceViewModel.class)) {
            return (T) new WorkspaceViewModel(
                    getWorkspacesUseCase,
                    getWorkspaceByIdUseCase,
                    createWorkspaceUseCase,
                    getWorkspaceProjectsUseCase,
                    getWorkspaceBoardsUseCase
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}