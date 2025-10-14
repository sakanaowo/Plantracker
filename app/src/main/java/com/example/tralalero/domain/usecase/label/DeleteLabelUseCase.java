package com.example.tralalero.domain.usecase.label;

import com.example.tralalero.domain.repository.ILabelRepository;

/**
 * UseCase: Delete a label
 *
 * Matches Backend: DELETE /api/labels/:id
 *
 * Input: String labelId
 * Output: Void (success/failure)
 *
 * Business Logic:
 * - Delete label from workspace
 * - Remove label from all tasks that use it
 * - Validate label ID
 */
public class DeleteLabelUseCase {

    private final ILabelRepository repository;

    public DeleteLabelUseCase(ILabelRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute: Delete label
     *
     * @param labelId Label ID to delete
     * @param callback Callback to receive result
     */
    public void execute(String labelId, Callback<Void> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        // Validate label ID
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

    /**
     * Validate UUID format
     */
    private boolean isValidUUID(String uuid) {
        if (uuid == null) return false;
        String uuidPattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        return uuid.matches(uuidPattern);
    }

    /**
     * Callback interface for use case result
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

