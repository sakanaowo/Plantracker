package com.example.tralalero.domain.usecase.label;

import com.example.tralalero.domain.model.Label;
import com.example.tralalero.domain.repository.ILabelRepository;

import java.util.List;

/**
 * Use case for getting all labels in a project
 */
public class GetLabelsByProjectUseCase {

    private final ILabelRepository repository;

    public GetLabelsByProjectUseCase(ILabelRepository repository) {
        this.repository = repository;
    }

    public void execute(String projectId, Callback<List<Label>> callback) {
        repository.getLabelsByProject(projectId, new ILabelRepository.RepositoryCallback<List<Label>>() {
            @Override
            public void onSuccess(List<Label> result) {
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
