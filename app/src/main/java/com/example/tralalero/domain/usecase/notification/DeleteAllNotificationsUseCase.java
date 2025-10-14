package com.example.tralalero.domain.usecase.notification;

import com.example.tralalero.domain.repository.INotificationRepository;

/**
 * UseCase: Delete all notifications
 *
 * Matches Backend: DELETE /api/notifications
 *
 * Input: None
 * Output: Void (success/failure)
 *
 * Business Logic:
 * - Delete all notifications for current user
 * - Batch operation for clearing notification history
 * - Used in "Clear all" button
 */
public class DeleteAllNotificationsUseCase {

    private final INotificationRepository repository;

    public DeleteAllNotificationsUseCase(INotificationRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute: Delete all notifications
     *
     * @param callback Callback to receive result
     */
    public void execute(Callback<Void> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        repository.deleteAllNotifications(new INotificationRepository.RepositoryCallback<Void>() {
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
     * Callback interface for use case result
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

