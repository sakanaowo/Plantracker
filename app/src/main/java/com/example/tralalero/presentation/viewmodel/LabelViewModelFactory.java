package com.example.tralalero.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.tralalero.domain.usecase.label.GetLabelsByWorkspaceUseCase;
import com.example.tralalero.domain.usecase.label.GetLabelByIdUseCase;
import com.example.tralalero.domain.usecase.label.CreateLabelUseCase;
import com.example.tralalero.domain.usecase.label.UpdateLabelUseCase;
import com.example.tralalero.domain.usecase.label.DeleteLabelUseCase;

public class LabelViewModelFactory implements ViewModelProvider.Factory {

    private final GetLabelsByWorkspaceUseCase getLabelsByWorkspaceUseCase;
    private final GetLabelByIdUseCase getLabelByIdUseCase;
    private final CreateLabelUseCase createLabelUseCase;
    private final UpdateLabelUseCase updateLabelUseCase;
    private final DeleteLabelUseCase deleteLabelUseCase;

    public LabelViewModelFactory(
            GetLabelsByWorkspaceUseCase getLabelsByWorkspaceUseCase,
            GetLabelByIdUseCase getLabelByIdUseCase,
            CreateLabelUseCase createLabelUseCase,
            UpdateLabelUseCase updateLabelUseCase,
            DeleteLabelUseCase deleteLabelUseCase
    ) {
        this.getLabelsByWorkspaceUseCase = getLabelsByWorkspaceUseCase;
        this.getLabelByIdUseCase = getLabelByIdUseCase;
        this.createLabelUseCase = createLabelUseCase;
        this.updateLabelUseCase = updateLabelUseCase;
        this.deleteLabelUseCase = deleteLabelUseCase;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LabelViewModel.class)) {
            return (T) new LabelViewModel(
                    getLabelsByWorkspaceUseCase,
                    getLabelByIdUseCase,
                    createLabelUseCase,
                    updateLabelUseCase,
                    deleteLabelUseCase
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
