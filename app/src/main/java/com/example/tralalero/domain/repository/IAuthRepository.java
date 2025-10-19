package com.example.tralalero.domain.repository;

import com.example.tralalero.domain.model.User;


public interface IAuthRepository {

    
    void login(String email, String password, RepositoryCallback<AuthResult> callback);

    
    void signup(String email, String password, String name, RepositoryCallback<AuthResult> callback);

    
    void logout(RepositoryCallback<Void> callback);

    
    void getCurrentUser(RepositoryCallback<User> callback);

    
    boolean isLoggedIn();

    
    void refreshToken(String refreshToken, RepositoryCallback<String> callback);

    
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


    interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
