package com.example.tralalero.domain.usecase.notification;

import com.example.tralalero.domain.model.Notification;
import com.example.tralalero.domain.repository.INotificationRepository;

import java.util.List;

/**
 * UseCase: Get unread notifications only
 *
 * Matches Backend: GET /api/notifications/unread
 *
 * Input: None
 * Output: List<Notification> (only unread ones)
 *
 * Business Logic:
 * - Retrieve only unread notifications
 * - Used for notification badge count
 */
public class GetUnreadNotificationsUseCase {

    private final INotificationRepository repository;

    public GetUnreadNotificationsUseCase(INotificationRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute: Get unread notifications
     *
     * @param callback Callback to receive result
     */
    public void execute(Callback<List<Notification>> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        repository.getUnreadNotifications(new INotificationRepository.RepositoryCallback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> result) {
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

