package com.example.tralalero.domain.repository;

import com.example.tralalero.domain.model.User;

/**
 * Repository Interface for Authentication operations
 *
 * Handles: Login, Logout, Token management, User session
 */
public interface IAuthRepository {

    /**
     * Login with email and password
     *
     * @param email User email
     * @param password User password
     * @param callback Callback with auth result (User + token)
     */
    void login(String email, String password, RepositoryCallback<AuthResult> callback);

    /**
     * Sign up new user with email and password
     *
     * @param email User email
     * @param password User password
     * @param name User display name
     * @param callback Callback with auth result (User + token)
     */
    void signup(String email, String password, String name, RepositoryCallback<AuthResult> callback);

    /**
     * Logout current user
     * Clear tokens and session
     *
     * @param callback Callback for success/failure
     */
    void logout(RepositoryCallback<Void> callback);

    /**
     * Get current logged-in user
     *
     * @param callback Callback with current user
     */
    void getCurrentUser(RepositoryCallback<User> callback);

    /**
     * Check if user is logged in
     *
     * @return true if user has valid session
     */
    boolean isLoggedIn();

    /**
     * Refresh authentication token
     *
     * @param refreshToken Refresh token
     * @param callback Callback with new access token
     */
    void refreshToken(String refreshToken, RepositoryCallback<String> callback);

    /**
     * Auth result containing user and token
     */
    class AuthResult {
        private final User user;
        private final String accessToken;
        private final String refreshToken;

        public AuthResult(User user, String accessToken, String refreshToken) {
            this.user = user;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public User getUser() {
            return user;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }

    /**
     * Repository callback interface
     */
    interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
