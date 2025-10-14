package com.example.tralalero.domain.usecase.notification;

import com.example.tralalero.domain.model.Notification;
import com.example.tralalero.domain.repository.INotificationRepository;

/**
 * UseCase: Get notification by ID
 *
 * Matches Backend: GET /api/notifications/:id
 *
 * Input: String notificationId
 * Output: Notification
 *
 * Business Logic:
 * - Retrieve single notification detail
 * - Validate notification ID format
 */
public class GetNotificationByIdUseCase {

    private final INotificationRepository repository;

    public GetNotificationByIdUseCase(INotificationRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute: Get notification by ID
     *
     * @param notificationId Notification ID to retrieve
     * @param callback Callback to receive result
     */
    public void execute(String notificationId, Callback<Notification> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        // Validate notification ID
        if (notificationId == null || notificationId.trim().isEmpty()) {
            callback.onError("Notification ID cannot be empty");
            return;
        }

        // Validate UUID format
        if (!isValidUUID(notificationId)) {
            callback.onError("Invalid notification ID format");
            return;
        }

        repository.getNotificationById(notificationId, new INotificationRepository.RepositoryCallback<Notification>() {
            @Override
            public void onSuccess(Notification result) {
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

