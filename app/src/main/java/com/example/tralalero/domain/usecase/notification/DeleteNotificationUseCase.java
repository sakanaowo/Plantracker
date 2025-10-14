package com.example.tralalero.domain.usecase.notification;

import com.example.tralalero.domain.repository.INotificationRepository;

/**
 * UseCase: Delete a notification
 *
 * Matches Backend: DELETE /api/notifications/:id
 *
 * Input: String notificationId
 * Output: Void (success/failure)
 *
 * Business Logic:
 * - Delete single notification
 * - Used when user swipes to dismiss notification
 */
public class DeleteNotificationUseCase {

    private final INotificationRepository repository;

    public DeleteNotificationUseCase(INotificationRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute: Delete notification
     *
     * @param notificationId Notification ID to delete
     * @param callback Callback to receive result
     */
    public void execute(String notificationId, Callback<Void> callback) {
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

        repository.deleteNotification(notificationId, new INotificationRepository.RepositoryCallback<Void>() {
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

