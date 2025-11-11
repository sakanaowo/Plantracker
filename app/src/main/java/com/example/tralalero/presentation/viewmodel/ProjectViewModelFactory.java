package com.example.tralalero.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.tralalero.domain.usecase.board.GetBoardsByProjectUseCase;
import com.example.tralalero.domain.usecase.project.GetProjectByIdUseCase;
import com.example.tralalero.domain.usecase.project.CreateProjectUseCase;
import com.example.tralalero.domain.usecase.project.UpdateProjectUseCase;
import com.example.tralalero.domain.usecase.project.DeleteProjectUseCase;
import com.example.tralalero.domain.usecase.project.SwitchBoardTypeUseCase;
import com.example.tralalero.domain.usecase.project.UpdateProjectKeyUseCase;
import com.example.tralalero.domain.usecase.task.GetTasksByBoardUseCase;

public class ProjectViewModelFactory implements ViewModelProvider.Factory {

    private final GetProjectByIdUseCase getProjectByIdUseCase;
    private final CreateProjectUseCase createProjectUseCase;
    private final UpdateProjectUseCase updateProjectUseCase;
    private final DeleteProjectUseCase deleteProjectUseCase;
    private final SwitchBoardTypeUseCase switchBoardTypeUseCase;
    private final UpdateProjectKeyUseCase updateProjectKeyUseCase;
    private final GetBoardsByProjectUseCase getBoardsByProjectUseCase;
    private final GetTasksByBoardUseCase getTasksByBoardUseCase;

    public ProjectViewModelFactory(
            GetProjectByIdUseCase getProjectByIdUseCase,
            CreateProjectUseCase createProjectUseCase,
            UpdateProjectUseCase updateProjectUseCase,
            DeleteProjectUseCase deleteProjectUseCase,
            SwitchBoardTypeUseCase switchBoardTypeUseCase,
            UpdateProjectKeyUseCase updateProjectKeyUseCase,
            GetBoardsByProjectUseCase getBoardsByProjectUseCase,
            GetTasksByBoardUseCase getTasksByBoardUseCase
    ) {
        this.getProjectByIdUseCase = getProjectByIdUseCase;
        this.createProjectUseCase = createProjectUseCase;
        this.updateProjectUseCase = updateProjectUseCase;
        this.deleteProjectUseCase = deleteProjectUseCase;
        this.switchBoardTypeUseCase = switchBoardTypeUseCase;
        this.updateProjectKeyUseCase = updateProjectKeyUseCase;
        this.getBoardsByProjectUseCase = getBoardsByProjectUseCase;
        this.getTasksByBoardUseCase = getTasksByBoardUseCase;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ProjectViewModel.class)) {
            return (T) new ProjectViewModel(
                    getProjectByIdUseCase,
                    createProjectUseCase,
                    updateProjectUseCase,
                    deleteProjectUseCase,
                    switchBoardTypeUseCase,
                    updateProjectKeyUseCase,
                    getBoardsByProjectUseCase,
                    getTasksByBoardUseCase
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}