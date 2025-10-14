package com.example.tralalero.domain.usecase.label;

import com.example.tralalero.domain.model.Label;
import com.example.tralalero.domain.repository.ILabelRepository;

/**
 * UseCase: Update label information
 *
 * Matches Backend: PUT /api/labels/:id
 * Backend DTO: UpdateLabelDto {
 *   name?: string
 *   color?: string
 * }
 *
 * Input: String labelId, Label label
 * Output: Label (updated)
 *
 * Business Logic:
 * - Validate name and color if provided
 * - Only update fields that are provided
 */
public class UpdateLabelUseCase {

    private final ILabelRepository repository;

    public UpdateLabelUseCase(ILabelRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute: Update label
     *
     * @param labelId Label ID to update
     * @param label Label object with updated fields
     * @param callback Callback to receive result
     */
    public void execute(String labelId, Label label, Callback<Label> callback) {
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

        // Validate label object
        if (label == null) {
            callback.onError("Label cannot be null");
            return;
        }

        // Validate label name if provided
        if (label.getName() != null) {
            if (label.getName().trim().isEmpty()) {
                callback.onError("Label name cannot be empty");
                return;
            }

            if (label.getName().length() > 50) {
                callback.onError("Label name cannot exceed 50 characters");
                return;
            }
        }

        // Validate color if provided
        if (label.getColor() != null) {
            if (label.getColor().trim().isEmpty()) {
                callback.onError("Label color cannot be empty");
                return;
            }

            if (!isValidHexColor(label.getColor())) {
                callback.onError("Invalid color format. Use hex color code (e.g., #FF5733)");
                return;
            }
        }

        repository.updateLabel(labelId, label, new ILabelRepository.RepositoryCallback<Label>() {
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
     * Validate hex color format (#RRGGBB or #RGB)
     */
    private boolean isValidHexColor(String color) {
        if (color == null) return false;
        String hexPattern = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
        return color.matches(hexPattern);
    }

    /**
     * Callback interface for use case result
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

