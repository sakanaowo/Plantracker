package com.example.tralalero.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tralalero.domain.model.User;
import com.example.tralalero.domain.repository.IAuthRepository;
import com.example.tralalero.domain.usecase.auth.GetCurrentUserUseCase;
import com.example.tralalero.domain.usecase.auth.IsLoggedInUseCase;
import com.example.tralalero.domain.usecase.auth.LoginUseCase;
import com.example.tralalero.domain.usecase.auth.LogoutUseCase;
import com.example.tralalero.domain.usecase.auth.SignupUseCase;

public class AuthViewModel extends ViewModel {

    private final LoginUseCase loginUseCase;
    private final SignupUseCase signupUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final IsLoggedInUseCase isLoggedInUseCase;
    private final MutableLiveData<User> currentUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoggedInLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public AuthViewModel(
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

        checkLoginStatus();
    }

    public LiveData<User> getCurrentUser() {
        return currentUserLiveData;
    }

    public LiveData<Boolean> isLoggedIn() {
        return isLoggedInLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return loadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void login(String email, String password) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        loginUseCase.execute(email, password, new LoginUseCase.Callback<IAuthRepository.AuthResult>() {
            @Override
            public void onSuccess(IAuthRepository.AuthResult result) {
                loadingLiveData.setValue(false);
                currentUserLiveData.setValue(result.getUser());
                isLoggedInLiveData.setValue(true);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void signup(String email, String password, String name) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        signupUseCase.execute(email, password, name, new SignupUseCase.Callback<IAuthRepository.AuthResult>() {
            @Override
            public void onSuccess(IAuthRepository.AuthResult result) {
                loadingLiveData.setValue(false);
                currentUserLiveData.setValue(result.getUser());
                isLoggedInLiveData.setValue(true);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void logout() {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        logoutUseCase.execute(new LogoutUseCase.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadingLiveData.setValue(false);
                currentUserLiveData.setValue(null);
                isLoggedInLiveData.setValue(false);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void loadCurrentUser() {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getCurrentUserUseCase.execute(new GetCurrentUserUseCase.Callback<User>() {
            @Override
            public void onSuccess(User result) {
                loadingLiveData.setValue(false);
                currentUserLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    private void checkLoginStatus() {
        boolean loggedIn = isLoggedInUseCase.execute();
        isLoggedInLiveData.setValue(loggedIn);
        if (loggedIn) {
            loadCurrentUser();
        }
    }

    public void clearError() {
        errorLiveData.setValue(null);
    }
    @Override
    protected void onCleared() {
        super.onCleared();
    }
}