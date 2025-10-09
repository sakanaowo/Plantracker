# Cấu trúc Package Hiện tại - Plantracker

## 📦 Package Structure Overview

Dựa trên phân tích code hiện tại, đây là cấu trúc package đầy đủ của project:

```
com.example.tralalero/
│
├── 📱 MainActivity.java                                    # Entry point chính
│
├── 🏢 App/                                                 # Application layer
│   └── App.java                                            # Custom Application class
│
├── 📡 network/                                             # Networking layer (shared)
│   ├── ApiClient.java                                      # Retrofit client configuration
│   └── api/
│       └── WorkspaceApiService.java                        # Workspace API endpoints
│
├── 🔐 auth/                                                # Authentication module
│   ├── storage/
│   │   └── TokenManager.java                              # Token persistence
│   ├── repository/
│   │   └── FirebaseAuthRepository.java                    # Auth repository
│   └── remote/
│       ├── AuthApi.java                                    # Auth API endpoints
│       ├── PublicAuthApi.java                             # Public auth endpoints
│       ├── AuthManager.java                               # Auth business logic
│       ├── FirebaseAuthenticator.java                     # OkHttp authenticator
│       ├── FirebaseInterceptor.java                       # OkHttp interceptor
│       ├── FirebaseAuthExample.java                       # Example/helper
│       └── dto/                                           # Data Transfer Objects
│           ├── UserDto.java
│           ├── LoginRequest.java
│           ├── LoginResponse.java
│           ├── FirebaseAuthDto.java
│           └── FirebaseAuthResponse.java
│
├── 🎯 feature/                                             # Feature modules
│   ├── auth/                                              # Auth UI features
│   │   └── ui/
│   │       ├── login/
│   │       │   ├── LoginActivity.java
│   │       │   └── ContinueWithGoogle.java
│   │       ├── signup/
│   │       │   └── SignupActivity.java
│   │       └── forgot/
│   │           └── ForgotPasswordActivity.java
│   │
│   └── home/                                              # Home/Main features
│       └── ui/
│           ├── HomeActivity.java                          # Dashboard
│           ├── WorkspaceActivity.java                     # Workspace list
│           ├── InboxActivity.java                         # Inbox
│           ├── ActivityActivity.java                      # Activity timeline
│           ├── AccountActivity.java                       # User account
│           ├── NewBoard.java                              # Create board
│           │
│           ├── Inbox/                                     # Inbox sub-feature
│           │   ├── InboxListFrm.java                      # Inbox fragment
│           │   └── InboxAdapter.java                      # Inbox adapter
│           │
│           ├── Activity/                                  # Activity timeline sub-feature
│           │   ├── ListFragment.java
│           │   └── ListFrmAdapter.java
│           │
│           └── BoardDetail/                               # Board detail sub-feature
│               ├── MainBoardDetail.java
│               ├── BoardPage.java
│               └── BoardPageAdapter.java
│
├── 🎨 adapter/                                             # Shared adapters
│   └── WorkspaceAdapter.java                              # RecyclerView adapter for workspaces
│
├── 📊 model/                                               # Shared models
│   └── Workspace.java                                      # Workspace domain model
│
└── 🔄 sync/                                                # Background sync
    └── StartupSyncWorker.java                             # WorkManager worker
```

---

## 📋 Package Breakdown chi tiết

### 1. **Root Package** - `com.example.tralalero`

#### Main Entry Points
```
com.example.tralalero.MainActivity
com.example.tralalero.App.App
```

---

### 2. **Network Package** - `com.example.tralalero.network`

#### Purpose
Xử lý tất cả các API calls và network configuration

#### Classes
```
com.example.tralalero.network.ApiClient
    └── Cấu hình Retrofit, OkHttp, Interceptors

com.example.tralalero.network.api.WorkspaceApiService
    └── API endpoints cho Workspace
```

#### Vấn đề
- ⚠️ Chỉ có WorkspaceApiService, thiếu các API khác (Board, Inbox, Activity...)
- ⚠️ Network config nên tách riêng khỏi business logic

---

### 3. **Auth Package** - `com.example.tralalero.auth`

#### Structure
```
com.example.tralalero.auth
├── storage
│   └── TokenManager                          # Quản lý JWT token
├── repository
│   └── FirebaseAuthRepository                # Repository pattern
└── remote
    ├── AuthApi                               # Authenticated API calls
    ├── PublicAuthApi                         # Non-authenticated API calls
    ├── AuthManager                           # Auth logic coordinator
    ├── FirebaseAuthenticator                 # Token refresh handler
    ├── FirebaseInterceptor                   # Add auth headers
    ├── FirebaseAuthExample                   # Helper/Example
    └── dto
        ├── UserDto                           # User data from API
        ├── LoginRequest                      # Login payload
        ├── LoginResponse                     # Login response
        ├── FirebaseAuthDto                   # Firebase auth data
        └── FirebaseAuthResponse              # Firebase response
```

#### Điểm mạnh
✅ Tổ chức tốt theo layer (storage, repository, remote)
✅ DTO riêng biệt
✅ Có interceptor và authenticator

#### Vấn đề
⚠️ Thiếu domain layer (business models)
⚠️ Repository chưa có interface

---

### 4. **Feature Package** - `com.example.tralalero.feature`

#### 4.1 Auth Feature - `feature.auth.ui`
```
com.example.tralalero.feature.auth.ui
├── login
│   ├── LoginActivity                         # Login screen
│   └── ContinueWithGoogle                    # Google Sign-In handler
├── signup
│   └── SignupActivity                        # Signup screen
└── forgot
    └── ForgotPasswordActivity                # Password recovery
```

#### 4.2 Home Feature - `feature.home.ui`
```
com.example.tralalero.feature.home.ui
├── HomeActivity                              # Main dashboard
├── WorkspaceActivity                         # Workspace list screen
├── InboxActivity                             # Inbox screen
├── ActivityActivity                          # Activity timeline screen
├── AccountActivity                           # User profile screen
├── NewBoard                                  # Create board screen
│
├── Inbox/                                    # Inbox sub-package
│   ├── InboxListFrm                          # Fragment
│   └── InboxAdapter                          # Adapter
│
├── Activity/                                 # Activity timeline sub-package
│   ├── ListFragment                          # Fragment
│   └── ListFrmAdapter                        # Adapter
│
└── BoardDetail/                              # Board detail sub-package
    ├── MainBoardDetail                       # Detail screen
    ├── BoardPage                             # Board page/tab
    └── BoardPageAdapter                      # ViewPager adapter
```

#### Vấn đề
❌ Tất cả logic nằm trong UI layer
❌ Không có ViewModel (nếu dùng MVVM)
❌ Thiếu data layer và domain layer
❌ Naming không nhất quán (Activity vs Frm vs Fragment)
❌ Sub-package với chữ hoa (Inbox/, Activity/, BoardDetail/)

---

### 5. **Adapter Package** - `com.example.tralalero.adapter`

```
com.example.tralalero.adapter.WorkspaceAdapter
    └── RecyclerView adapter để hiển thị danh sách workspace
```

#### Vấn đề
❌ Adapter đứng riêng, không thuộc feature cụ thể
❌ Các adapter khác lại nằm trong feature/home/ui/

---

### 6. **Model Package** - `com.example.tralalero.model`

```
com.example.tralalero.model.Workspace.Workspace
    └── Domain model cho Workspace
```

#### Vấn đề
❌ Model đứng riêng, không rõ thuộc feature nào
❌ Chỉ có 1 model, các entity khác ở đâu?
❌ Không phân biệt giữa DTO và Domain Model

---

### 7. **Sync Package** - `com.example.tralalero.sync`

```
com.example.tralalero.sync.StartupSyncWorker
    └── WorkManager worker cho background sync
```

#### Vấn đề
⚠️ Sync logic nên nằm trong core package
⚠️ Thiếu SyncManager để điều phối

---

## 🔍 Phân tích Dependencies

### Current Dependencies Flow
```
MainActivity
    ↓
HomeActivity/WorkspaceActivity
    ↓
WorkspaceAdapter (from adapter package) ← ⚠️ Cross-package dependency
    ↓
Workspace (from model package) ← ⚠️ Cross-package dependency
    ↓
WorkspaceApiService (from network package)
    ↓
ApiClient (from network package)
    ↓
FirebaseInterceptor (from auth package)
```

### Vấn đề về Dependencies
1. **Circular dependencies risk**: adapter ↔ model ↔ network
2. **Tight coupling**: UI trực tiếp gọi API service
3. **Hard to test**: Không có abstraction layer
4. **Inconsistent**: Auth có repository, nhưng Workspace không có

---

## 📊 Package Statistics

```
Total Packages: 16
Total Java Files: 39

By Category:
├── UI Layer: 19 files (48.7%)
├── Data Layer: 12 files (30.8%)
├── Domain Layer: 1 file (2.6%)  ← ⚠️ Quá ít!
├── Network Layer: 2 files (5.1%)
├── Adapter: 5 files (12.8%)
└── Other: 0 files
```

---

## ⚠️ Các vấn đề chính

### 1. **Architecture Inconsistency**
- Auth có: storage + repository + remote + dto ✅
- Workspace có: model + adapter + api ⚠️
- Other features: chỉ có UI ❌

### 2. **Package Naming Issues**
```
❌ com.example.tralalero.App.App            # Folder viết hoa
❌ feature.home.ui.Inbox.InboxAdapter       # Sub-package viết hoa
❌ feature.home.ui.Activity.ListFragment    # Activity trùng tên với Android class
```

### 3. **Separation of Concerns**
```
❌ UI Activities chứa business logic
❌ Không có ViewModel layer
❌ Data models trộn với DTOs
❌ Adapters rải rác khắp nơi
```

### 4. **Missing Layers**
```
❌ Không có UseCase layer
❌ Không có Repository cho Workspace/Board/Inbox
❌ Không có local data source (caching)
❌ Không có Domain Models riêng
```

---

## 🎯 Mapping hiện tại → Đề xuất

### Auth Feature
```
HIỆN TẠI:
auth/remote/AuthApi.java
auth/repository/FirebaseAuthRepository.java
feature/auth/ui/login/LoginActivity.java

ĐỀ XUẤT:
feature/auth/data/remote/AuthApi.java
feature/auth/data/repository/AuthRepositoryImpl.java
feature/auth/domain/usecase/LoginUseCase.java
feature/auth/presentation/login/LoginActivity.java
```

### Workspace Feature
```
HIỆN TẠI:
model/Workspace.java
adapter/WorkspaceAdapter.java
network/api/WorkspaceApiService.java
feature/home/ui/WorkspaceActivity.java

ĐỀ XUẤT:
feature/workspace/domain/model/Workspace.java
feature/workspace/data/remote/dto/WorkspaceDto.java
feature/workspace/data/remote/WorkspaceApiService.java
feature/workspace/data/repository/WorkspaceRepositoryImpl.java
feature/workspace/domain/usecase/GetWorkspacesUseCase.java
feature/workspace/presentation/WorkspaceActivity.java
feature/workspace/presentation/adapter/WorkspaceAdapter.java
```

### Core Components
```
HIỆN TẠI:
network/ApiClient.java
auth/storage/TokenManager.java
sync/StartupSyncWorker.java

ĐỀ XUẤT:
core/network/ApiClient.java
core/storage/TokenManager.java
core/sync/StartupSyncWorker.java
```

---

## 📝 Full Package Tree (As-Is)

```
com.example.tralalero/
│
├── MainActivity.java
│
├── App/
│   └── App.java
│
├── adapter/
│   └── WorkspaceAdapter.java
│
├── auth/
│   ├── remote/
│   │   ├── AuthApi.java
│   │   ├── AuthManager.java
│   │   ├── FirebaseAuthExample.java
│   │   ├── FirebaseAuthenticator.java
│   │   ├── FirebaseInterceptor.java
│   │   ├── PublicAuthApi.java
│   │   └── dto/
│   │       ├── FirebaseAuthDto.java
│   │       ├── FirebaseAuthResponse.java
│   │       ├── LoginRequest.java
│   │       ├── LoginResponse.java
│   │       └── UserDto.java
│   ├── repository/
│   │   └── FirebaseAuthRepository.java
│   └── storage/
│       └── TokenManager.java
│
├── feature/
│   ├── auth/
│   │   └── ui/
│   │       ├── forgot/
│   │       │   └── ForgotPasswordActivity.java
│   │       ├── login/
│   │       │   ├── ContinueWithGoogle.java
│   │       │   └── LoginActivity.java
│   │       └── signup/
│   │           └── SignupActivity.java
│   └── home/
│       └── ui/
│           ├── AccountActivity.java
│           ├── Activity/
│           │   ├── ListFragment.java
│           │   └── ListFrmAdapter.java
│           ├── ActivityActivity.java
│           ├── BoardDetail/
│           │   ├── BoardPage.java
│           │   ├── BoardPageAdapter.java
│           │   └── MainBoardDetail.java
│           ├── HomeActivity.java
│           ├── Inbox/
│           │   ├── InboxAdapter.java
│           │   └── InboxListFrm.java
│           ├── InboxActivity.java
│           ├── NewBoard.java
│           └── WorkspaceActivity.java
│
├── model/
│   └── Workspace.java
│
├── network/
│   ├── ApiClient.java
│   └── api/
│       └── WorkspaceApiService.java
│
└── sync/
    └── StartupSyncWorker.java
```

---

## 🚀 Next Steps

1. **Review** cấu trúc hiện tại với team
2. **Quyết định** có refactor hay không
3. **Nếu refactor**: Xem `Proposed_Project_Structure.md` để biết cấu trúc đề xuất
4. **Nếu giữ nguyên**: Ít nhất cần fix các issues:
   - Đổi tên `App/` → `app/`
   - Di chuyển `adapter/WorkspaceAdapter` vào feature tương ứng
   - Tạo repository cho Workspace
   - Tách UI logic ra khỏi Activities

---

**Tạo bởi:** GitHub Copilot  
**Ngày:** 05/10/2025  
**Version:** 1.0

