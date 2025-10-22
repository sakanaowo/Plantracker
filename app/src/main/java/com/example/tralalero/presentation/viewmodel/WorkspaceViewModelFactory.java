package com.example.tralalero.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.tralalero.domain.usecase.workspace.CreateWorkspaceUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspaceBoardsUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspaceByIdUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspaceProjectsUseCase;
import com.example.tralalero.domain.usecase.workspace.GetWorkspacesUseCase;

public class WorkspaceViewModelFactory implements ViewModelProvider.Factory {

    private final GetWorkspacesUseCase getWorkspacesUseCase;
    private final GetWorkspaceByIdUseCase getWorkspaceByIdUseCase;
    private final CreateWorkspaceUseCase createWorkspaceUseCase;
    private final GetWorkspaceProjectsUseCase getWorkspaceProjectsUseCase;
    private final GetWorkspaceBoardsUseCase getWorkspaceBoardsUseCase;

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