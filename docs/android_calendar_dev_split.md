# 📱 ANDROID CALENDAR INTEGRATION - DEV TEAM SPLIT

**Total**: 12-15 hours → **Split**: 6-8 hours mỗi dev  
**Parallel Development**: 2 devs làm độc lập, minimal dependencies

---

## 👨‍💻 **DEV A: DATA LAYER & API INTEGRATION** 
**Time**: 6-7 hours | **Focus**: Backend communication & data handling

### **🎯 SCOPE DEV A:**
- API Services & DTOs
- Repository implementation  
- Domain models
- Error handling & network logic
- **NO UI components** - chỉ data layer

### **📋 DEV A TASKS:**

#### **Task A1: API Service Layer (2h)**
**Files to create:**
```
data/remote/api/CalendarApiService.java
data/remote/dto/calendar/AuthUrlResponse.java  
data/remote/dto/calendar/CallbackResponse.java
data/remote/dto/calendar/SyncResponse.java
data/remote/dto/calendar/IntegrationStatusResponse.java
```

**CalendarApiService.java:**
```java
package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.calendar.*;
import retrofit2.Call;
import retrofit2.http.*;

public interface CalendarApiService {
    @GET("calendar/google/auth-url")
    Call<AuthUrlResponse> getGoogleAuthUrl();
    
    @POST("calendar/google/callback")
    Call<CallbackResponse> handleGoogleCallback(
        @Query("code") String authCode,
        @Query("state") String state
    );
    
    @POST("calendar/sync")
    Call<SyncResponse> syncEvents(@Query("projectId") String projectId);
    
    @GET("calendar/integration-status") 
    Call<IntegrationStatusResponse> getIntegrationStatus();
    
    @DELETE("calendar/integration")
    Call<Void> disconnectCalendarIntegration();
}
```

**DTO Classes structure (all similar pattern):**
```java
public class AuthUrlResponse {
    @SerializedName("authUrl")
    private String authUrl;
    
    // Getters/setters
}
```

#### **Task A2: Repository Interface & Implementation (3-4h)**
**Files to create:**
```
domain/repository/ICalendarRepository.java
data/repository/CalendarRepositoryImpl.java
domain/model/CalendarIntegrationStatus.java
```

**ICalendarRepository.java:**
```java
package com.example.tralalero.domain.repository;

public interface ICalendarRepository {
    void getGoogleAuthUrl(RepositoryCallback<String> callback);
    void handleOAuthCallback(String authCode, String state, RepositoryCallback<Boolean> callback);
    void syncEvents(String projectId, RepositoryCallback<Boolean> callback);
    void getIntegrationStatus(RepositoryCallback<CalendarIntegrationStatus> callback);
    void disconnectIntegration(RepositoryCallback<Boolean> callback);
    
    interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
```

**CalendarRepositoryImpl.java:**
- Full Retrofit implementation
- Error handling logic
- ApiClient integration
- Response mapping
- ~150-200 lines of code

#### **Task A3: Domain Models & Error Handling (1h)**
**CalendarIntegrationStatus.java:**
```java
public class CalendarIntegrationStatus {
    private boolean isConnected;
    private String accountEmail;
    private String lastSyncAt;
    
    // Constructor, getters, setters
}
```

**Error handling patterns:**
- Network failures
- API error codes
- Null response handling
- Timeout handling

#### **Task A4: Unit Testing (1h)**
**Test files:**
```
test/java/.../CalendarRepositoryImplTest.java
test/java/.../CalendarApiServiceTest.java
```

**Testing scope:**
- Repository success/error callbacks
- API response mapping  
- Error handling scenarios
- Mock Retrofit responses

### **🔧 DEV A DELIVERABLES:**
1. ✅ Complete API layer working with backend
2. ✅ Repository pattern implementation
3. ✅ Error handling & network logic
4. ✅ Unit tests for data layer
5. ✅ Integration ready for UI consumption

---

## 🎨 **DEV B: UI LAYER & USER EXPERIENCE**
**Time**: 6-8 hours | **Focus**: OAuth flow & user interface

### **🎯 SCOPE DEV B:**
- OAuth WebView activity
- Settings UI fragments
- Deep link handling
- UI/UX integration with existing app
- **NO data layer** - consumes Dev A's interfaces

### **📋 DEV B TASKS:**

#### **Task B1: OAuth WebView Activity (3-4h)**
**Files to create:**
```
feature/calendar/GoogleCalendarOAuthActivity.java
res/layout/activity_google_calendar_oauth.xml
```

**GoogleCalendarOAuthActivity.java key features:**
- WebView setup with JavaScript enabled
- OAuth URL loading 
- Deep link callback handling (`plantracker://oauth/callback`)
- Loading states & error handling
- Result passing back to caller

**WebView configuration:**
```java
webView.setWebViewClient(new WebViewClient() {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.startsWith("plantracker://oauth/callback")) {
            handleOAuthCallback(url);
            return true;
        }
        return false;
    }
});
```

#### **Task B2: Settings UI Fragment (2-3h)**
**Files to create:**
```
feature/settings/CalendarIntegrationFragment.java
res/layout/fragment_calendar_integration.xml
```

**CalendarIntegrationFragment features:**
- Integration status display (connected/disconnected)
- Connect/Disconnect buttons
- Manual sync button
- Account email display
- Last sync timestamp
- ActivityResultLauncher for OAuth

**UI States:**
- Loading state during operations
- Success/error feedback
- Button enable/disable logic
- Status text color changes

#### **Task B3: AndroidManifest & Deep Link Setup (1h)**
**AndroidManifest.xml updates:**
```xml
<activity
    android:name=".feature.calendar.GoogleCalendarOAuthActivity"
    android:exported="true">
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="plantracker"
              android:host="oauth"
              android:path="/callback" />
    </intent-filter>
</activity>
```

**Proguard rules:**
```proguard
-keep class com.example.tralalero.data.remote.dto.calendar.** { *; }
-keep class com.example.tralalero.domain.model.CalendarIntegrationStatus { *; }
```

#### **Task B4: Existing UI Integration (1h)**
**Files to modify:**
```
ProjectDetailActivity.java (add menu option)
CreateEventDialog.java (add sync checkbox)  
EventAdapter.java (add sync status icon)
```

**Integration points:**
- Project settings menu → Calendar Integration option
- Event creation → "Sync to Google Calendar" checkbox
- Event list → Calendar sync status indicator

### **🔧 DEV B DELIVERABLES:**
1. ✅ OAuth flow working end-to-end
2. ✅ Settings UI with all states
3. ✅ Deep link handling tested
4. ✅ Integration with existing app UI
5. ✅ User experience polished

---

## 🔗 **COORDINATION & DEPENDENCIES**

### **📞 Communication Protocol:**

**Week 1 (Days 1-2):**
- **Dev A**: Focus on API interfaces first
- **Dev B**: Create mock implementations for testing UI
- **Sync point**: Dev A provides interface definitions

**Week 1 (Days 3-4):**
- **Dev A**: Complete repository implementation
- **Dev B**: Integrate real API calls replacing mocks
- **Sync point**: Integration testing

**Week 1 (Day 5):**
- **Both**: Bug fixes and polish
- **Integration testing**
- **Code review**

### **🔄 Interface Contract (Dev A → Dev B):**

```java
// Dev A provides this interface, Dev B consumes it
ICalendarRepository calendarRepo = new CalendarRepositoryImpl(context);

// Dev B uses these methods in UI:
calendarRepo.getGoogleAuthUrl(callback);
calendarRepo.getIntegrationStatus(callback);
calendarRepo.syncEvents(projectId, callback);
```

### **🧪 Mock Strategy for Dev B:**

**MockCalendarRepository.java (Dev B creates temporarily):**
```java
public class MockCalendarRepository implements ICalendarRepository {
    @Override
    public void getGoogleAuthUrl(RepositoryCallback<String> callback) {
        // Mock OAuth URL
        callback.onSuccess("https://accounts.google.com/oauth2/auth?...");
    }
    
    @Override 
    public void getIntegrationStatus(RepositoryCallback<CalendarIntegrationStatus> callback) {
        // Mock connected status
        callback.onSuccess(new CalendarIntegrationStatus(true, "test@gmail.com", "2 hours ago"));
    }
    
    // ... other mocked methods
}
```

### **📋 TESTING COORDINATION:**

**Dev A Testing:**
- Unit tests for repository logic
- API integration tests with Postman/curl
- Error scenario testing

**Dev B Testing:**
- UI flow testing with mocks
- Deep link testing with ADB commands:
  ```bash
  adb shell am start -W -a android.intent.action.VIEW -d "plantracker://oauth/callback?code=test123&state=xyz" com.example.tralalero
  ```
- User experience testing

**Integration Testing (Both):**
- End-to-end OAuth flow
- Real API calls + UI response
- Error handling integration
- Performance testing

---

## ⏱️ **TIMELINE OPTIMIZATION**

### **Parallel Development (5 days):**

| Day | Dev A Tasks | Dev B Tasks |
|-----|-------------|-------------|  
| 1 | API interfaces + DTOs | OAuth Activity setup + Mock repo |
| 2 | Repository implementation | Settings Fragment + Layouts |
| 3 | Error handling + Domain models | Deep link + Manifest setup |
| 4 | **Integration**: Replace Dev B's mocks | Existing UI integration |
| 5 | Testing + Bug fixes | Testing + Polish |

### **🎯 SUCCESS METRICS:**

**Dev A Success:**
- ✅ All API endpoints working
- ✅ Repository tests passing  
- ✅ Error scenarios handled
- ✅ Ready for UI consumption

**Dev B Success:**
- ✅ OAuth flow complete
- ✅ Settings UI functional
- ✅ Deep links working
- ✅ Integrated with existing app

**Combined Success:**
- ✅ End-to-end calendar sync working
- ✅ User can connect/disconnect Google Calendar
- ✅ Events sync bi-directionally
- ✅ Error handling graceful

---

## 🚨 **RISK MITIGATION**

### **Potential Conflicts:**
1. **Interface changes**: Dev A communicates interface updates immediately
2. **Dependency delays**: Dev B uses mocks until Dev A ready
3. **Testing conflicts**: Separate test environments/projects

### **Backup Plans:**
1. **Dev A delayed**: Dev B continues with extended mock testing
2. **Dev B delayed**: Dev A can create basic UI for testing
3. **Integration issues**: Daily sync meetings for quick resolution

**🎯 Result**: 2 devs working efficiently in parallel, minimal waiting time, clean handoff points!