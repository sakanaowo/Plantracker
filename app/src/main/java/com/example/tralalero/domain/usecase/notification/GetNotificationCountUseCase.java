package com.example.tralalero.domain.usecase.notification;

import com.example.tralalero.domain.repository.INotificationRepository;

/**
 * UseCase: Get unread notification count
 *
 * Matches Backend: GET /api/notifications/unread/count
 *
 * Input: None
 * Output: Integer (count of unread notifications)
 *
 * Business Logic:
 * - Get count of unread notifications
 * - Used for notification badge
 * - Lightweight operation (no full notification data)
 */
public class GetNotificationCountUseCase {

    private final INotificationRepository repository;

    public GetNotificationCountUseCase(INotificationRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute: Get unread notification count
     *
     * @param callback Callback to receive result
     */
    public void execute(Callback<Integer> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        repository.getUnreadCount(new INotificationRepository.RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                // Business logic: Cap at 99+ for UI display
                // But return actual count for now
                callback.onSuccess(result);
            }

            @Override
            public void onError(String error) {
                // On error, return 0 count (fail gracefully)
                callback.onSuccess(0);
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
