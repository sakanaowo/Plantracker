package com.example.tralalero.domain.usecase.auth;

import com.example.tralalero.domain.repository.IAuthRepository;

/**
 * UseCase: Check if user is logged in
 *
 * Input: None
 * Output: Boolean (true if logged in)
 *
 * Business Logic:
 * - Check if valid token exists
 * - Used on app startup to determine navigation (Login vs Home)
 * - Lightweight operation (no API call)
 */
public class IsLoggedInUseCase {

    private final IAuthRepository repository;

    public IsLoggedInUseCase(IAuthRepository repository) {
        this.repository = repository;
    }

    /**
     * Execute: Check login status
     *
     * @return true if user is logged in
     */
    public boolean execute() {
        return repository.isLoggedIn();
    }
}

