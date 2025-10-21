package com.example.tralalero.domain.usecase.auth;

import com.example.tralalero.domain.model.User;
import com.example.tralalero.domain.repository.IAuthRepository;


public class LoginUseCase {

    private final IAuthRepository repository;

    public LoginUseCase(IAuthRepository repository) {
        this.repository = repository;
    }

    
    public void execute(String email, String password, Callback<IAuthRepository.AuthResult> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        if (email == null || email.trim().isEmpty()) {
            callback.onError("Email cannot be empty");
            return;
        }

        if (!isValidEmail(email)) {
            callback.onError("Invalid email format");
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            callback.onError("Password cannot be empty");
            return;
        }

        if (password.length() < 6) {
            callback.onError("Password must be at least 6 characters");
            return;
        }

        repository.login(email, password, new IAuthRepository.RepositoryCallback<IAuthRepository.AuthResult>() {
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


    private boolean isValidEmail(String email) {
        if (email == null) return false;
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailPattern);
    }


    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

