package com.example.tralalero.feature.calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.tralalero.App.App;
import com.example.tralalero.auth.remote.AuthManager;
import com.example.tralalero.data.remote.api.GoogleAuthApiService;
import com.example.tralalero.domain.model.GoogleCalendarStatusResponse;
import com.example.tralalero.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Singleton để quản lý logic sync Google Calendar
 * 
 * Flow hoạt động:
 * 1. Đăng ký/Đăng nhập mới → Kiểm tra trạng thái → Hiện dialog nếu chưa sync
 * 2. Nếu đồng ý kết nối → Đánh dấu "user_synced" → Không hiện nữa
 * 3. Nếu từ chối → Cache "last_declined" 24h → Hiện lại sau 24h
 * 4. Khi logout → Xóa tất cả cache của user
 * 5. Đã đăng nhập + cache hết → Kiểm tra lại → Hiện dialog nếu chưa sync
 */
public class CalendarSyncManager {
    private static final String TAG = "CalendarSyncManager";
    private static CalendarSyncManager instance;
    
    private static final String PREFS_NAME = "CalendarSyncPrefs";
    private static final String KEY_USER_SYNCED_PREFIX = "user_synced_"; // true nếu user đã connect
    private static final String KEY_LAST_DECLINED_PREFIX = "last_declined_"; // timestamp lần cuối từ chối
    private static final long DECLINE_CACHE_DURATION = 24 * 60 * 60 * 1000; // 24 giờ
    
    private final Context context;
    private final SharedPreferences prefs;
    private final GoogleAuthApiService apiService;
    
    private CalendarSyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        AuthManager authManager = App.authManager;
        this.apiService = ApiClient.get(authManager).create(GoogleAuthApiService.class);
    }
    
    public static synchronized CalendarSyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new CalendarSyncManager(context);
        }
        return instance;
    }
    
    /**
     * Kiểm tra xem có nên hiển thị dialog đề xuất sync không
     * 
     * @param userId User ID hiện tại
     * @param callback Callback trả về kết quả
     */
    public void shouldShowSyncPrompt(String userId, ShouldShowCallback callback) {
        if (userId == null) {
            Log.w(TAG, "userId is null, cannot check sync prompt");
            callback.onResult(false);
            return;
        }
        
        // Kiểm tra xem user đã sync chưa (cache local)
        String syncedKey = KEY_USER_SYNCED_PREFIX + userId;
        boolean userHasSynced = prefs.getBoolean(syncedKey, false);
        
        if (userHasSynced) {
            Log.d(TAG, "User already synced (local cache), skip prompt");
            callback.onResult(false);
            return;
        }
        
        // Kiểm tra cache từ chối (24h cooldown)
        String declinedKey = KEY_LAST_DECLINED_PREFIX + userId;
        long lastDeclined = prefs.getLong(declinedKey, 0);
        long currentTime = System.currentTimeMillis();
        
        if (lastDeclined > 0 && (currentTime - lastDeclined) < DECLINE_CACHE_DURATION) {
            long hoursRemaining = (DECLINE_CACHE_DURATION - (currentTime - lastDeclined)) / (60 * 60 * 1000);
            Log.d(TAG, "User declined recently, skip prompt (" + hoursRemaining + "h remaining)");
            callback.onResult(false);
            return;
        }
        
        // Cache hết hoặc chưa từng từ chối → Kiểm tra trạng thái sync từ server
        Log.d(TAG, "Checking sync status from server...");
        checkSyncStatusFromServer(userId, callback);
    }
    
    /**
     * Kiểm tra trạng thái sync từ server
     */
    private void checkSyncStatusFromServer(String userId, ShouldShowCallback callback) {
        apiService.getIntegrationStatus().enqueue(new Callback<GoogleCalendarStatusResponse>() {
            @Override
            public void onResponse(Call<GoogleCalendarStatusResponse> call, Response<GoogleCalendarStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean isConnected = response.body().isConnected();
                    
                    if (isConnected) {
                        // User đã sync từ nơi khác → Cập nhật cache local
                        Log.d(TAG, "User already synced (from server), updating cache");
                        markUserAsSynced(userId);
                        callback.onResult(false);
                    } else {
                        // Chưa sync → Hiện dialog
                        Log.d(TAG, "User not synced, should show prompt");
                        callback.onResult(true);
                    }
                } else {
                    // Lỗi API → Không hiện dialog (fail-safe)
                    Log.w(TAG, "Failed to check sync status: " + response.code());
                    callback.onResult(false);
                }
            }
            
            @Override
            public void onFailure(Call<GoogleCalendarStatusResponse> call, Throwable t) {
                // Lỗi mạng → Không hiện dialog (fail-safe)
                Log.e(TAG, "Network error checking sync status", t);
                callback.onResult(false);
            }
        });
    }
    
    /**
     * Đánh dấu user đã đồng ý sync (connect thành công)
     * → Không hiện dialog nữa cho đến khi logout
     */
    public void markUserAsSynced(String userId) {
        if (userId == null) return;
        
        String syncedKey = KEY_USER_SYNCED_PREFIX + userId;
        prefs.edit()
            .putBoolean(syncedKey, true)
            .remove(KEY_LAST_DECLINED_PREFIX + userId) // Xóa cache từ chối
            .apply();
        
        Log.d(TAG, "Marked user as synced: " + userId);
    }
    
    /**
     * Đánh dấu user từ chối sync (Maybe Later)
     * → Cache 24h, hiện lại sau 24h
     */
    public void markUserDeclined(String userId) {
        if (userId == null) return;
        
        String declinedKey = KEY_LAST_DECLINED_PREFIX + userId;
        prefs.edit()
            .putLong(declinedKey, System.currentTimeMillis())
            .apply();
        
        Log.d(TAG, "Marked user declined sync: " + userId);
    }
    
    /**
     * Xóa tất cả cache của user (khi logout)
     * → Hiện lại dialog khi user đăng nhập lại
     */
    public void clearUserCache(String userId) {
        if (userId == null) return;
        
        String syncedKey = KEY_USER_SYNCED_PREFIX + userId;
        String declinedKey = KEY_LAST_DECLINED_PREFIX + userId;
        
        prefs.edit()
            .remove(syncedKey)
            .remove(declinedKey)
            .apply();
        
        Log.d(TAG, "Cleared calendar sync cache for user: " + userId);
    }
    
    /**
     * Interface callback cho shouldShowSyncPrompt
     */
    public interface ShouldShowCallback {
        void onResult(boolean shouldShow);
    }
}
