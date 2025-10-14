package com.example.tralalero.domain.usecase.auth;

import com.example.tralalero.domain.model.User;
import com.example.tralalero.domain.repository.IAuthRepository;

/**
 * UseCase: Get current logged-in user
 *
 * Matches Backend: GET /api/auth/me
 *
 * Input: None (uses stored token)
 * Output: User (current user info)
 *
 * Business Logic:
 * - Retrieve current user from session/token
 * - Used on app startup to check if user is logged in
 * - Used to display user info in profile screen
 */
public class GetCurrentUserUseCase {

    private final IAuthRepository repository;

    public GetCurrentUserUseCase(IAuthRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute: Get current user
     *
     * @param callback Callback to receive result
     */
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

    /**
     * Callback interface for use case result
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

