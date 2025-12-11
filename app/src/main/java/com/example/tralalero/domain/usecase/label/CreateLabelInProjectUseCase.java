package com.example.tralalero.domain.usecase.label;

import com.example.tralalero.domain.model.Label;
import com.example.tralalero.domain.repository.ILabelRepository;

/**
 * Use case for creating a label in a project
 */
public class CreateLabelInProjectUseCase {

    private final ILabelRepository repository;

    public CreateLabelInProjectUseCase(ILabelRepository repository) {
        this.repository = repository;
    }

    public void execute(String projectId, Label label, Callback<Label> callback) {
        repository.createLabelInProject(projectId, label, new ILabelRepository.RepositoryCallback<Label>() {
            @Override
            public void onSuccess(Label result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
