package com.example.tralalero.domain.usecase.label;

import com.example.tralalero.domain.repository.ILabelRepository;

/**
 * Use case for removing a label from a task
 */
public class RemoveLabelFromTaskUseCase {

    private final ILabelRepository repository;

    public RemoveLabelFromTaskUseCase(ILabelRepository repository) {
        this.repository = repository;
    }

    public void execute(String taskId, String labelId, Callback<Void> callback) {
        repository.removeLabelFromTask(taskId, labelId, new ILabelRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(null);
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
