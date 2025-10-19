# Đề xuất Cấu trúc Package Thực tế - Plantracker

## 🎯 Mục tiêu

Đề xuất này dựa trên **cấu trúc hiện tại** của project, chỉ refactor những gì CẦN THIẾT để:
- ✅ Giữ lại những phần tốt (auth module)
- ✅ Sửa những vấn đề rõ ràng (package naming, organization)
- ✅ Dễ migrate (không cần viết lại toàn bộ)
- ✅ Team có thể làm từng phần một

---

## 📦 Cấu trúc Đề xuất (Practical & Realistic)

```
com.example.plantracker/                                    # ← Đổi tên từ tralalero
│
├── 📱 MainActivity.java
│
├── 🏢 app/                                                 # ← Đổi từ App/ (lowercase)
│   └── PlanTrackerApp.java                                # ← Đổi từ App.java (tên rõ ràng hơn)
│
├── 🔧 core/                                                # ← NEW: Core components dùng chung
│   │
│   ├── network/                                            # ← Di chuyển từ network/
│   │   ├── ApiClient.java
│   │   ├── interceptor/                                    # ← NEW: Tổ chức interceptors
│   │   │   └── AuthInterceptor.java                       # ← Di chuyển từ auth/remote/
│   │   └── authenticator/                                  # ← NEW: Tổ chức authenticators
│   │       └── TokenAuthenticator.java                     # ← Di chuyển từ auth/remote/
│   │
│   ├── storage/                                            # ← NEW: Local storage
│   │   └── TokenManager.java                              # ← Di chuyển từ auth/storage/
│   │
│   ├── sync/                                               # ← Di chuyển từ sync/
│   │   └── StartupSyncWorker.java
│   │
│   └── base/                                               # ← NEW: Base classes (optional)
│       ├── BaseActivity.java
│       └── BaseAdapter.java
│
├── 🎯 feature/                                             # Feature modules
│   │
│   ├── auth/                                               # ✅ Auth feature (giữ nguyên cấu trúc tốt)
│   │   │
│   │   ├── data/                                           # ← Đổi tên từ remote/
│   │   │   ├── api/                                        # ← API interfaces
│   │   │   │   ├── AuthApi.java
│   │   │   │   └── PublicAuthApi.java
│   │   │   ├── dto/                                        # ← Data Transfer Objects
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── LoginResponse.java
│   │   │   │   ├── UserDto.java
│   │   │   │   ├── FirebaseAuthDto.java
│   │   │   │   └── FirebaseAuthResponse.java
│   │   │   └── repository/                                 # ← Repository implementation
│   │   │       ├── AuthRepository.java                     # ← Interface
│   │   │       └── AuthRepositoryImpl.java                 # ← Đổi từ FirebaseAuthRepository
│   │   │
│   │   ├── domain/                                         # ← NEW: Business logic (optional - có thể bỏ qua)
│   │   │   └── model/
│   │   │       └── User.java                               # ← Domain model (nếu cần)
│   │   │
│   │   └── ui/                                             # ✅ UI layer (giữ nguyên)
│   │       ├── login/
│   │       │   ├── LoginActivity.java
│   │       │   └── GoogleSignInHelper.java                 # ← Đổi từ ContinueWithGoogle
│   │       ├── signup/
│   │       │   └── SignupActivity.java
│   │       └── forgot/
│   │           └── ForgotPasswordActivity.java
│   │
│   ├── workspace/                                          # ← NEW: Tách từ home/
│   │   │
│   │   ├── data/
│   │   │   ├── api/
│   │   │   │   └── WorkspaceApi.java                       # ← Di chuyển từ network/api/
│   │   │   ├── dto/
│   │   │   │   └── WorkspaceDto.java                       # ← API response model
│   │   │   └── repository/
│   │   │       ├── WorkspaceRepository.java                # ← NEW: Interface
│   │   │       └── WorkspaceRepositoryImpl.java            # ← NEW: Implementation
│   │   │
│   │   ├── model/                                          # ← Di chuyển từ model/
│   │   │   └── Workspace.java                              # Domain model
│   │   │
│   │   └── ui/
│   │       ├── WorkspaceActivity.java                      # ← Di chuyển từ home/ui/
│   │       └── adapter/
│   │           └── WorkspaceAdapter.java                   # ← Di chuyển từ adapter/
│   │
│   ├── board/                                              # ← NEW: Tách từ home/
│   │   │
│   │   ├── data/
│   │   │   ├── api/
│   │   │   │   └── BoardApi.java                           # ← NEW: Board API
│   │   │   ├── dto/
│   │   │   │   └── BoardDto.java
│   │   │   └── repository/
│   │   │       ├── BoardRepository.java
│   │   │       └── BoardRepositoryImpl.java
│   │   │
│   │   ├── model/
│   │   │   ├── Board.java
│   │   │   └── BoardPage.java
│   │   │
│   │   └── ui/
│   │       ├── create/
│   │       │   └── NewBoardActivity.java                   # ← Đổi từ NewBoard.java
│   │       ├── detail/
│   │       │   ├── BoardDetailActivity.java                # ← Đổi từ MainBoardDetail
│   │       │   └── adapter/
│   │       │       └── BoardPageAdapter.java
│   │       └── adapter/
│   │           └── BoardListAdapter.java                   # ← NEW: Nếu có list
│   │
│   ├── inbox/                                              # ← NEW: Tách từ home/
│   │   │
│   │   ├── data/
│   │   │   ├── api/
│   │   │   │   └── InboxApi.java
│   │   │   ├── dto/
│   │   │   │   └── InboxItemDto.java
│   │   │   └── repository/
│   │   │       ├── InboxRepository.java
│   │   │       └── InboxRepositoryImpl.java
│   │   │
│   │   ├── model/
│   │   │   └── InboxItem.java
│   │   │
│   │   └── ui/
│   │       ├── InboxActivity.java                          # ← Di chuyển từ home/ui/
│   │       ├── fragment/
│   │       │   └── InboxListFragment.java                  # ← Đổi từ InboxListFrm
│   │       └── adapter/
│   │           └── InboxAdapter.java
│   │
│   ├── activity/                                           # ← NEW: Tách từ home/ (đổi tên thành timeline)
│   │   │                                                   # hoặc activitylog để tránh nhầm với Activity class
│   │   ├── data/
│   │   │   ├── api/
│   │   │   │   └── ActivityApi.java
│   │   │   ├── dto/
│   │   │   │   └── ActivityItemDto.java
│   │   │   └── repository/
│   │   │       ├── ActivityRepository.java
│   │   │       └── ActivityRepositoryImpl.java
│   │   │
│   │   ├── model/
│   │   │   └── ActivityItem.java
│   │   │
│   │   └── ui/
│   │       ├── ActivityTimelineActivity.java               # ← Đổi từ ActivityActivity
│   │       ├── fragment/
│   │       │   └── ActivityListFragment.java               # ← Đổi từ ListFragment
│   │       └── adapter/
│   │           └── ActivityListAdapter.java                # ← Đổi từ ListFrmAdapter
│   │
│   ├── account/                                            # ← NEW: Tách từ home/
│   │   │
│   │   ├── data/
│   │   │   ├── api/
│   │   │   │   └── AccountApi.java
│   │   │   ├── dto/
│   │   │   │   └── UserProfileDto.java
│   │   │   └── repository/
│   │   │       ├── AccountRepository.java
│   │   │       └── AccountRepositoryImpl.java
│   │   │
│   │   ├── model/
│   │   │   └── UserProfile.java
│   │   │
│   │   └── ui/
│   │       └── AccountActivity.java                        # ← Di chuyển từ home/ui/
│   │
│   └── home/                                               # ← GIỮ LẠI: Dashboard chính
│       │
│       ├── data/
│       │   ├── api/
│       │   │   └── DashboardApi.java                       # ← NEW: Dashboard data
│       │   ├── dto/
│       │   │   └── DashboardDto.java
│       │   └── repository/
│       │       ├── DashboardRepository.java
│       │       └── DashboardRepositoryImpl.java
│       │
│       ├── model/
│       │   └── DashboardData.java
│       │
│       └── ui/
│           └── HomeActivity.java                           # ← Giữ nguyên (main dashboard)
│
└── 🧰 util/                                                # ← NEW: Utilities (optional)
    ├── Constants.java
    ├── DateUtil.java
    └── ValidationUtil.java
```

---

## 🔄 Migration Plan (Kế hoạch di chuyển CỤ THỂ)

### **Phase 1: Rename & Reorganize Core (1 ngày)**

#### Step 1.1: Đổi tên package root (Optional - có thể bỏ qua)
```
com.example.tralalero → com.example.plantracker
```
⚠️ **Lưu ý**: Nếu đổi, phải update:
- `build.gradle.kts`: `namespace` và `applicationId`
- `AndroidManifest.xml`: package name
- Tất cả imports

**💡 Khuyến nghị**: BỎ QUA bước này nếu app đã deploy, chỉ làm nếu còn development.

#### Step 1.2: Tạo package `core/`
```
✅ Tạo: core/network/
✅ Tạo: core/storage/
✅ Tạo: core/sync/
```

#### Step 1.3: Di chuyển files vào `core/`
```
network/ApiClient.java                     → core/network/ApiClient.java
auth/storage/TokenManager.java             → core/storage/TokenManager.java
sync/StartupSyncWorker.java                → core/sync/StartupSyncWorker.java
auth/remote/FirebaseInterceptor.java       → core/network/interceptor/AuthInterceptor.java
auth/remote/FirebaseAuthenticator.java     → core/network/authenticator/TokenAuthenticator.java
```

#### Step 1.4: Đổi tên `App/` → `app/`
```
App/App.java → app/PlanTrackerApp.java
```

---

### **Phase 2: Refactor Auth Feature (1 ngày)**

#### Step 2.1: Tổ chức lại auth/
```
auth/remote/           → auth/data/api/
auth/remote/dto/       → auth/data/dto/
auth/repository/       → auth/data/repository/
```

#### Step 2.2: Tạo interface cho Repository
```java
// NEW: auth/data/repository/AuthRepository.java
public interface AuthRepository {
    LoginResponse login(LoginRequest request);
    void logout();
    User getCurrentUser();
}

// RENAME: FirebaseAuthRepository → AuthRepositoryImpl
public class AuthRepositoryImpl implements AuthRepository {
    // ...existing code...
}
```

#### Step 2.3: Đổi tên files
```
ContinueWithGoogle.java → GoogleSignInHelper.java
```

---

### **Phase 3: Tách Workspace Feature (2 ngày)**

#### Step 3.1: Tạo `feature/workspace/`
```
✅ Tạo: feature/workspace/data/api/
✅ Tạo: feature/workspace/data/dto/
✅ Tạo: feature/workspace/data/repository/
✅ Tạo: feature/workspace/model/
✅ Tạo: feature/workspace/ui/adapter/
```

#### Step 3.2: Di chuyển files
```
network/api/WorkspaceApiService.java       → feature/workspace/data/api/WorkspaceApi.java
model/Workspace.java                       → feature/workspace/model/Workspace.java
adapter/WorkspaceAdapter.java              → feature/workspace/ui/adapter/WorkspaceAdapter.java
feature/home/ui/WorkspaceActivity.java     → feature/workspace/ui/WorkspaceActivity.java
```

#### Step 3.3: Tạo Repository cho Workspace
```java
// NEW: feature/workspace/data/repository/WorkspaceRepository.java
public interface WorkspaceRepository {
    List<Workspace> getWorkspaces();
    Workspace getWorkspaceById(String id);
    Workspace createWorkspace(Workspace workspace);
    void deleteWorkspace(String id);
}

// NEW: feature/workspace/data/repository/WorkspaceRepositoryImpl.java
public class WorkspaceRepositoryImpl implements WorkspaceRepository {
    private final WorkspaceApi api;
    
    // Implementation...
}
```

#### Step 3.4: Update WorkspaceActivity
```java
// feature/workspace/ui/WorkspaceActivity.java
public class WorkspaceActivity extends AppCompatActivity {
    private WorkspaceRepository repository; // ← Thay vì gọi API trực tiếp
    private WorkspaceAdapter adapter;
    
    // ...existing code...
}
```

---

### **Phase 4: Tách Board Feature (2 ngày)**

#### Step 4.1: Tạo `feature/board/`
```
✅ Tạo: feature/board/data/api/
✅ Tạo: feature/board/data/dto/
✅ Tạo: feature/board/data/repository/
✅ Tạo: feature/board/model/
✅ Tạo: feature/board/ui/create/
✅ Tạo: feature/board/ui/detail/
✅ Tạo: feature/board/ui/adapter/
```

#### Step 4.2: Di chuyển files
```
feature/home/ui/NewBoard.java                          → feature/board/ui/create/NewBoardActivity.java
feature/home/ui/BoardDetail/MainBoardDetail.java       → feature/board/ui/detail/BoardDetailActivity.java
feature/home/ui/BoardDetail/BoardPage.java             → feature/board/model/BoardPage.java
feature/home/ui/BoardDetail/BoardPageAdapter.java      → feature/board/ui/detail/adapter/BoardPageAdapter.java
```

#### Step 4.3: Tạo API & Repository
```java
// NEW: feature/board/data/api/BoardApi.java
public interface BoardApi {
    @GET("boards")
    Call<List<BoardDto>> getBoards(@Query("workspaceId") String workspaceId);
    
    @GET("boards/{id}")
    Call<BoardDto> getBoardById(@Path("id") String id);
    
    @POST("boards")
    Call<BoardDto> createBoard(@Body CreateBoardRequest request);
}

// NEW: feature/board/data/repository/BoardRepository.java + BoardRepositoryImpl.java
```

---

### **Phase 5: Tách Inbox Feature (1 ngày)**

#### Step 5.1: Tạo `feature/inbox/`
```
✅ Tạo: feature/inbox/data/api/
✅ Tạo: feature/inbox/data/dto/
✅ Tạo: feature/inbox/data/repository/
✅ Tạo: feature/inbox/model/
✅ Tạo: feature/inbox/ui/fragment/
✅ Tạo: feature/inbox/ui/adapter/
```

#### Step 5.2: Di chuyển files
```
feature/home/ui/InboxActivity.java              → feature/inbox/ui/InboxActivity.java
feature/home/ui/Inbox/InboxListFrm.java         → feature/inbox/ui/fragment/InboxListFragment.java
feature/home/ui/Inbox/InboxAdapter.java         → feature/inbox/ui/adapter/InboxAdapter.java
```

#### Step 5.3: Tạo API & Repository
```java
// NEW: feature/inbox/data/api/InboxApi.java
// NEW: feature/inbox/data/repository/InboxRepository.java
// NEW: feature/inbox/model/InboxItem.java
```

---

### **Phase 6: Tách Activity Timeline Feature (1 ngày)**

#### Step 6.1: Tạo `feature/activity/` (hoặc `feature/timeline/`)
```
✅ Tạo: feature/activity/data/api/
✅ Tạo: feature/activity/data/dto/
✅ Tạo: feature/activity/data/repository/
✅ Tạo: feature/activity/model/
✅ Tạo: feature/activity/ui/fragment/
✅ Tạo: feature/activity/ui/adapter/
```

#### Step 6.2: Di chuyển files
```
feature/home/ui/ActivityActivity.java                → feature/activity/ui/ActivityTimelineActivity.java
feature/home/ui/Activity/ListFragment.java           → feature/activity/ui/fragment/ActivityListFragment.java
feature/home/ui/Activity/ListFrmAdapter.java         → feature/activity/ui/adapter/ActivityListAdapter.java
```

---

### **Phase 7: Tách Account Feature (1 ngày)**

#### Step 7.1: Tạo `feature/account/`
```
✅ Tạo: feature/account/data/api/
✅ Tạo: feature/account/data/dto/
✅ Tạo: feature/account/data/repository/
✅ Tạo: feature/account/model/
✅ Tạo: feature/account/ui/
```

#### Step 7.2: Di chuyển files
```
feature/home/ui/AccountActivity.java → feature/account/ui/AccountActivity.java
```

---

### **Phase 8: Cleanup (1 ngày)**

#### Step 8.1: Xóa packages cũ (rỗng)
```
❌ Xóa: adapter/ (đã di chuyển hết)
❌ Xóa: model/ (đã di chuyển hết)
❌ Xóa: network/ (đã di chuyển vào core/)
❌ Xóa: sync/ (đã di chuyển vào core/)
❌ Xóa: auth/remote/ (đã di chuyển vào auth/data/)
❌ Xóa: auth/storage/ (đã di chuyển vào core/)
❌ Xóa: feature/home/ui/Inbox/ (đã tách thành feature riêng)
❌ Xóa: feature/home/ui/Activity/ (đã tách thành feature riêng)
❌ Xóa: feature/home/ui/BoardDetail/ (đã tách thành feature riêng)
```

#### Step 8.2: Update imports trong toàn bộ project
```
// Dùng Find & Replace trong IDE
com.example.tralalero.network.ApiClient 
→ com.example.tralalero.core.network.ApiClient

com.example.tralalero.adapter.WorkspaceAdapter 
→ com.example.tralalero.feature.workspace.ui.adapter.WorkspaceAdapter

// ... và các imports khác
```

#### Step 8.3: Test toàn bộ app
```
✅ Test login/logout
✅ Test workspace CRUD
✅ Test board CRUD
✅ Test inbox
✅ Test activity timeline
✅ Test account
```

---

## 📊 So sánh Before/After

### Before (Hiện tại)
```
com.example.tralalero/
├── MainActivity.java
├── App/App.java
├── adapter/WorkspaceAdapter.java            ← Adapter riêng lẻ
├── model/Workspace.java                     ← Model riêng lẻ
├── network/ApiClient.java                   ← Network config
├── network/api/WorkspaceApiService.java     ← API riêng lẻ
├── auth/                                    ← Auth module OK
├── feature/auth/ui/                         ← Auth UI
├── feature/home/ui/                         ← Tất cả features trộn vào home
└── sync/StartupSyncWorker.java              ← Sync riêng lẻ

Vấn đề:
❌ Adapter, Model, API nằm rải rác
❌ Tất cả features UI nằm trong home/
❌ Khó tìm kiếm và maintain
❌ Cross-package dependencies
```

### After (Đề xuất)
```
com.example.tralalero/
├── MainActivity.java
├── app/PlanTrackerApp.java
│
├── core/                                    ← Shared components
│   ├── network/ApiClient.java
│   ├── storage/TokenManager.java
│   └── sync/StartupSyncWorker.java
│
└── feature/                                 ← Feature-based organization
    ├── auth/
    │   ├── data/                            ← Data layer
    │   └── ui/                              ← UI layer
    ├── workspace/
    │   ├── data/api/, dto/, repository/
    │   ├── model/Workspace.java
    │   └── ui/WorkspaceActivity.java, adapter/
    ├── board/
    │   ├── data/
    │   ├── model/
    │   └── ui/create/, detail/
    ├── inbox/
    │   ├── data/
    │   ├── model/
    │   └── ui/
    ├── activity/
    │   ├── data/
    │   ├── model/
    │   └── ui/
    ├── account/
    │   ├── data/
    │   ├── model/
    │   └── ui/
    └── home/
        ├── data/
        ├── model/
        └── ui/HomeActivity.java

Lợi ích:
✅ Mỗi feature tự chứa: data + model + ui
✅ Dễ tìm kiếm (tất cả workspace code ở 1 chỗ)
✅ Dễ test (mock repository)
✅ Dễ scale (thêm feature mới = copy structure)
✅ Core components tách biệt
```

---

## 🎯 Checklist cho từng Feature

Khi tách một feature mới, đảm bảo có đủ các thành phần:

```
feature/[tên-feature]/
├── data/
│   ├── api/
│   │   └── [Feature]Api.java              ✅ Retrofit interface
│   ├── dto/
│   │   └── [Feature]Dto.java              ✅ API response model
│   └── repository/
│       ├── [Feature]Repository.java       ✅ Interface
│       └── [Feature]RepositoryImpl.java   ✅ Implementation
├── model/
│   └── [Feature].java                     ✅ Domain model
└── ui/
    ├── [Feature]Activity.java             ✅ Main screen
    ├── fragment/                          ⚠️ (if needed)
    │   └── [Feature]Fragment.java
    └── adapter/                           ⚠️ (if needed)
        └── [Feature]Adapter.java
```

---

## 🔧 Tools & Scripts hỗ trợ Migration

### Script 1: Tạo feature structure
```bash
# create_feature.sh (Git Bash on Windows)
FEATURE_NAME=$1
BASE_PACKAGE="app/src/main/java/com/example/tralalero/feature"

mkdir -p "$BASE_PACKAGE/$FEATURE_NAME/data/api"
mkdir -p "$BASE_PACKAGE/$FEATURE_NAME/data/dto"
mkdir -p "$BASE_PACKAGE/$FEATURE_NAME/data/repository"
mkdir -p "$BASE_PACKAGE/$FEATURE_NAME/model"
mkdir -p "$BASE_PACKAGE/$FEATURE_NAME/ui/adapter"

echo "✅ Created structure for $FEATURE_NAME feature"
```

### Script 2: Find & Replace helper
```
Trong Android Studio:
1. Ctrl+Shift+R (Replace in Path)
2. Scope: Project Files
3. File mask: *.java

Examples:
- import com.example.tralalero.adapter.WorkspaceAdapter
  → import com.example.tralalero.feature.workspace.ui.adapter.WorkspaceAdapter

- import com.example.tralalero.network.ApiClient
  → import com.example.tralalero.core.network.ApiClient
```

---

## 📝 Naming Conventions

### Package Names
```
✅ feature.workspace.data.api        (lowercase, descriptive)
✅ feature.board.ui.detail            (lowercase, nested OK)
✅ core.network.interceptor           (lowercase)

❌ feature.home.ui.Inbox              (capitalized sub-package)
❌ feature.home.ui.Activity           (conflicts with Android class name)
```

### Class Names
```
✅ WorkspaceAdapter                   (clear purpose)
✅ BoardDetailActivity                (descriptive)
✅ InboxListFragment                  (clear)
✅ GoogleSignInHelper                 (helper suffix)

❌ NewBoard                           (should be NewBoardActivity)
❌ MainBoardDetail                    (should be BoardDetailActivity)
❌ InboxListFrm                       (abbreviation)
❌ ContinueWithGoogle                 (verb, not noun)
```

### File Organization
```
✅ feature/workspace/ui/adapter/WorkspaceAdapter.java
✅ feature/board/ui/detail/adapter/BoardPageAdapter.java
✅ feature/inbox/ui/fragment/InboxListFragment.java

❌ adapter/WorkspaceAdapter.java      (not in feature)
❌ feature/home/ui/Inbox/InboxAdapter.java (capitalized sub-package)
```

---

## 🚦 Timeline Tổng thể

| Phase | Task | Duration | Difficulty | Priority |
|-------|------|----------|------------|----------|
| 1 | Core reorganization | 1 ngày | Dễ | 🔴 Cao |
| 2 | Refactor Auth | 1 ngày | Trung bình | 🔴 Cao |
| 3 | Tách Workspace | 2 ngày | Trung bình | 🟡 Trung bình |
| 4 | Tách Board | 2 ngày | Trung bình | 🟡 Trung bình |
| 5 | Tách Inbox | 1 ngày | Dễ | 🟢 Thấp |
| 6 | Tách Activity | 1 ngày | Dễ | 🟢 Thấp |
| 7 | Tách Account | 1 ngày | Dễ | 🟢 Thấp |
| 8 | Cleanup & Test | 1 ngày | Dễ | 🔴 Cao |

**Tổng cộng: ~10 ngày làm việc**

---

## 💡 Tips & Best Practices

### 1. **Làm từng phase một**
- ✅ Commit sau mỗi phase
- ✅ Test sau mỗi phase
- ✅ Không refactor quá nhiều cùng lúc

### 2. **Use IDE Refactoring Tools**
```
Android Studio:
- Shift+F6: Rename
- F6: Move
- Ctrl+Alt+Shift+T: Refactor menu
- Ctrl+Shift+R: Replace in path
```

### 3. **Keep app running**
- ✅ App phải build được sau mỗi commit
- ✅ Không break existing features
- ✅ Test thoroughly

### 4. **Document changes**
```
Git commit messages:
✅ "refactor: move core network components to core/ package"
✅ "refactor(workspace): extract workspace feature from home"
✅ "refactor: rename App/ to app/ following convention"

❌ "refactor stuff"
❌ "WIP"
```

### 5. **Backward compatibility**
- ⚠️ Nếu có external dependencies (other modules, libraries)
- ⚠️ Cân nhắc giữ lại old classes với `@Deprecated` annotation

---

## ❓ FAQ

### Q1: Có nhất thiết phải refactor không?
**A:** Không. Nếu team đang:
- Gấp deadline
- Thiếu người
- App đang stable và production

→ **Có thể BỎ QUA** refactor này. Chỉ làm khi:
- Có thời gian
- Team đồng ý
- Muốn improve code quality

### Q2: Có thể làm từng feature một không?
**A:** CÓ! Đề xuất:
1. Phase 1 (Core) - Làm trước
2. Chọn 1 feature nhỏ (VD: Account) - Làm pilot
3. Nếu OK → Làm tiếp các feature khác
4. Nếu không OK → Rollback và giữ nguyên

### Q3: Domain layer có bắt buộc không?
**A:** KHÔNG. Đối với app nhỏ/vừa:
- ✅ Có: `data/` + `ui/` + `model/` là đủ
- ⚠️ Bỏ qua: `domain/` layer nếu business logic đơn giản

### Q4: Repository pattern có cần thiết không?
**A:** CÓ! Vì:
- ✅ Dễ test (mock repository)
- ✅ Dễ switch data source (API → local → cache)
- ✅ Tách UI khỏi API logic
- ✅ Best practice của Android

### Q5: Có nên đổi tên package root không?
**A:** TÙY:
- ✅ Nếu app mới, chưa deploy → Nên đổi
- ❌ Nếu app đã deploy, có users → KHÔNG NÊN (gây breaking changes)

---

## 🎓 References

1. **Android Package Structure**: https://developer.android.com/topic/architecture
2. **Repository Pattern**: https://developer.android.com/topic/architecture/data-layer
3. **Package by Feature**: https://phauer.com/2020/package-by-feature/

---

**Tạo bởi:** GitHub Copilot  
**Ngày:** 2025-01-05  
**Version:** 1.0  
**Status:** ✅ Ready for Review

