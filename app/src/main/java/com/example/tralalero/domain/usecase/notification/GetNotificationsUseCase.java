package com.example.tralalero.domain.usecase.notification;

import com.example.tralalero.domain.model.Notification;
import com.example.tralalero.domain.repository.INotificationRepository;

import java.util.List;

/**
 * UseCase: Get all notifications for current user
 *
 * Matches Backend: GET /api/notifications
 *
 * Input: None (uses current user session)
 * Output: List<Notification>
 *
 * Business Logic:
 * - Retrieve all notifications (read + unread)
 * - Sorted by created date DESC (newest first)
 */
public class GetNotificationsUseCase {

    private final INotificationRepository repository;

    public GetNotificationsUseCase(INotificationRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute: Get all notifications
     *
     * @param callback Callback to receive result
     */
    public void execute(Callback<List<Notification>> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        repository.getNotifications(new INotificationRepository.RepositoryCallback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> result) {
                // Business logic: Filter out expired notifications if needed
                // For now, return all as-is
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
