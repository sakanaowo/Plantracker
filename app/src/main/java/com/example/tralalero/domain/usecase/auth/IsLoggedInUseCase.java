package com.example.tralalero.domain.usecase.auth;

import com.example.tralalero.domain.repository.IAuthRepository;


public class IsLoggedInUseCase {

    private final IAuthRepository repository;

    public IsLoggedInUseCase(IAuthRepository repository) {
        this.repository = repository;
    }


    public boolean execute() {
        return repository.isLoggedIn();
    }
}

