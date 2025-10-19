package com.example.tralalero.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.tralalero.domain.usecase.auth.GetCurrentUserUseCase;
import com.example.tralalero.domain.usecase.auth.IsLoggedInUseCase;
import com.example.tralalero.domain.usecase.auth.LoginUseCase;
import com.example.tralalero.domain.usecase.auth.LogoutUseCase;
import com.example.tralalero.domain.usecase.auth.SignupUseCase;

/**
 * Factory để tạo AuthViewModel với dependencies injection.
 * 
 * @author Người 1 - Phase 4
 * @date 14/10/2025
 */
public class AuthViewModelFactory implements ViewModelProvider.Factory {
    
    private final LoginUseCase loginUseCase;
    private final SignupUseCase signupUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final IsLoggedInUseCase isLoggedInUseCase;
    
    /**
     * Constructor inject các UseCase dependencies.
     * 
     * @param loginUseCase UseCase xử lý login
     * @param signupUseCase UseCase xử lý signup
     * @param logoutUseCase UseCase xử lý logout
     * @param getCurrentUserUseCase UseCase lấy thông tin user hiện tại
     * @param isLoggedInUseCase UseCase check trạng thái login
     */
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