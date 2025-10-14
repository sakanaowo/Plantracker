package com.example.tralalero.domain.usecase.auth;

import com.example.tralalero.domain.repository.IAuthRepository;

/**
 * UseCase: Logout current user
 *
 * Matches Backend: POST /api/auth/logout
 *
 * Input: None
 * Output: Void (success/failure)
 *
 * Business Logic:
 * - Clear stored tokens (access token, refresh token)
 * - Clear user session
 * - Navigate to login screen
 */
public class LogoutUseCase {

    private final IAuthRepository repository;

    public LogoutUseCase(IAuthRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute: Logout user
     *
     * @param callback Callback to receive result
     */
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

    /**
     * Callback interface for use case result
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

