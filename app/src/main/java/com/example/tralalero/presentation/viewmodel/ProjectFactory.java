package com.example.tralalero.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.tralalero.domain.usecase.project.GetProjectByIdUseCase;
import com.example.tralalero.domain.usecase.project.CreateProjectUseCase;
import com.example.tralalero.domain.usecase.project.UpdateProjectUseCase;
import com.example.tralalero.domain.usecase.project.DeleteProjectUseCase;
import com.example.tralalero.domain.usecase.project.SwitchBoardTypeUseCase;
import com.example.tralalero.domain.usecase.project.UpdateProjectKeyUseCase;

/**
 * Factory for creating ProjectViewModel instances
 * Required by Android Architecture Components to inject dependencies
 *
 * @author Người 2
 * @date 14/10/2025
 */
public class ProjectFactory implements ViewModelProvider.Factory {

    private final GetProjectByIdUseCase getProjectByIdUseCase;
    private final CreateProjectUseCase createProjectUseCase;
    private final UpdateProjectUseCase updateProjectUseCase;
    private final DeleteProjectUseCase deleteProjectUseCase;
    private final SwitchBoardTypeUseCase switchBoardTypeUseCase;
    private final UpdateProjectKeyUseCase updateProjectKeyUseCase;

    /**
     * Constructor with all required UseCases
     */
    public ProjectFactory(
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
                    updateProjectKeyUseCase
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}