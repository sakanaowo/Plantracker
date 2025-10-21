package com.example.tralalero.domain.usecase.notification;

import com.example.tralalero.domain.repository.INotificationRepository;


public class DeleteAllNotificationsUseCase {

    private final INotificationRepository repository;

    public DeleteAllNotificationsUseCase(INotificationRepository repository) {
        this.repository = repository;
    }


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


    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

