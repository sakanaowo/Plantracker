package com.example.tralalero.domain.usecase.auth;

import com.example.tralalero.domain.repository.IAuthRepository;

/**
 * Use Case: Sign up new user with email and password
 *
 * Flow:
 * 1. Validate input (email format, password strength)
 * 2. Create Firebase account
 * 3. Authenticate with backend
 * 4. Save token and user data
 *
 * @author Người 1 - Phase 6
 * @date 15/10/2025
 */
public class SignupUseCase {

    private final IAuthRepository authRepository;

    public SignupUseCase(IAuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    /**
     * Execute signup operation
     *
     * @param email User email
     * @param password User password
     * @param name User display name
     * @param callback Callback with auth result
     */
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

    /**
     * Callback interface for async result
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

