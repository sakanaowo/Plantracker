package com.example.tralalero.domain.usecase.label;

import com.example.tralalero.domain.repository.ILabelRepository;


public class DeleteLabelUseCase {

    private final ILabelRepository repository;

    public DeleteLabelUseCase(ILabelRepository repository) {
        this.repository = repository;
    }


    public void execute(String labelId, Callback<Void> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        if (labelId == null || labelId.trim().isEmpty()) {
            callback.onError("Label ID cannot be empty");
            return;
        }

        if (!isValidUUID(labelId)) {
            callback.onError("Invalid label ID format");
            return;
        }

        repository.deleteLabel(labelId, new ILabelRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }


    private boolean isValidUUID(String uuid) {
        if (uuid == null) return false;
        String uuidPattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        return uuid.matches(uuidPattern);
    }


    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

