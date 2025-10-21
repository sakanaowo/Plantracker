package com.example.tralalero.domain.usecase.label;

import com.example.tralalero.domain.model.Label;
import com.example.tralalero.domain.repository.ILabelRepository;


public class CreateLabelUseCase {

    private final ILabelRepository repository;

    public CreateLabelUseCase(ILabelRepository repository) {
        this.repository = repository;
    }


    public void execute(String workspaceId, Label label, Callback<Label> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        if (workspaceId == null || workspaceId.trim().isEmpty()) {
            callback.onError("Workspace ID cannot be empty");
            return;
        }

        if (!isValidUUID(workspaceId)) {
            callback.onError("Invalid workspace ID format");
            return;
        }

        if (label == null) {
            callback.onError("Label cannot be null");
            return;
        }

        if (label.getName() == null || label.getName().trim().isEmpty()) {
            callback.onError("Label name cannot be empty");
            return;
        }

        if (label.getName().length() > 50) {
            callback.onError("Label name cannot exceed 50 characters");
            return;
        }

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


    private boolean isValidUUID(String uuid) {
        if (uuid == null) return false;
        String uuidPattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        return uuid.matches(uuidPattern);
    }


    private boolean isValidHexColor(String color) {
        if (color == null) return false;
        String hexPattern = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
        return color.matches(hexPattern);
    }


    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

