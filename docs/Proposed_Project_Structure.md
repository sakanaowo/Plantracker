# Đề xuất Cấu trúc Thư mục Mới cho Plantracker

## 📋 Tổng quan

Dự án hiện tại đang sử dụng một cấu trúc lai ghép giữa feature-based và layer-based architecture. Đề xuất này sẽ tổ chức lại theo **Clean Architecture + Feature-First Pattern** để dễ bảo trì và mở rộng hơn.

---

## 🏗️ Cấu trúc hiện tại (Vấn đề)

```
com.example.tralalero/
├── adapter/                    ❌ Adapter nằm rời, không theo feature
├── App/                        ❌ Tên folder không chuẩn (viết hoa)
├── auth/                       ✅ Module auth tốt
│   ├── remote/
│   ├── repository/
│   └── storage/
├── feature/                    ⚠️ Chỉ có UI, thiếu data/domain layer
│   ├── auth/ui/
│   └── home/ui/
├── model/                      ❌ Model nằm rời, không rõ thuộc feature nào
├── network/                    ⚠️ Network layer chung, nhưng cần tách rõ hơn
└── sync/                       ❌ Sync logic nằm rời

**Vấn đề chính:**
- Trộn lẫn giữa feature-based và layer-based
- Adapter và Model nằm rời không thuộc feature cụ thể
- Thiếu separation of concerns rõ ràng
- Khó test và maintain khi project lớn
```

---

## ✅ Cấu trúc đề xuất (Clean Architecture + Feature-First)

```
app/src/main/java/com/example/plantracker/
│
├── 📱 app/                                    # Application Layer
│   ├── Application.kt/java                    # Custom Application class
│   ├── MainActivity.java                      # Main entry point
│   └── di/                                    # Dependency Injection (nếu dùng Dagger/Hilt)
│       ├── AppModule.java
│       ├── NetworkModule.java
│       └── RepositoryModule.java
│
├── 🎯 core/                                   # Core/Shared Components
│   ├── network/                               # Networking infrastructure
│   │   ├── ApiClient.java
│   │   ├── interceptor/
│   │   │   ├── AuthInterceptor.java
│   │   │   ├── LoggingInterceptor.java
│   │   │   └── FirebaseInterceptor.java
│   │   └── authenticator/
│   │       └── FirebaseAuthenticator.java
│   │
│   ├── storage/                               # Local storage (SharedPreferences, Room, etc.)
│   │   ├── TokenManager.java
│   │   └── preferences/
│   │       └── UserPreferences.java
│   │
│   ├── util/                                  # Utility classes
│   │   ├── DateUtil.java
│   │   ├── ValidationUtil.java
│   │   └── Constants.java
│   │
│   ├── base/                                  # Base classes
│   │   ├── BaseActivity.java
│   │   ├── BaseFragment.java
│   │   ├── BaseAdapter.java
│   │   └── BaseViewModel.java (nếu dùng MVVM)
│   │
│   └── sync/                                  # Background sync logic
│       ├── SyncManager.java
│       └── worker/
│           └── StartupSyncWorker.java
│
├── 🔐 feature/                                # Feature Modules
│   │
│   ├── auth/                                  # Authentication Feature
│   │   ├── data/                              # Data Layer
│   │   │   ├── repository/
│   │   │   │   ├── AuthRepository.java
│   │   │   │   └── AuthRepositoryImpl.java
│   │   │   ├── remote/
│   │   │   │   ├── AuthApi.java
│   │   │   │   ├── PublicAuthApi.java
│   │   │   │   └── dto/                       # Data Transfer Objects
│   │   │   │       ├── LoginRequest.java
│   │   │   │       ├── LoginResponse.java
│   │   │   │       ├── FirebaseAuthDto.java
│   │   │   │       └── FirebaseAuthResponse.java
│   │   │   └── local/                         # Local data source (if needed)
│   │   │       └── AuthLocalDataSource.java
│   │   │
│   │   ├── domain/                            # Domain Layer (Business Logic)
│   │   │   ├── model/                         # Domain models (entities)
│   │   │   │   ├── User.java
│   │   │   │   └── AuthState.java
│   │   │   ├── usecase/                       # Use cases
│   │   │   │   ├── LoginUseCase.java
│   │   │   │   ├── GoogleSignInUseCase.java
│   │   │   │   ├── LogoutUseCase.java
│   │   │   │   └── ForgotPasswordUseCase.java
│   │   │   └── repository/                    # Repository interfaces
│   │   │       └── IAuthRepository.java
│   │   │
│   │   └── presentation/                      # Presentation Layer (UI)
│   │       ├── login/
│   │       │   ├── LoginActivity.java
│   │       │   ├── LoginViewModel.java        # (nếu dùng MVVM)
│   │       │   └── GoogleSignInHandler.java
│   │       ├── signup/
│   │       │   ├── SignupActivity.java
│   │       │   └── SignupViewModel.java
│   │       └── forgot/
│   │           ├── ForgotPasswordActivity.java
│   │           └── ForgotPasswordViewModel.java
│   │
│   ├── home/                                  # Home Feature
│   │   ├── data/
│   │   │   ├── repository/
│   │   │   │   └── HomeRepositoryImpl.java
│   │   │   ├── remote/
│   │   │   │   └── dto/
│   │   │   └── local/
│   │   │
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   ├── usecase/
│   │   │   │   └── GetUserDashboardUseCase.java
│   │   │   └── repository/
│   │   │       └── IHomeRepository.java
│   │   │
│   │   └── presentation/
│   │       ├── HomeActivity.java
│   │       ├── HomeViewModel.java
│   │       └── adapter/                       # Adapters specific to home
│   │
│   ├── workspace/                             # Workspace Feature
│   │   ├── data/
│   │   │   ├── repository/
│   │   │   │   └── WorkspaceRepositoryImpl.java
│   │   │   ├── remote/
│   │   │   │   ├── WorkspaceApiService.java
│   │   │   │   └── dto/
│   │   │   │       └── WorkspaceDto.java
│   │   │   └── local/
│   │   │
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   └── Workspace.java             # Domain model
│   │   │   ├── usecase/
│   │   │   │   ├── GetWorkspacesUseCase.java
│   │   │   │   ├── CreateWorkspaceUseCase.java
│   │   │   │   └── DeleteWorkspaceUseCase.java
│   │   │   └── repository/
│   │   │       └── IWorkspaceRepository.java
│   │   │
│   │   └── presentation/
│   │       ├── WorkspaceActivity.java
│   │       ├── WorkspaceViewModel.java
│   │       └── adapter/
│   │           └── WorkspaceAdapter.java      # Moved from root adapter/
│   │
│   ├── board/                                 # Board Feature
│   │   ├── data/
│   │   │   ├── repository/
│   │   │   ├── remote/
│   │   │   │   └── dto/
│   │   │   └── local/
│   │   │
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   └── Board.java
│   │   │   ├── usecase/
│   │   │   │   ├── GetBoardsUseCase.java
│   │   │   │   └── CreateBoardUseCase.java
│   │   │   └── repository/
│   │   │       └── IBoardRepository.java
│   │   │
│   │   └── presentation/
│   │       ├── list/
│   │       │   └── NewBoard.java
│   │       ├── detail/
│   │       │   ├── MainBoardDetail.java
│   │       │   ├── BoardPage.java
│   │       │   └── BoardPageAdapter.java
│   │       └── adapter/
│   │
│   ├── inbox/                                 # Inbox Feature
│   │   ├── data/
│   │   ├── domain/
│   │   └── presentation/
│   │       ├── InboxActivity.java
│   │       ├── InboxListFrm.java
│   │       └── adapter/
│   │           └── InboxAdapter.java
│   │
│   ├── activity/                              # Activity/Timeline Feature
│   │   ├── data/
│   │   ├── domain/
│   │   └── presentation/
│   │       ├── ActivityActivity.java
│   │       ├── ListFragment.java
│   │       └── adapter/
│   │           └── ListFrmAdapter.java
│   │
│   └── account/                               # Account/Profile Feature
│       ├── data/
│       ├── domain/
│       └── presentation/
│           ├── AccountActivity.java
│           └── AccountViewModel.java
│
└── 📦 res/                                    # Resources
    ├── layout/
    │   ├── activity_login.xml
    │   ├── activity_home.xml
    │   ├── fragment_inbox_list.xml
    │   └── item_workspace.xml
    ├── values/
    │   ├── strings.xml
    │   ├── colors.xml
    │   └── themes.xml
    └── drawable/
```

---

## 🔄 Migration Plan (Kế hoạch chuyển đổi)

### Phase 1: Chuẩn bị (1-2 ngày)
1. ✅ Backup toàn bộ project
2. ✅ Tạo branch mới: `refactor/clean-architecture`
3. ✅ Setup các package cơ bản

### Phase 2: Refactor Core Layer (2-3 ngày)
1. Di chuyển `network/` → `core/network/`
2. Di chuyển `auth/storage/TokenManager` → `core/storage/TokenManager`
3. Di chuyển `sync/` → `core/sync/`
4. Tạo `core/base/` với các base classes
5. Tạo `core/util/` với utility classes

### Phase 3: Refactor Auth Feature (2-3 ngày)
1. Tạo `feature/auth/data/`
   - Di chuyển `auth/remote/` → `feature/auth/data/remote/`
   - Di chuyển `auth/repository/` → `feature/auth/data/repository/`
2. Tạo `feature/auth/domain/`
   - Tạo domain models
   - Tạo use cases
   - Tạo repository interfaces
3. Refactor `feature/auth/ui/` → `feature/auth/presentation/`

### Phase 4: Refactor Home & Other Features (3-4 ngày)
1. Tách `feature/home/` thành:
   - `feature/workspace/`
   - `feature/board/`
   - `feature/inbox/`
   - `feature/activity/`
   - `feature/account/`
2. Di chuyển adapters vào từng feature tương ứng
3. Di chuyển models vào domain layer của từng feature

### Phase 5: Testing & Cleanup (2-3 ngày)
1. Test từng feature đã refactor
2. Xóa các file/folder cũ không dùng
3. Update documentation
4. Code review & merge

---

## 📊 So sánh Before/After

### Before (Hiện tại)
```
❌ adapter/WorkspaceAdapter.java              # Không rõ thuộc feature nào
❌ model/Workspace.java                       # Data model hay domain model?
❌ auth/remote/dto/UserDto.java               # OK nhưng thiếu domain layer
⚠️ feature/home/ui/WorkspaceActivity.java    # UI + Business logic trộn lẫn
```

### After (Đề xuất)
```
✅ feature/workspace/presentation/adapter/WorkspaceAdapter.java
✅ feature/workspace/domain/model/Workspace.java
✅ feature/auth/data/remote/dto/UserDto.java
✅ feature/auth/domain/model/User.java
✅ feature/workspace/presentation/WorkspaceActivity.java
✅ feature/workspace/presentation/WorkspaceViewModel.java
✅ feature/workspace/domain/usecase/GetWorkspacesUseCase.java
```

---

## 🎯 Lợi ích của cấu trúc mới

### 1. **Separation of Concerns**
- Data layer: Xử lý API, database, cache
- Domain layer: Business logic thuần, không phụ thuộc framework
- Presentation layer: UI, ViewModel, Adapter

### 2. **Testability**
- Use cases dễ test (không phụ thuộc Android framework)
- Repository có thể mock dễ dàng
- ViewModel/Presenter test được độc lập

### 3. **Scalability**
- Thêm feature mới chỉ cần copy structure
- Nhiều dev có thể làm việc song song trên các feature khác nhau
- Dễ dàng tách thành module riêng sau này

### 4. **Maintainability**
- Code organization rõ ràng
- Dễ tìm kiếm và navigate
- Giảm merge conflicts

### 5. **Reusability**
- Core layer dùng chung cho tất cả features
- Base classes giảm code duplication
- Use cases có thể combine với nhau

---

## 🛠️ Best Practices áp dụng

### 1. **Naming Conventions**
```java
// Repository
interface IWorkspaceRepository {}
class WorkspaceRepositoryImpl implements IWorkspaceRepository {}

// Use Case
class GetWorkspacesUseCase {}
class CreateWorkspaceUseCase {}

// ViewModel
class WorkspaceViewModel extends BaseViewModel {}

// DTO vs Domain Model
class WorkspaceDto {}        // Data layer (API response)
class Workspace {}           // Domain layer (business entity)
```

### 2. **Dependency Rule**
```
Presentation → Domain ← Data
     ↓           ↓        ↓
  (UI/VM)   (UseCase)  (Repo)
```
- Domain layer KHÔNG phụ thuộc vào Data/Presentation
- Data/Presentation phụ thuộc vào Domain

### 3. **Package by Feature, not by Layer**
```
✅ feature/workspace/data/...
✅ feature/workspace/domain/...
✅ feature/workspace/presentation/...

❌ data/workspace/...
❌ domain/workspace/...
❌ presentation/workspace/...
```

---

## 📝 Action Items

### Immediate (Ngay lập tức)
- [ ] Review đề xuất này với team
- [ ] Quyết định có áp dụng full Clean Architecture hay simplified version
- [ ] Setup base structure trong branch mới

### Short-term (1-2 tuần)
- [ ] Refactor Core layer
- [ ] Refactor Auth feature (pilot)
- [ ] Test thoroughly

### Long-term (1 tháng)
- [ ] Refactor tất cả features
- [ ] Implement MVVM/MVI pattern (optional)
- [ ] Consider modularization (dynamic feature modules)

---

## 🔗 Tài liệu tham khảo

1. **Clean Architecture** by Robert C. Martin
2. **Android Architecture Guide**: https://developer.android.com/topic/architecture
3. **Google's Guide to App Architecture**: https://developer.android.com/jetpack/guide
4. **Package by Feature**: https://proandroiddev.com/package-by-feature-vs-package-by-layer-9c51e6b1c583

---

## 💡 Lưu ý quan trọng

1. **Không cần refactor tất cả ngay lập tức** - Làm từng feature một
2. **Giữ app chạy được** - Refactor incremental, test liên tục
3. **Team agreement** - Toàn team cần hiểu và đồng ý với structure mới
4. **Documentation** - Document lại decisions và patterns

---

**Tạo bởi:** GitHub Copilot  
**Ngày:** 05/10/2025  
**Version:** 1.0

