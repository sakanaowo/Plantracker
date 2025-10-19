package com.example.tralalero.domain.usecase.auth;

import com.example.tralalero.domain.model.User;
import com.example.tralalero.domain.repository.IAuthRepository;


public class GetCurrentUserUseCase {

    private final IAuthRepository repository;

    public GetCurrentUserUseCase(IAuthRepository repository) {
        this.repository = repository;
    }

   
    public void execute(Callback<User> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        repository.getCurrentUser(new IAuthRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result) {
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

