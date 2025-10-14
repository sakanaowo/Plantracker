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

/**
 * ViewModel quản lý authentication state và operations.
 * Xử lý login, logout, và theo dõi trạng thái đăng nhập của user.
 */
public class AuthViewModel extends ViewModel {
    
    // ========== Dependencies ==========
    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final IsLoggedInUseCase isLoggedInUseCase;
    
    // ========== LiveData (UI State) ==========
    private final MutableLiveData<User> currentUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoggedInLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    
    // ========== Constructor ==========
    /**
     * Constructor inject các UseCase dependencies.
     * Tự động check login status khi khởi tạo.
     * 
     * @param loginUseCase UseCase xử lý đăng nhập
     * @param logoutUseCase UseCase xử lý đăng xuất
     * @param getCurrentUserUseCase UseCase lấy thông tin user hiện tại
     * @param isLoggedInUseCase UseCase kiểm tra trạng thái đăng nhập
     */
    public AuthViewModel(
            LoginUseCase loginUseCase,
            LogoutUseCase logoutUseCase,
            GetCurrentUserUseCase getCurrentUserUseCase,
            IsLoggedInUseCase isLoggedInUseCase
    ) {
        this.loginUseCase = loginUseCase;
        this.logoutUseCase = logoutUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.isLoggedInUseCase = isLoggedInUseCase;
        
        // Check login status on initialization
        checkLoginStatus();
    }
    
    // ========== Getters (Public API) ==========
    
    /**
     * @return LiveData chứa thông tin user hiện tại (null nếu chưa login)
     */
    public LiveData<User> getCurrentUser() {
        return currentUserLiveData;
    }
    
    /**
     * @return LiveData trạng thái đăng nhập (true/false)
     */
    public LiveData<Boolean> isLoggedIn() {
        return isLoggedInLiveData;
    }
    
    /**
     * @return LiveData trạng thái loading
     */
    public LiveData<Boolean> isLoading() {
        return loadingLiveData;
    }
    
    /**
     * @return LiveData chứa error message (null nếu không có lỗi)
     */
    public LiveData<String> getError() {
        return errorLiveData;
    }
    
    // ========== Public Methods ==========
    
    /**
     * Đăng nhập với email và password.
     * Set loading state và handle success/error callbacks.
     * 
     * @param email Email của user
     * @param password Password của user
     */
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
    
    /**
     * Đăng xuất user hiện tại.
     * Clear user data và update login state.
     */
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
    
    /**
     * Load thông tin user hiện tại từ server/cache.
     * Dùng khi cần refresh user data.
     */
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
    
    /**
     * Check xem user có đang đăng nhập hay không.
     * Update isLoggedInLiveData và load user data nếu đã login.
     */
    private void checkLoginStatus() {
        boolean loggedIn = isLoggedInUseCase.execute();
        isLoggedInLiveData.setValue(loggedIn);
        
        // Nếu đã login, load user data
        if (loggedIn) {
            loadCurrentUser();
        }
    }
    
    /**
     * Clear error message.
     * Dùng sau khi UI đã hiển thị error.
     */
    public void clearError() {
        errorLiveData.setValue(null);
    }
    
    // ========== Lifecycle ==========
    @Override
    protected void onCleared() {
        super.onCleared();
        // Cleanup nếu cần (hiện tại không cần vì không có background tasks)
    }
}