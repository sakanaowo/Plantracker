package com.example.tralalero.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.tralalero.domain.usecase.label.AssignLabelToTaskUseCase;
import com.example.tralalero.domain.usecase.label.CreateLabelInProjectUseCase;
import com.example.tralalero.domain.usecase.label.GetLabelsByProjectUseCase;
import com.example.tralalero.domain.usecase.label.GetTaskLabelsUseCase;
import com.example.tralalero.domain.usecase.label.RemoveLabelFromTaskUseCase;
import com.example.tralalero.domain.usecase.label.GetLabelsByWorkspaceUseCase;
import com.example.tralalero.domain.usecase.label.GetLabelByIdUseCase;
import com.example.tralalero.domain.usecase.label.CreateLabelUseCase;
import com.example.tralalero.domain.usecase.label.UpdateLabelUseCase;
import com.example.tralalero.domain.usecase.label.DeleteLabelUseCase;

public class LabelViewModelFactory implements ViewModelProvider.Factory {

    private final GetLabelsByWorkspaceUseCase getLabelsByWorkspaceUseCase;
    private final GetLabelsByProjectUseCase getLabelsByProjectUseCase;
    private final GetLabelByIdUseCase getLabelByIdUseCase;
    private final CreateLabelUseCase createLabelUseCase;
    private final CreateLabelInProjectUseCase createLabelInProjectUseCase;
    private final UpdateLabelUseCase updateLabelUseCase;
    private final DeleteLabelUseCase deleteLabelUseCase;
    private final GetTaskLabelsUseCase getTaskLabelsUseCase;
    private final AssignLabelToTaskUseCase assignLabelToTaskUseCase;
    private final RemoveLabelFromTaskUseCase removeLabelFromTaskUseCase;

    public LabelViewModelFactory(
            GetLabelsByWorkspaceUseCase getLabelsByWorkspaceUseCase,
            GetLabelsByProjectUseCase getLabelsByProjectUseCase,
            GetLabelByIdUseCase getLabelByIdUseCase,
            CreateLabelUseCase createLabelUseCase,
            CreateLabelInProjectUseCase createLabelInProjectUseCase,
            UpdateLabelUseCase updateLabelUseCase,
            DeleteLabelUseCase deleteLabelUseCase,
            GetTaskLabelsUseCase getTaskLabelsUseCase,
            AssignLabelToTaskUseCase assignLabelToTaskUseCase,
            RemoveLabelFromTaskUseCase removeLabelFromTaskUseCase
    ) {
        this.getLabelsByWorkspaceUseCase = getLabelsByWorkspaceUseCase;
        this.getLabelsByProjectUseCase = getLabelsByProjectUseCase;
        this.getLabelByIdUseCase = getLabelByIdUseCase;
        this.createLabelUseCase = createLabelUseCase;
        this.createLabelInProjectUseCase = createLabelInProjectUseCase;
        this.updateLabelUseCase = updateLabelUseCase;
        this.deleteLabelUseCase = deleteLabelUseCase;
        this.getTaskLabelsUseCase = getTaskLabelsUseCase;
        this.assignLabelToTaskUseCase = assignLabelToTaskUseCase;
        this.removeLabelFromTaskUseCase = removeLabelFromTaskUseCase;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LabelViewModel.class)) {
            return (T) new LabelViewModel(
                    getLabelsByWorkspaceUseCase,
                    getLabelsByProjectUseCase,
                    getLabelByIdUseCase,
                    createLabelUseCase,
                    createLabelInProjectUseCase,
                    updateLabelUseCase,
                    deleteLabelUseCase,
                    getTaskLabelsUseCase,
                    assignLabelToTaskUseCase,
                    removeLabelFromTaskUseCase
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
