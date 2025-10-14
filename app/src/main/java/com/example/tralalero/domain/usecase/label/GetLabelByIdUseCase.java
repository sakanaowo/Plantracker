package com.example.tralalero.domain.usecase.label;

import com.example.tralalero.domain.model.Label;
import com.example.tralalero.domain.repository.ILabelRepository;

/**
 * UseCase: Get label by ID
 *
 * Matches Backend: GET /api/labels/:id
 *
 * Input: String labelId
 * Output: Label
 *
 * Business Logic:
 * - Retrieve single label detail
 * - Validate label ID format
 */
public class GetLabelByIdUseCase {

    private final ILabelRepository repository;

    public GetLabelByIdUseCase(ILabelRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute: Get label by ID
     *
     * @param labelId Label ID to retrieve
     * @param callback Callback to receive result
     */
    public void execute(String labelId, Callback<Label> callback) {
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

        repository.getLabelById(labelId, new ILabelRepository.RepositoryCallback<Label>() {
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

