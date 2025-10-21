package com.example.tralalero.domain.usecase.auth;

import com.example.tralalero.domain.repository.IAuthRepository;


public class SignupUseCase {

    private final IAuthRepository authRepository;

    public SignupUseCase(IAuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    
    public void execute(String email, String password, String name, Callback<IAuthRepository.AuthResult> callback) {
        authRepository.signup(email, password, name, new IAuthRepository.RepositoryCallback<IAuthRepository.AuthResult>() {
            @Override
            public void onSuccess(IAuthRepository.AuthResult result) {
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

