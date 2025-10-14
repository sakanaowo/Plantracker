package com.example.tralalero.domain.usecase.notification;

import com.example.tralalero.domain.repository.INotificationRepository;

/**
 * UseCase: Mark a notification as read
 *
 * Matches Backend: PUT /api/notifications/:id/read
 *
 * Input: String notificationId
 * Output: Void (success/failure)
 *
 * Business Logic:
 * - Mark single notification as read
 * - Update read_at timestamp
 * - Used when user clicks on notification
 */
public class MarkAsReadUseCase {

    private final INotificationRepository repository;

    public MarkAsReadUseCase(INotificationRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute: Mark notification as read
     *
     * @param notificationId Notification ID to mark as read
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

        repository.markAsRead(notificationId, new INotificationRepository.RepositoryCallback<Void>() {
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

