package com.example.tralalero.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.tralalero.domain.usecase.auth.GetCurrentUserUseCase;
import com.example.tralalero.domain.usecase.auth.IsLoggedInUseCase;
import com.example.tralalero.domain.usecase.auth.LoginUseCase;
import com.example.tralalero.domain.usecase.auth.LogoutUseCase;
import com.example.tralalero.domain.usecase.auth.SignupUseCase;

public class AuthViewModelFactory implements ViewModelProvider.Factory {

    private final LoginUseCase loginUseCase;
    private final SignupUseCase signupUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final IsLoggedInUseCase isLoggedInUseCase;

    public AuthViewModelFactory(
            LoginUseCase loginUseCase,
            SignupUseCase signupUseCase,
            LogoutUseCase logoutUseCase,
            GetCurrentUserUseCase getCurrentUserUseCase,
            IsLoggedInUseCase isLoggedInUseCase
    ) {
        this.loginUseCase = loginUseCase;
        this.signupUseCase = signupUseCase;
        this.logoutUseCase = logoutUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.isLoggedInUseCase = isLoggedInUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AuthViewModel.class)) {
            return (T) new AuthViewModel(
                    loginUseCase,
                    signupUseCase,
                    logoutUseCase,
                    getCurrentUserUseCase,
                    isLoggedInUseCase
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}