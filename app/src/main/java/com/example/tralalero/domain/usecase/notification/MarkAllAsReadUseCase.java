package com.example.tralalero.domain.usecase.notification;

import com.example.tralalero.domain.repository.INotificationRepository;

/**
 * UseCase: Mark all notifications as read
 *
 * Matches Backend: PUT /api/notifications/read-all
 *
 * Input: None
 * Output: Void (success/failure)
 *
 * Business Logic:
 * - Mark all unread notifications as read
 * - Batch operation for better UX
 * - Used in "Mark all as read" button
 */
public class MarkAllAsReadUseCase {

    private final INotificationRepository repository;

    public MarkAllAsReadUseCase(INotificationRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute: Mark all notifications as read
     *
     * @param callback Callback to receive result
     */
    public void execute(Callback<Void> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        repository.markAllAsRead(new INotificationRepository.RepositoryCallback<Void>() {
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

