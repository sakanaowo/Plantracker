package com.example.tralalero.domain.usecase.notification;

import com.example.tralalero.domain.model.Notification;
import com.example.tralalero.domain.repository.INotificationRepository;

import java.util.List;


public class GetNotificationsUseCase {

    private final INotificationRepository repository;

    public GetNotificationsUseCase(INotificationRepository repository) {
        this.repository = repository;
    }


    public void execute(Callback<List<Notification>> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        repository.getNotifications(new INotificationRepository.RepositoryCallback<List<Notification>>() {
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


    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
