package com.example.tralalero.domain.usecase.notification;

import com.example.tralalero.domain.repository.INotificationRepository;


public class DeleteNotificationUseCase {

    private final INotificationRepository repository;

    public DeleteNotificationUseCase(INotificationRepository repository) {
        this.repository = repository;
    }


    public void execute(String notificationId, Callback<Void> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        if (notificationId == null || notificationId.trim().isEmpty()) {
            callback.onError("Notification ID cannot be empty");
            return;
        }

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


    private boolean isValidUUID(String uuid) {
        if (uuid == null) return false;
        String uuidPattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        return uuid.matches(uuidPattern);
    }


    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

