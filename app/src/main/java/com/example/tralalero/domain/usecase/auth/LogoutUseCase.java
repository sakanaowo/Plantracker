package com.example.tralalero.domain.usecase.auth;

import com.example.tralalero.domain.repository.IAuthRepository;


public class LogoutUseCase {

    private final IAuthRepository repository;

    public LogoutUseCase(IAuthRepository repository) {
        this.repository = repository;
    }


    public void execute(Callback<Void> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        repository.logout(new IAuthRepository.RepositoryCallback<Void>() {
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

