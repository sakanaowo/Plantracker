package com.example.tralalero.domain.usecase.notification;

import com.example.tralalero.domain.repository.INotificationRepository;


public class GetNotificationCountUseCase {

    private final INotificationRepository repository;

    public GetNotificationCountUseCase(INotificationRepository repository) {
        this.repository = repository;
    }


    public void execute(Callback<Integer> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        repository.getUnreadCount(new INotificationRepository.RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                
                callback.onSuccess(result);
            }

            @Override
            public void onError(String error) {
                callback.onSuccess(0);
            }
        });
    }


    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
