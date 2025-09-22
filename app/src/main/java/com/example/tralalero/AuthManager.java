package com.example.tralalero;

public class AuthManager {
    private boolean isSignedIn = false;

    public void signOut() {
        // Clear any auth tokens, user data, etc.
        isSignedIn = false;
    }

    public boolean isSignedIn() {
        return isSignedIn;
    }

    // TODO: Add sign in methods and other auth-related functionality
}