# 🏗️ CẤU TRÚC PROJECT PLANTRACKER - CHI TIẾT FILE

**Date**: October 20, 2025  
**Architecture**: Clean Architecture + MVVM + Room Database + Retrofit  
**Total Files**: 464 Java files  

---

## 📊 CẤU TRÚC CHI TIẾT FILE (464 Java Files)

```
Plantracker/
│
├── 📱 app/
│   ├── build.gradle.kts
│   ├── google-services.json
│   ├── proguard-rules.pro
│   │
│   └── src/
│       ├── 🧪 androidTest/java/com/example/tralalero/
│       │   └── ExampleInstrumentedTest.java
│       │
│       ├── 🧪 test/java/com/example/tralalero/      # Unit Tests (11 files)
│       │   ├── ExampleUnitTest.java
│       │   │
│       │   ├── data/mapper/
│       │   │   └── EntityMapperTest.java
│       │   │
│       │   └── domain/usecase/
│       │       ├── workspace/                        # (5 test files)
│       │       │   ├── CreateWorkspaceUseCaseTest.java
│       │       │   ├── GetWorkspaceByIdUseCaseTest.java
│       │       │   ├── GetWorkspaceBoardsUseCaseTest.java
│       │       │   ├── GetWorkspaceProjectsUseCaseTest.java
│       │       │   └── GetWorkspacesUseCaseTest.java
│       │       │
│       │       └── project/                         # (6 test files)
│       │           ├── CreateProjectUseCaseTest.java
│       │           ├── DeleteProjectUseCaseTest.java
│       │           ├── GetProjectByIdUseCaseTest.java
│       │           ├── SwitchBoardTypeUseCaseTest.java
│       │           ├── UpdateProjectKeyUseCaseTest.java
│       │           └── UpdateProjectUseCaseTest.java
│       │
│       └── main/java/com/example/tralalero/         # Main Source (452 files)
│           │
│           ├── 📄 MainActivity.java
│           │
│           ├── 🏠 App/
│           │   └── App.java                         # Application class
│           │
│           ├── 🔐 auth/
│           │   ├── storage/
│           │   │   └── TokenManager.java
│           │   │
│           │   ├── repository/
│           │   │   └── FirebaseAuthRepository.java
│           │   │
│           │   └── remote/                          # (7 files)
│           │       ├── AuthApi.java
│           │       ├── AuthManager.java
│           │       ├── FirebaseAuthExample.java
│           │       ├── FirebaseAuthenticator.java
│           │       ├── FirebaseInterceptor.java
│           │       ├── PublicAuthApi.java
│           │       │
│           │       └── dto/
│           │           ├── FirebaseAuthDto.java
│           │           └── UpdateProfileRequest.java
│           │
│           ├── ⚙️ core/
│           │   └── DependencyProvider.java          # Singleton DI
│           │
│           ├── 💾 data/                             # === DATA LAYER ===
│           │   │
│           │   ├── local/database/                  # Room Database (12 files)
│           │   │   ├── AppDatabase.java
│           │   │   │
│           │   │   ├── entity/                      # (5 entities)
│           │   │   │   ├── BoardEntity.java
│           │   │   │   ├── CacheMetadata.java
│           │   │   │   ├── ProjectEntity.java
│           │   │   │   ├── TaskEntity.java
│           │   │   │   └── WorkspaceEntity.java
│           │   │   │
│           │   │   ├── dao/                         # (5 DAOs)
│           │   │   │   ├── BoardDao.java
│           │   │   │   ├── CacheMetadataDao.java
│           │   │   │   ├── ProjectDao.java
│           │   │   │   ├── TaskDao.java
│           │   │   │   └── WorkspaceDao.java
│           │   │   │
│           │   │   └── converter/
│           │   │       └── DateConverter.java
│           │   │
│           │   ├── remote/
│           │   │   │
│           │   │   ├── api/                         # (10 API services)
│           │   │   │   ├── BoardApiService.java
│           │   │   │   ├── EventApiService.java
│           │   │   │   ├── HomeApiService.java
│           │   │   │   ├── LabelApiService.java
│           │   │   │   ├── NotificationApiService.java
│           │   │   │   ├── ProjectApiService.java
│           │   │   │   ├── SprintApiService.java
│           │   │   │   ├── TaskApiService.java
│           │   │   │   ├── TimerApiService.java
│           │   │   │   └── WorkspaceApiService.java
│           │   │   │
│           │   │   ├── dto/                         # Data Transfer Objects
│           │   │   │   │
│           │   │   │   ├── auth/                    # (4 DTOs)
│           │   │   │   │   ├── FirebaseAuthResponse.java
│           │   │   │   │   ├── LoginRequest.java
│           │   │   │   │   ├── LoginResponse.java
│           │   │   │   │   └── UserDto.java
│           │   │   │   │
│           │   │   │   ├── board/
│           │   │   │   │   └── BoardDTO.java
│           │   │   │   │
│           │   │   │   ├── event/
│           │   │   │   │   └── EventDTO.java
│           │   │   │   │
│           │   │   │   ├── label/
│           │   │   │   │   └── LabelDTO.java
│           │   │   │   │
│           │   │   │   ├── notification/
│           │   │   │   │   └── NotificationDTO.java
│           │   │   │   │
│           │   │   │   ├── project/
│           │   │   │   │   └── ProjectDTO.java
│           │   │   │   │
│           │   │   │   ├── sprint/
│           │   │   │   │   └── SprintDTO.java
│           │   │   │   │
│           │   │   │   ├── task/                    # (6 DTOs)
│           │   │   │   │   ├── AttachmentDTO.java
│           │   │   │   │   ├── CheckListDTO.java
│           │   │   │   │   ├── CheckListItemDTO.java
│           │   │   │   │   ├── TaskCommentDTO.java
│           │   │   │   │   ├── TaskDTO.java
│           │   │   │   │   └── TimeEntryDTO.java
│           │   │   │   │
│           │   │   │   └── workspace/               # (2 DTOs)
│           │   │   │       ├── MembershipDTO.java
│           │   │   │       └── WorkspaceDTO.java
│           │   │   │
│           │   │   └── mapper/                      # (2 mappers)
│           │   │       ├── TimeEntryMapper.java
│           │   │       └── WorkspaceMapper.java
│           │   │
│           │   ├── mapper/                          # Entity ↔ Domain (17 mappers)
│           │   │   ├── AttachmentMapper.java
│           │   │   ├── BoardEntityMapper.java
│           │   │   ├── BoardMapper.java
│           │   │   ├── ChecklistItemMapper.java
│           │   │   ├── ChecklistMapper.java
│           │   │   ├── DtoToEntityMapper.java
│           │   │   ├── EventMapper.java
│           │   │   ├── LabelMapper.java
│           │   │   ├── NotificationMapper.java
│           │   │   ├── ProjectEntityMapper.java
│           │   │   ├── ProjectMapper.java
│           │   │   ├── SprintMapper.java
│           │   │   ├── TaskCommentMapper.java
│           │   │   ├── TaskEntityMapper.java
│           │   │   ├── TaskMapper.java
│           │   │   ├── UserMapper.java
│           │   │   └── WorkspaceEntityMapper.java
│           │   │
│           │   └── repository/                      # Implementations (13 repos)
│           │       ├── AuthRepositoryImpl.java
│           │       ├── BoardRepositoryImpl.java
│           │       ├── EventRepositoryImpl.java
│           │       ├── LabelRepositoryImpl.java
│           │       ├── NotificationRepositoryImpl.java
│           │       ├── ProjectRepositoryImpl.java
│           │       ├── ProjectRepositoryImplWithCache.java ⚡
│           │       ├── SprintRepositoryImpl.java
│           │       ├── TaskRepositoryImpl.java
│           │       ├── TaskRepositoryImplWithCache.java ⚡
│           │       ├── TimeEntryRepositoryImpl.java
│           │       ├── WorkspaceRepositoryImpl.java
│           │       ├── WorkspaceRepositoryImplWithCache.java ⚡
│           │       │
│           │       └── test/
│           │           └── RepositoryTestHelper.java
│           │
│           ├── 🎯 domain/                           # === DOMAIN LAYER ===
│           │   │
│           │   ├── model/                           # Domain Models (16 models)
│           │   │   ├── Attachment.java
│           │   │   ├── Board.java
│           │   │   ├── Checklist.java
│           │   │   ├── ChecklistItem.java
│           │   │   ├── Event.java
│           │   │   ├── Label.java
│           │   │   ├── Membership.java
│           │   │   ├── Notification.java
│           │   │   ├── Project.java
│           │   │   ├── Role.java
│           │   │   ├── Sprint.java
│           │   │   ├── SprintState.java
│           │   │   ├── Task.java
│           │   │   ├── TaskComment.java
│           │   │   ├── TimeEntry.java
│           │   │   ├── User.java
│           │   │   └── Workspace.java
│           │   │
│           │   ├── repository/                      # Interfaces (10 interfaces)
│           │   │   ├── IAuthRepository.java
│           │   │   ├── IBoardRepository.java
│           │   │   ├── IEventRepository.java
│           │   │   ├── ILabelRepository.java
│           │   │   ├── INotificationRepository.java
│           │   │   ├── IProjectRepository.java
│           │   │   ├── ISprintRepository.java
│           │   │   ├── ITaskRepository.java
│           │   │   ├── ITimeEntryRepository.java
│           │   │   └── IWorkspaceRepository.java
│           │   │
│           │   └── usecase/                         # Business Logic (60+ use cases)
│           │       │
│           │       ├── auth/                        # (5 use cases)
│           │       │   ├── GetCurrentUserUseCase.java
│           │       │   ├── IsLoggedInUseCase.java
│           │       │   ├── LoginUseCase.java
│           │       │   ├── LogoutUseCase.java
│           │       │   └── SignupUseCase.java
│           │       │
│           │       ├── board/                       # (7 use cases)
│           │       │   ├── CreateBoardUseCase.java
│           │       │   ├── DeleteBoardUseCase.java
│           │       │   ├── GetBoardByIdUseCase.java
│           │       │   ├── GetBoardsByProjectUseCase.java
│           │       │   ├── GetBoardTasksUseCase.java
│           │       │   ├── ReorderBoardsUseCase.java
│           │       │   └── UpdateBoardUseCase.java
│           │       │
│           │       ├── label/                       # (5 use cases)
│           │       │   ├── CreateLabelUseCase.java
│           │       │   ├── DeleteLabelUseCase.java
│           │       │   ├── GetLabelByIdUseCase.java
│           │       │   ├── GetLabelsByWorkspaceUseCase.java
│           │       │   └── UpdateLabelUseCase.java
│           │       │
│           │       ├── notification/                # (9 use cases)
│           │       │   ├── DeleteAllNotificationsUseCase.java
│           │       │   ├── DeleteNotificationUseCase.java
│           │       │   ├── GetNotificationByIdUseCase.java
│           │       │   ├── GetNotificationCountUseCase.java
│           │       │   ├── GetNotificationsUseCase.java
│           │       │   ├── GetUnreadNotificationsUseCase.java
│           │       │   ├── MarkAllAsReadUseCase.java
│           │       │   ├── MarkAsReadUseCase.java
│           │       │   └── UpdateNotificationPreferencesUseCase.java
│           │       │
│           │       ├── project/                     # (6 use cases)
│           │       │   ├── CreateProjectUseCase.java
│           │       │   ├── DeleteProjectUseCase.java
│           │       │   ├── GetProjectByIdUseCase.java
│           │       │   ├── SwitchBoardTypeUseCase.java
│           │       │   ├── UpdateProjectKeyUseCase.java
│           │       │   └── UpdateProjectUseCase.java
│           │       │
│           │       ├── sprint/                      # (1 use case)
│           │       │   └── GetCurrentSprintUseCase.java
│           │       │
│           │       ├── task/                        # (18 use cases)
│           │       │   ├── AddAttachmentUseCase.java
│           │       │   ├── AddChecklistUseCase.java
│           │       │   ├── AddCommentUseCase.java
│           │       │   ├── AssignTaskUseCase.java
│           │       │   ├── CreateTaskUseCase.java
│           │       │   ├── DeleteTaskUseCase.java
│           │       │   ├── GetTaskAttachmentsUseCase.java
│           │       │   ├── GetTaskByIdUseCase.java
│           │       │   ├── GetTaskChecklistsUseCase.java
│           │       │   ├── GetTaskCommentsUseCase.java
│           │       │   ├── GetTasksByBoardUseCase.java
│           │       │   ├── GetTasksUseCase.java
│           │       │   ├── MoveTaskToBoardUseCase.java
│           │       │   ├── SearchTasksUseCase.java
│           │       │   ├── UnassignTaskUseCase.java
│           │       │   ├── UpdateChecklistItemUseCase.java
│           │       │   ├── UpdateTaskPositionUseCase.java
│           │       │   └── UpdateTaskUseCase.java
│           │       │
│           │       └── workspace/                   # (7 use cases)
│           │           ├── CreateWorkspaceUseCase.java
│           │           ├── DeleteWorkspaceUseCase.java
│           │           ├── GetWorkspaceBoardsUseCase.java
│           │           ├── GetWorkspaceByIdUseCase.java
│           │           ├── GetWorkspaceProjectsUseCase.java
│           │           ├── GetWorkspacesUseCase.java
│           │           └── UpdateWorkspaceUseCase.java
│           │
│           ├── 🎨 presentation/                     # === PRESENTATION LAYER ===
│           │   └── viewmodel/                       # (14 ViewModels + Factories)
│           │       ├── AuthViewModel.java
│           │       ├── AuthViewModelFactory.java
│           │       ├── BoardViewModel.java
│           │       ├── BoardViewModelFactory.java
│           │       ├── LabelViewModel.java
│           │       ├── LabelViewModelFactory.java
│           │       ├── NotificationViewModel.java
│           │       ├── NotificationViewModelFactory.java
│           │       ├── ProjectViewModel.java
│           │       ├── ProjectViewModelFactory.java
│           │       ├── TaskViewModel.java
│           │       ├── TaskViewModelFactory.java
│           │       ├── ViewModelFactoryProvider.java
│           │       ├── WorkspaceViewModel.java
│           │       └── WorkspaceViewModelFactory.java
│           │
│           ├── 📱 feature/                          # === UI LAYER ===
│           │   │
│           │   ├── home/ui/                         # (24 UI files)
│           │   │   ├── BaseActivity.java
│           │   │   ├── BottomNavigationFragment.java
│           │   │   ├── InboxActivity.java
│           │   │   ├── NewBoard.java
│           │   │   ├── SettingsActivity.java
│           │   │   ├── SettingsFragment.java
│           │   │   ├── TimerReceiver.java
│           │   │   ├── AccountActivity.java
│           │   │   ├── ActivityActivity.java
│           │   │   ├── ActivityTimer.java
│           │   │   │
│           │   │   ├── Home/
│           │   │   │   ├── HomeActivity.java
│           │   │   │   ├── ProjectActivity.java
│           │   │   │   ├── WorkspaceActivity.java
│           │   │   │   │
│           │   │   │   └── project/
│           │   │   │       ├── ListProject.java
│           │   │   │       ├── ListProjectAdapter.java
│           │   │   │       ├── ListProjectViewModel.java
│           │   │   │       ├── TaskAdapter.java
│           │   │   │       ├── TaskCreateEditBottomSheet.java
│           │   │   │       └── TaskDetailBottomSheet.java
│           │   │   │
│           │   │   ├── Inbox/
│           │   │   │   ├── InboxAdpater.java
│           │   │   │   └── InboxListFrm.java
│           │   │   │
│           │   │   └── Activity/
│           │   │       ├── ListFragment.java
│           │   │       └── ListFrmAdapter.java
│           │   │
│           │   ├── auth/ui/                         # (4 Auth UI files)
│           │   │   ├── login/
│           │   │   │   ├── ContinueWithGoogle.java
│           │   │   │   └── LoginActivity.java
│           │   │   │
│           │   │   ├── signup/
│           │   │   │   └── SignupActivity.java
│           │   │   │
│           │   │   └── forgot/
│           │   │       └── ForgotPasswordActivity.java
│           │   │
│           │   └── account/                         # (1 Account UI file)
│           │       └── AccountActivity.java
│           │
│           ├── 🔄 adapter/                          # RecyclerView Adapters (4 files)
│           │   ├── BoardAdapter.java
│           │   ├── HomeAdapter.java
│           │   ├── TaskAdapter.java
│           │   └── WorkspaceAdapter.java
│           │
│           ├── 🌐 network/                          # Network Infrastructure (1 file)
│           │   └── ApiClient.java
│           │
│           ├── 🔄 sync/                             # Background Sync (1 file)
│           │   └── StartupSyncWorker.java
│           │
│           ├── 🧪 test/                             # Testing Utilities (1 file)
│           │   └── RepositoryTestActivity.java
│           │
│           └── 🛠️ util/                             # Utilities (1 file)
│               └── PerformanceLogger.java
│
├── 📚 docs/                                         # Documentation
│   ├── DATABASE_FIX_ISSUE_1_2_SUMMARY.md
│   ├── DATABASE_LAYER_COMPARISON_REPORT.md
│   ├── GOOGLE_SIGNIN_FIX.md
│   ├── LEADER_REVIEW_CHECKLIST_Room_Implementation.md
│   ├── PROJECT_STRUCTURE.md                         # This file
│   │
│   ├── backend report/
│   │   ├── ACTION_ITEMS_Code_Changes.md
│   │   ├── API_Input_Output_Specification.md
│   │   ├── FIELD_MAPPING_QUICK_REFERENCE.md
│   │   ├── INDEX.md
│   │   ├── README.md
│   │   ├── SUMMARY_Complete_CamelCase_Solution.md
│   │   └── VISUAL_GUIDE.md
│   │
│   ├── refactor/
│   │   ├── (40+ refactoring guides)
│   │
│   └── room implementation/
│       └── ...
│
├── 🔧 gradle/
│   ├── libs.versions.toml
│   └── wrapper/
│
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🏛️ KIẾN TRÚC CLEAN ARCHITECTURE

```
┌─────────────────────────────────────────────────────────────┐
│                      📱 UI LAYER                            │
│                                                             │
│  feature/                                                   │
│  ├── home/ui/         → Màn hình chính, danh sách workspace│
│  ├── auth/ui/         → Đăng nhập, đăng ký                 │
│  └── account/ui/      → Quản lý tài khoản                   │
│                                                             │
│  adapter/             → RecyclerView adapters               │
└─────────────────────────────────────────────────────────────┘
                            ↕️
┌─────────────────────────────────────────────────────────────┐
│                  🎨 PRESENTATION LAYER                      │
│                                                             │
│  presentation/viewmodel/                                    │
│  ├── TaskViewModel         → Quản lý state cho Task        │
│  ├── ProjectViewModel      → Quản lý state cho Project     │
│  ├── WorkspaceViewModel    → Quản lý state cho Workspace   │
│  └── AuthViewModel         → Quản lý authentication        │
└─────────────────────────────────────────────────────────────┘
                            ↕️
┌─────────────────────────────────────────────────────────────┐
│                    🎯 DOMAIN LAYER                          │
│                  (Business Logic - Pure Java)               │
│                                                             │
│  domain/model/          → Task, Project, Workspace, ...    │
│  domain/repository/     → Interfaces (contracts)            │
│  domain/usecase/        → Business rules                    │
└─────────────────────────────────────────────────────────────┘
                            ↕️
┌─────────────────────────────────────────────────────────────┐
│                     💾 DATA LAYER                           │
│                                                             │
│  data/repository/       → Implementations                   │
│  │                                                           │
│  ├── *RepositoryImpl           → API only                  │
│  └── *RepositoryImplWithCache  → Cache + API ⚡            │
│                                                             │
│         ↙️                              ↘️                   │
│  ┌──────────────────┐          ┌──────────────────┐       │
│  │  data/local/     │          │  data/remote/    │       │
│  │  (Room Database) │          │  (Retrofit API)  │       │
│  │                  │          │                  │       │
│  │  - Entities      │          │  - API Services  │       │
│  │  - DAOs          │          │  - DTOs          │       │
│  │  - Converters    │          │  - Mappers       │       │
│  └──────────────────┘          └──────────────────┘       │
│                                                             │
│  data/mapper/       → Entity ↔ Domain conversion          │
└─────────────────────────────────────────────────────────────┘
                            ↕️
┌─────────────────────────────────────────────────────────────┐
│                  ⚙️ INFRASTRUCTURE                          │
│                                                             │
│  core/DependencyProvider  → Singleton DI container          │
│  network/ApiClient        → Retrofit setup                  │
│  auth/TokenManager        → JWT token management            │
│  sync/StartupSyncWorker   → Background data sync            │
└─────────────────────────────────────────────────────────────┘
```

---

## 💾 DATA LAYER - Chi tiết

### 1. Local Storage (Room Database)

```
data/local/database/
│
├── AppDatabase.java              # Database instance (Singleton)
│
├── entity/                       # Bảng trong database
│   ├── TaskEntity.java           # Bảng tasks
│   ├── ProjectEntity.java        # Bảng projects
│   ├── WorkspaceEntity.java      # Bảng workspaces
│   ├── BoardEntity.java          # Bảng boards
│   ├── LabelEntity.java          # Bảng labels
│   └── ...
│
├── dao/                          # Truy vấn database
│   ├── TaskDao.java              # CRUD cho tasks
│   ├── ProjectDao.java           # CRUD cho projects
│   ├── WorkspaceDao.java         # CRUD cho workspaces
│   └── ...
│
└── converter/                    # Convert kiểu dữ liệu
    ├── DateConverter.java        # Long ↔ Date
    ├── ListConverter.java        # List ↔ JSON String
    └── ...
```

**Chức năng**:
- Lưu trữ dữ liệu offline
- Truy vấn nhanh (30-50ms)
- Cache API responses
- Hỗ trợ quan hệ Foreign Key

---

### 2. Remote API (Retrofit)

```
data/remote/
│
├── api/                          # API endpoints
│   ├── TaskApiService.java       # GET, POST, PUT, DELETE tasks
│   ├── ProjectApiService.java    # API cho projects
│   ├── WorkspaceApiService.java  # API cho workspaces
│   └── ...
│
├── dto/                          # Request/Response objects
│   ├── task/
│   │   ├── TaskDTO.java          # Response từ server
│   │   ├── CreateTaskRequest     # Body khi tạo task
│   │   └── UpdateTaskRequest     # Body khi update task
│   └── ...
│
└── mapper/                       # Convert DTO ↔ Domain
    ├── TaskMapper.java           # TaskDTO → Task
    ├── ProjectMapper.java        # ProjectDTO → Project
    └── ...
```

**Chức năng**:
- Gọi API backend
- Xử lý authentication (JWT token)
- Convert JSON ↔ Object
- Error handling

---

### 3. Repository Layer

```
data/repository/
│
├── TaskRepositoryImpl.java                    # API only
├── TaskRepositoryImplWithCache.java ⚡        # Cache + API
│
├── ProjectRepositoryImpl.java                 # API only
│
├── WorkspaceRepositoryImpl.java               # API only
├── WorkspaceRepositoryImplWithCache.java ⚡   # Cache + API
│
└── ...
```

**Chức năng**:
- **Repository (API only)**: Gọi API trực tiếp
- **RepositoryWithCache**: Check cache trước → API sau
- Kết hợp Local + Remote
- Đảm bảo data consistency

---

## 🎯 DOMAIN LAYER - Chi tiết

```
domain/
│
├── model/                        # Pure business objects
│   ├── Task.java                 # id, title, description, status, ...
│   ├── Project.java              # id, name, workspaceId, ...
│   ├── Workspace.java            # id, name, userId, ...
│   ├── Board.java                # id, name, projectId, ...
│   ├── Label.java                # id, name, color, ...
│   └── User.java                 # id, email, displayName, ...
│
├── repository/                   # Interfaces (contracts)
│   ├── ITaskRepository.java
│   ├── IProjectRepository.java
│   ├── IWorkspaceRepository.java
│   └── ...
│
└── usecase/                      # Business logic
    ├── task/
    ├── project/
    └── workspace/
```

**Chức năng**:
- **model/**: Define cấu trúc dữ liệu business
- **repository/**: Interface cho data layer implement
- **usecase/**: Business rules (validate, transform, ...)

---

## 🎨 PRESENTATION LAYER - Chi tiết

```
presentation/viewmodel/
│
├── TaskViewModel.java              # Quản lý state của Task UI
├── ProjectViewModel.java           # Quản lý state của Project UI
├── WorkspaceViewModel.java         # Quản lý state của Workspace UI
├── BoardViewModel.java             # Quản lý state của Board UI
├── AuthViewModel.java              # Quản lý authentication state
└── ViewModelFactoryProvider.java   # Factory để tạo ViewModels
```

**Chức năng**:
- Giữ state của UI
- Gọi repository để lấy/cập nhật data
- Expose LiveData cho UI observe
- Survive configuration changes (xoay màn hình)

---

## 📱 UI LAYER - Chi tiết

```
feature/
│
├── home/
│   └── ui/
│       ├── Home/
│       │   └── HomeActivity.java         # Màn hình chính
│       └── BaseActivity.java             # Base class cho activities
│
├── auth/
│   └── ui/
│       ├── LoginActivity.java            # Đăng nhập
│       ├── SignupActivity.java           # Đăng ký
│       └── ForgotPasswordActivity.java   # Quên mật khẩu
│
└── account/
    └── ui/
        └── AccountActivity.java          # Quản lý tài khoản
```

**Chức năng**:
- Hiển thị UI
- Xử lý user input
- Observe ViewModel
- Navigate giữa các màn hình

---

## 🔄 ADAPTERS - Chi tiết

```
adapter/
│
├── TaskAdapter.java              # RecyclerView cho danh sách tasks
├── ProjectAdapter.java           # RecyclerView cho danh sách projects
├── HomeAdapter.java              # RecyclerView cho workspaces
├── BoardAdapter.java             # RecyclerView cho boards
└── ...
```

**Chức năng**:
- Bind data vào RecyclerView
- Handle click events
- Support ViewHolder pattern

---

## ⚙️ INFRASTRUCTURE - Chi tiết

### 1. DependencyProvider (core/)

```
core/DependencyProvider.java
```

**Chức năng**:
- Singleton pattern
- Tạo và quản lý dependencies
- Lazy initialization
- Provide: Database, DAOs, Repositories, ExecutorService

---

### 2. Network (network/)

```
network/
├── ApiClient.java                # Retrofit client factory
└── AuthInterceptor.java          # Tự động thêm JWT token vào headers
```

**Chức năng**:
- Setup Retrofit
- Base URL configuration
- Add interceptors (auth, logging)

---

### 3. Authentication (auth/)

```
auth/
├── storage/
│   └── TokenManager.java         # Lưu/đọc JWT token, userId
└── AuthManager.java              # Firebase Authentication wrapper
```

**Chức năng**:
- Quản lý Firebase Auth
- Lưu JWT token vào SharedPreferences
- Provide userId cho queries

---

### 4. Background Sync (sync/)

```
sync/
└── StartupSyncWorker.java        # WorkManager worker
```

**Chức năng**:
- Sync data khi app khởi động
- Background periodic sync
- Handle network constraints

---

## 🔄 DATA FLOW - Luồng dữ liệu

### 📖 READ (Cache-First Pattern)

```
1. HomeActivity
   └─> loadWorkspacesWithCache()

2. WorkspaceRepositoryImplWithCache
   └─> Check cache (Room DB)
       │
       ├─ HIT ✅ (30-50ms)
       │  └─> Return cached data
       │      └─> UI updates ngay lập tức
       │
       └─ MISS ❌
          └─> onCacheEmpty()
              └─> Call WorkspaceViewModel
                  └─> Call API (500-1000ms)
                      └─> Save to cache
                          └─> UI updates
```

### ✍️ WRITE (Create/Update/Delete)

```
1. User action (tạo/sửa/xóa)
   └─> Activity/Fragment

2. ViewModel
   └─> Call Repository

3. Repository
   └─> Send to API
       └─> Success
           └─> Update cache
               └─> Notify UI
```

---

## 📦 MODULES & COMPONENTS

### Core Modules
- **App**: Application initialization
- **Auth**: Authentication & Authorization
- **Core**: Dependency Injection
- **Network**: API communication
- **Sync**: Background synchronization

### Feature Modules
- **Home**: Main workspace/project list
- **Auth**: Login, Signup
- **Account**: User profile management

### Data Modules
- **Local**: Room Database (offline storage)
- **Remote**: Retrofit API (online data)
- **Repository**: Combine local + remote

### Shared Modules
- **Adapter**: RecyclerView adapters
- **Util**: Utility functions
- **Test**: Testing utilities

---

## 📊 ENTITIES (Database Tables)

```
📦 Room Database Tables:
│
├── workspaces              # Workspaces
│   ├── id (PK)
│   ├── name
│   ├── userId
│   └── timestamps
│
├── projects                # Projects
│   ├── id (PK)
│   ├── name
│   ├── workspaceId (FK)
│   └── timestamps
│
├── boards                  # Boards (Kanban)
│   ├── id (PK)
│   ├── name
│   ├── projectId (FK)
│   └── timestamps
│
├── tasks                   # Tasks
│   ├── id (PK)
│   ├── title
│   ├── projectId (FK)
│   ├── boardId (FK)
│   ├── status
│   ├── priority
│   └── timestamps
│
├── labels                  # Labels/Tags
│   ├── id (PK)
│   ├── name
│   ├── color
│   └── workspaceId (FK)
│
└── assignments             # Task assignments
    ├── id (PK)
    ├── taskId (FK)
    ├── userId
    └── timestamps
```

---

## 🎯 CHỨC NĂNG CHÍNH

### 1. Authentication
- ✅ Đăng nhập/Đăng ký với Firebase
- ✅ Lưu JWT token
- ✅ Auto-login nếu có token
- ✅ Logout

### 2. Workspace Management
- ✅ Xem danh sách workspaces
- ✅ Tạo workspace mới
- ✅ Sửa workspace
- ✅ Xóa workspace

### 3. Project Management
- ✅ Xem projects trong workspace
- ✅ Tạo/sửa/xóa project
- ✅ Filter projects

### 4. Task Management
- ✅ Xem tasks trong project
- ✅ Tạo/sửa/xóa task
- ✅ Assign tasks
- ✅ Set priority, due date

### 5. Offline Support
- ✅ Cache data với Room DB
- ✅ Hoạt động offline
- ✅ Sync khi có mạng

### 6. Performance
- ⚡ Cache-first: 30-50ms
- 🌐 API fallback: 500-1000ms
- 📈 95-97% faster với cache

---

## 📝 NAMING CONVENTIONS

### Package Structure
```
com.example.tralalero.
├── data.*              → Data layer
├── domain.*            → Domain layer
├── presentation.*      → Presentation layer
├── feature.*           → UI layer
├── core.*              → Infrastructure
└── util.*              → Utilities
```

### Class Names
- `*Activity` - Android Activities
- `*Fragment` - Android Fragments
- `*Adapter` - RecyclerView Adapters
- `*ViewModel` - ViewModels
- `*Repository` / `*RepositoryImpl` - Repositories
- `*Entity` - Room entities
- `*Dao` - Data Access Objects
- `*DTO` - Data Transfer Objects
- `*Mapper` - Data converters

---

## 🚀 STARTUP FLOW

```
1. App.onCreate()
   ├── Initialize Firebase
   ├── Create DependencyProvider
   │   ├── Create AppDatabase
   │   ├── Create ExecutorService
   │   └── Create TokenManager
   └── Start background sync

2. MainActivity
   ├── Check authentication
   │   ├── Logged in → HomeActivity
   │   └── Not logged in → LoginActivity

3. HomeActivity
   ├── Load workspaces from cache
   ├── Display immediately (30-50ms)
   └── Background API refresh
```

---

**Architecture**: Clean Architecture + MVVM  
**Database**: Room (SQLite)  
**API**: Retrofit  
**Auth**: Firebase + JWT  
**Performance**: Cache-first (30-50ms)  

**Date**: October 20, 2025
