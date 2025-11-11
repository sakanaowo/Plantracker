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

    // AuthState enum for tracking authentication flow
    public enum AuthState {
        IDLE,           // Initial state
        LOGGING_IN,     // Login in progress
        LOGIN_SUCCESS,  // Login successful
        LOGIN_ERROR,    // Login failed
        SIGNING_UP,     // Signup in progress
        SIGNUP_SUCCESS, // Signup successful
        SIGNUP_ERROR,   // Signup failed
        LOGGED_OUT      // User logged out
    }

    private final LoginUseCase loginUseCase;
    private final SignupUseCase signupUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final IsLoggedInUseCase isLoggedInUseCase;
    private final MutableLiveData<User> currentUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoggedInLiveData = new MutableLiveData<>();
    private final MutableLiveData<AuthState> authStateLiveData = new MutableLiveData<>(AuthState.IDLE);
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

        // Auto-restore session on ViewModel creation
        checkStoredSession();
    }

    public LiveData<User> getCurrentUser() {
        return currentUserLiveData;
    }

    public LiveData<Boolean> isLoggedIn() {
        return isLoggedInLiveData;
    }

    public LiveData<AuthState> getAuthState() {
        return authStateLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return loadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void login(String email, String password) {
        authStateLiveData.setValue(AuthState.LOGGING_IN);
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        loginUseCase.execute(email, password, new LoginUseCase.Callback<IAuthRepository.AuthResult>() {
            @Override
            public void onSuccess(IAuthRepository.AuthResult result) {
                loadingLiveData.setValue(false);
                currentUserLiveData.setValue(result.getUser());
                isLoggedInLiveData.setValue(true);
                authStateLiveData.setValue(AuthState.LOGIN_SUCCESS);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                authStateLiveData.setValue(AuthState.LOGIN_ERROR);
            }
        });
    }

    public void signup(String email, String password, String name) {
        authStateLiveData.setValue(AuthState.SIGNING_UP);
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        signupUseCase.execute(email, password, name, new SignupUseCase.Callback<IAuthRepository.AuthResult>() {
            @Override
            public void onSuccess(IAuthRepository.AuthResult result) {
                loadingLiveData.setValue(false);
                currentUserLiveData.setValue(result.getUser());
                isLoggedInLiveData.setValue(true);
                authStateLiveData.setValue(AuthState.SIGNUP_SUCCESS);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                authStateLiveData.setValue(AuthState.SIGNUP_ERROR);
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
                authStateLiveData.setValue(AuthState.LOGGED_OUT);
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
                // ✅ Ensure authState is LOGIN_SUCCESS only when user data loaded
                authStateLiveData.setValue(AuthState.LOGIN_SUCCESS);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
                // ✅ FIX ERROR HANDLING: Set state to LOGGED_OUT if can't load user
                authStateLiveData.setValue(AuthState.LOGGED_OUT);
                isLoggedInLiveData.setValue(false);
            }
        });
    }

    private void checkStoredSession() {
        boolean loggedIn = isLoggedInUseCase.execute();
        isLoggedInLiveData.setValue(loggedIn);
        if (loggedIn) {
            // ✅ FIX: Don't set LOGIN_SUCCESS until user data is loaded
            authStateLiveData.setValue(AuthState.LOGGING_IN);
            loadCurrentUser(); // Will set LOGIN_SUCCESS on success, LOGGED_OUT on error
        } else {
            authStateLiveData.setValue(AuthState.IDLE);
        }
    }

    public void clearError() {
        errorLiveData.setValue(null);
        authStateLiveData.setValue(AuthState.IDLE);
    }
    @Override
    protected void onCleared() {
        super.onCleared();
    }
}