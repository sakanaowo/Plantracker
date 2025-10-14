package com.example.tralalero.domain.usecase.label;

import com.example.tralalero.domain.model.Label;
import com.example.tralalero.domain.repository.ILabelRepository;

/**
 * UseCase: Create new label in workspace
 *
 * Matches Backend: POST /api/workspaces/:workspaceId/labels
 * Backend DTO: CreateLabelDto {
 *   name: string
 *   color: string (hex color code)
 * }
 *
 * Input: String workspaceId, Label label
 * Output: Label (created with generated ID)
 *
 * Business Logic:
 * - Validate label name not empty
 * - Validate color format (hex code)
 * - Workspace-level labels (shared across projects)
 */
public class CreateLabelUseCase {

    private final ILabelRepository repository;

    public CreateLabelUseCase(ILabelRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute: Create new label
     *
     * @param workspaceId Workspace ID to create label in
     * @param label Label object (must have name and color)
     * @param callback Callback to receive result
     */
    public void execute(String workspaceId, Label label, Callback<Label> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        // Validate workspace ID
        if (workspaceId == null || workspaceId.trim().isEmpty()) {
            callback.onError("Workspace ID cannot be empty");
            return;
        }

        if (!isValidUUID(workspaceId)) {
            callback.onError("Invalid workspace ID format");
            return;
        }

        // Validate label object
        if (label == null) {
            callback.onError("Label cannot be null");
            return;
        }

        // Validate label name
        if (label.getName() == null || label.getName().trim().isEmpty()) {
            callback.onError("Label name cannot be empty");
            return;
        }

        if (label.getName().length() > 50) {
            callback.onError("Label name cannot exceed 50 characters");
            return;
        }

        // Validate color
        if (label.getColor() == null || label.getColor().trim().isEmpty()) {
            callback.onError("Label color cannot be empty");
            return;
        }

        if (!isValidHexColor(label.getColor())) {
            callback.onError("Invalid color format. Use hex color code (e.g., #FF5733)");
            return;
        }

        repository.createLabel(workspaceId, label, new ILabelRepository.RepositoryCallback<Label>() {
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

