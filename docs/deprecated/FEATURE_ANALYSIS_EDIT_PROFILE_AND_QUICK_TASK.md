# Phân Tích Chi Tiết 2 Features: Edit Profile & Create Quick Task

## Mục lục

1. [Feature 1: Edit Profile](#feature-1-edit-profile)
2. [Feature 2: Create Quick Task](#feature-2-create-quick-task)

---

## Feature 1: Edit Profile

### 1.1. Tổng quan

Feature cho phép người dùng chỉnh sửa thông tin cá nhân (tên và avatar) với upload ảnh lên Supabase Storage.

### 1.2. Architecture & Classes

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
├─────────────────────────────────────────────────────────────┤
│  EditProfileActivity.java                                    │
│  - UI Controllers                                            │
│  - Event Handlers                                            │
│  - Image Pickers                                             │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      NETWORK LAYER                           │
├─────────────────────────────────────────────────────────────┤
│  AuthApi.java (Retrofit Interface)                          │
│  - getMe(): Call<UserDto>                                   │
│  - updateProfile(UpdateProfileRequest): Call<UserDto>       │
│  - requestUploadUrl(UploadUrlRequest): Call<UploadUrlResponse>│
│  - uploadToSupabase(signedUrl, file): Call<ResponseBody>   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                       DATA LAYER                             │
├─────────────────────────────────────────────────────────────┤
│  DTOs:                                                       │
│  - UserDto.java (Response từ backend)                       │
│  - UpdateProfileRequest.java (Request update profile)       │
│  - UploadUrlRequest.java (Request signed URL)               │
│  - UploadUrlResponse.java (Response với signed URL)         │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    EXTERNAL SERVICES                         │
├─────────────────────────────────────────────────────────────┤
│  - Backend API (NestJS)                                      │
│  - Supabase Storage                                          │
│  - Firebase Auth                                             │
│  - Glide (Image Loading)                                     │
└─────────────────────────────────────────────────────────────┘
```

### 1.3. Sequence Diagram - Load Profile

```
User -> EditProfileActivity: Mở màn hình Edit Profile
    EditProfileActivity -> onCreate(): Khởi tạo
        onCreate() -> initViews(): Tìm tất cả views
        onCreate() -> setupActivityResultLaunchers(): Setup image pickers
        onCreate() -> setupToolbar(): Setup navigation
        onCreate() -> setupClickListeners(): Setup button listeners
        onCreate() -> loadCurrentProfile(): Load dữ liệu hiện tại

    loadCurrentProfile() -> FirebaseAuth: getDisplayName(), getEmail()
        FirebaseAuth --> loadCurrentProfile(): Trả về name, email
    loadCurrentProfile() -> UI: Hiển thị name, email, letter avatar
    loadCurrentProfile() -> fetchUserProfileFromBackend(): Gọi API

    fetchUserProfileFromBackend() -> ApiClient: create(AuthApi.class)
        ApiClient --> fetchUserProfileFromBackend(): authApi instance
    fetchUserProfileFromBackend() -> authApi.getMe(): GET /api/users/me
        authApi.getMe() -> Backend: HTTP GET with Firebase token
        Backend --> authApi.getMe(): UserDto (JSON camelCase)
        authApi.getMe() --> fetchUserProfileFromBackend(): Response<UserDto>

    fetchUserProfileFromBackend() -> Callback.onResponse(): Parse response
        onResponse() -> UserDto: Lấy name, avatarUrl
        onResponse() -> UI: Update etName, etUsername, tvAvatarLetter
        onResponse() -> loadAvatarImage(avatarUrl): Load ảnh

    loadAvatarImage() -> Glide.with(context): Khởi tạo Glide
        Glide.with() -> load(url): Set URL
        load() -> diskCacheStrategy(ALL): Enable cache
        diskCacheStrategy() -> circleCrop(): Transform hình tròn
        circleCrop() -> listener(): Set callbacks
        listener() -> into(imgAvatar): Load vào ImageView

        Glide -> Supabase: Download image từ URL
        Supabase --> Glide: Image bytes
        Glide -> RequestListener.onResourceReady(): Ảnh load thành công
            onResourceReady() -> UI: imgAvatar.setVisibility(VISIBLE)
            onResourceReady() -> UI: tvAvatarLetter.setVisibility(GONE)
        Glide --> User: Hiển thị avatar tròn
```

### 1.4. Sequence Diagram - Upload Avatar

```
User -> avatarContainer: Click vào avatar
    avatarContainer -> showImagePickerBottomSheet(): Hiện bottom sheet
        showImagePickerBottomSheet() -> BottomSheetDialog: Tạo dialog
        BottomSheetDialog -> User: Hiện 3 options (Camera/Gallery/Remove)

User -> layoutChooseGallery: Chọn "Choose from gallery"
    layoutChooseGallery -> checkStoragePermissionAndOpen(): Kiểm tra permission
        checkStoragePermissionAndOpen() -> ContextCompat: checkSelfPermission()
            ContextCompat --> checkStoragePermissionAndOpen(): GRANTED
        checkStoragePermissionAndOpen() -> openGallery(): Mở gallery
            openGallery() -> Intent: ACTION_PICK
            Intent -> galleryLauncher.launch(): Hiện gallery picker

User -> Gallery: Chọn ảnh
    Gallery --> galleryLauncher: onActivityResult(imageUri)
        galleryLauncher -> uploadImageToFirebase(imageUri): Bắt đầu upload

uploadImageToFirebase() -> Step 1: Request Signed URL
    uploadImageToFirebase() -> getFileNameFromUri(uri): Lấy tên file
        getFileNameFromUri() -> ContentResolver: query(uri)
        ContentResolver --> getFileNameFromUri(): "avatar.jpg"
    uploadImageToFirebase() -> ApiClient: create(AuthApi.class)
    uploadImageToFirebase() -> UploadUrlRequest: new UploadUrlRequest(fileName)
    uploadImageToFirebase() -> authApi.requestUploadUrl(request): POST /api/storage/upload-url
        authApi.requestUploadUrl() -> Backend: POST request với fileName
        Backend -> Supabase: Tạo signed URL
        Supabase --> Backend: signedUrl, storagePath
        Backend --> authApi.requestUploadUrl(): UploadUrlResponse
            {
              "path": "userId/uploads/timestamp-filename.png",
              "signedUrl": "https://supabase.co/.../signed-url",
              "token": "token..."
            }
        authApi.requestUploadUrl() --> uploadImageToFirebase(): Response<UploadUrlResponse>

    uploadImageToFirebase() -> Callback.onResponse(): Nhận signed URL
        onResponse() -> uploadFileToSupabase(uri, signedUrl, storagePath): Step 2

uploadFileToSupabase() -> Step 2: Upload File
    uploadFileToSupabase() -> ContentResolver: openInputStream(uri)
        ContentResolver --> uploadFileToSupabase(): InputStream
    uploadFileToSupabase() -> InputStream: read() -> byte[]
    uploadFileToSupabase() -> getMimeType(uri): "image/jpeg"
    uploadFileToSupabase() -> RequestBody: create(MediaType, bytes)
    uploadFileToSupabase() -> authApi.uploadToSupabase(signedUrl, body): PUT to signed URL
        authApi.uploadToSupabase() -> Supabase: PUT file bytes
        Supabase --> authApi.uploadToSupabase(): 200 OK
        authApi.uploadToSupabase() --> uploadFileToSupabase(): Response<ResponseBody>

    uploadFileToSupabase() -> Callback.onResponse(): Upload thành công
        onResponse() -> SupabaseConfig.getPublicUrl(storagePath): Construct URL
            getPublicUrl() = SUPABASE_URL + "/storage/v1/object/public/images/" + storagePath
            getPublicUrl() --> onResponse(): "https://...supabase.co/.../images/userId/uploads/file.png"
        onResponse() -> newAvatarUrl = publicUrl: Lưu URL mới
        onResponse() -> avatarChanged = true: Đánh dấu đã thay đổi
        onResponse() -> loadAvatarImage(publicUrl): Hiển thị ảnh ngay lập tức
            loadAvatarImage() -> Glide: Load từ public URL
            Glide --> UI: Hiển thị avatar mới
        onResponse() -> Toast: "Photo uploaded successfully"
```

### 1.5. Sequence Diagram - Save Profile

```
User -> btnSave: Click "Save Changes"
    btnSave -> saveProfile(): Validate và save
        saveProfile() -> etName.getText(): Lấy name mới
        saveProfile() -> Validation: Check empty, length
            Validation --> saveProfile(): Valid
        saveProfile() -> Compare: So sánh với name cũ
            Compare --> saveProfile(): Có thay đổi
        saveProfile() -> showLoading(true): Hiện loading overlay
        saveProfile() -> updateFirebaseProfile(newName): Step 1 - Update Firebase

updateFirebaseProfile() -> Step 1: Update Firebase
    updateFirebaseProfile() -> FirebaseUser: getCurrentUser()
    updateFirebaseProfile() -> UserProfileChangeRequest.Builder: setDisplayName(newName)
    updateFirebaseProfile() -> firebaseUser.updateProfile(request): Update Firebase
        firebaseUser.updateProfile() -> Firebase: Update profile
        Firebase --> firebaseUser.updateProfile(): Success
        firebaseUser.updateProfile() --> updateFirebaseProfile(): Task<Void>

    updateFirebaseProfile() -> onSuccessListener: Firebase updated
        onSuccessListener -> Log: "✓ Firebase profile updated"
        onSuccessListener -> updateBackendProfile(name, avatarUrl): Step 2

updateBackendProfile() -> Step 2: Update Backend
    updateBackendProfile() -> ApiClient: create(AuthApi.class)
    updateBackendProfile() -> UpdateProfileRequest: new UpdateProfileRequest(name, avatarUrl)
        UpdateProfileRequest {
          "name": "New Name",
          "avatarUrl": "https://...supabase.co/.../avatar.png"  // hoặc null
        }
    updateBackendProfile() -> authApi.updateProfile(request): PUT /api/users/me
        authApi.updateProfile() -> Backend: PUT request với JWT token
        Backend -> Database: UPDATE users SET name, avatar_url
        Database --> Backend: Updated user
        Backend --> authApi.updateProfile(): UserDto
        authApi.updateProfile() --> updateBackendProfile(): Response<UserDto>

    updateBackendProfile() -> Callback.onResponse(): Backend updated
        onResponse() -> TokenManager: saveAuthData(..., newName): Update cache
        onResponse() -> showLoading(false): Ẩn loading
        onResponse() -> Toast: "✅ Profile updated successfully!"
        onResponse() -> setResult(RESULT_OK): Trả kết quả cho caller
        onResponse() -> finish(): Đóng activity

EditProfileActivity --> AccountActivity: Return with RESULT_OK
    AccountActivity -> editProfileLauncher.onActivityResult(): Nhận kết quả
        onActivityResult -> refreshProfileUI(): Refresh UI
            refreshProfileUI() -> loadUserProfileFromBackend(): Reload từ backend
            loadUserProfileFromBackend() -> authApi.getMe(): GET /api/users/me
            authApi.getMe() --> loadUserProfileFromBackend(): UserDto với data mới
            loadUserProfileFromBackend() -> UI: Update name
            loadUserProfileFromBackend() -> loadAvatarImage(): Load avatar mới
            loadAvatarImage() -> Glide: Load từ URL mới
            Glide --> UI: Hiển thị avatar mới trong AccountActivity
        refreshProfileUI() -> Toast: "Profile updated!"
```

### 1.6. Key Components Giải thích

#### 1.6.1. EditProfileActivity

**Vai trò**: Activity chính quản lý màn hình edit profile

**Properties quan trọng**:

- `firebaseUser`: User hiện tại từ Firebase Auth
- `tokenManager`: Quản lý auth tokens
- `newAvatarUrl`: URL avatar mới sau khi upload
- `avatarChanged`: Flag đánh dấu có thay đổi avatar không

**Methods chính**:

- `onCreate()`: Initialize UI, setup listeners, load profile
- `loadCurrentProfile()`: Load data từ Firebase
- `fetchUserProfileFromBackend()`: Gọi API GET /users/me
- `loadAvatarImage(url)`: Dùng Glide load ảnh
- `uploadImageToFirebase(uri)`: Step 1 - Request signed URL
- `uploadFileToSupabase(uri, signedUrl, path)`: Step 2 - Upload file
- `saveProfile()`: Validate và bắt đầu update
- `updateFirebaseProfile(name)`: Update Firebase Auth
- `updateBackendProfile(name, avatarUrl)`: Update backend database

#### 1.6.2. AuthApi (Retrofit Interface)

**Vai trò**: Định nghĩa các API endpoints

```java
@GET("users/me")
Call<UserDto> getMe();  // Lấy thông tin user

@PUT("users/me")
Call<UserDto> updateProfile(@Body UpdateProfileRequest request);  // Update profile

@POST("storage/upload-url")
Call<UploadUrlResponse> requestUploadUrl(@Body UploadUrlRequest request);  // Xin signed URL

@PUT
Call<ResponseBody> uploadToSupabase(@Url String signedUrl, @Body RequestBody file);  // Upload file
```

#### 1.6.3. SupabaseConfig

**Vai trò**: Helper class cho Supabase configuration

```java
public class SupabaseConfig {
    public static final String SUPABASE_URL = "https://acswtonhplvmdryauyhp.supabase.co";
    public static final String STORAGE_BUCKET = "images";

    // Convert storage path thành public URL
    public static String getPublicUrl(String storagePath) {
        return SUPABASE_URL + "/storage/v1/object/public/" + STORAGE_BUCKET + "/" + storagePath;
    }
}
```

**Cách hoạt động**:

- Backend trả về `storagePath`: `"userId/uploads/timestamp-file.png"`
- `getPublicUrl()` tạo full URL: `"https://...supabase.co/storage/v1/object/public/images/userId/uploads/timestamp-file.png"`
- URL này là public, không cần authentication để view

#### 1.6.4. Glide Image Loading

**Vai trò**: Load và cache images

```java
Glide.with(context)
    .load(avatarUrl)                          // Source URL
    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache cả original và transformed
    .circleCrop()                             // Transform thành hình tròn
    .listener(RequestListener)                 // Handle success/error
    .into(imgAvatar);                         // Target ImageView
```

**Flow**:

1. Check memory cache → Có → Return
2. Check disk cache → Có → Load từ disk
3. Không có cache → Download từ URL
4. Transform (circleCrop)
5. Save to cache
6. Display in ImageView

---

## Feature 2: Create Quick Task

### 2.1. Tổng quan

Feature cho phép tạo task nhanh từ Inbox Activity, tự động assign vào default project và board.

### 2.2. Architecture & Classes

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
├─────────────────────────────────────────────────────────────┤
│  InboxActivity.java                                          │
│  - FloatingActionButton (FAB)                                │
│  - EditText (etQuickTask)                                    │
│  - RecyclerView (Task List)                                  │
│  - TaskAdapter                                               │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                            │
├─────────────────────────────────────────────────────────────┤
│  ITaskRepository.java (Interface)                            │
│  - createQuickTask(title, description, callback)            │
│  - getTasksByProject(projectId, callback)                   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                       DATA LAYER                             │
├─────────────────────────────────────────────────────────────┤
│  TaskRepositoryImpl.java (Implementation)                    │
│  - createQuickTask() → Call API                              │
│  - Convert DTO ↔ Domain Model                                │
│                                                              │
│  TaskApiService.java (Retrofit)                              │
│  - createQuickTask(@Body Map): Call<TaskDTO>                │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     CACHE LAYER                              │
├─────────────────────────────────────────────────────────────┤
│  TaskRepositoryWithCache.java                                │
│  - saveTaskToCache(task)                                     │
│  - getTasksFromCache(projectId)                              │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    EXTERNAL SERVICES                         │
├─────────────────────────────────────────────────────────────┤
│  - Backend API (NestJS)                                      │
│  - Room Database (Local Cache)                               │
└─────────────────────────────────────────────────────────────┘
```

### 2.3. Sequence Diagram - Create Quick Task

```
User -> FloatingActionButton: Click FAB (ic_add)
    FAB -> etQuickTask: Show EditText
        etQuickTask -> User: Hiện input field với hint "Add a task..."

User -> etQuickTask: Nhập text "Buy groceries"
    etQuickTask -> TextWatcher: afterTextChanged()
        TextWatcher -> Do nothing: Chỉ listen, không action

User -> etQuickTask: Press Enter (IME_ACTION_DONE)
    etQuickTask -> OnEditorActionListener: onEditorAction()
        onEditorAction() -> getText(): Lấy text từ input
        onEditorAction() -> trim(): Remove whitespace
        onEditorAction() -> Check: text.isEmpty()?
            Check --> onEditorAction(): Not empty
        onEditorAction() -> Log: "User submitted: Buy groceries"
        onEditorAction() -> createTask("Buy groceries"): Gọi method tạo task
        onEditorAction() -> setText(""): Clear input
        onEditorAction() -> clearFocus(): Ẩn keyboard
        onEditorAction() -> return true: Consume event

createTask("Buy groceries") -> Step 1: Setup Repository
    createTask() -> Log: "Creating quick task: Buy groceries"
    createTask() -> ApiClient.get(authManager): Lấy authenticated client
        ApiClient.get() -> AuthManager: getFirebaseIdToken()
        AuthManager --> ApiClient.get(): "Bearer eyJhbGc..."
        ApiClient.get() -> OkHttpClient: Add interceptor with token
        ApiClient.get() -> Retrofit.Builder: Build retrofit
        ApiClient.get() --> createTask(): Retrofit instance
    createTask() -> Retrofit.create(TaskApiService.class): Tạo API service
    createTask() -> new TaskRepositoryImpl(apiService): Khởi tạo repository

createTask() -> Step 2: Call Repository
    createTask() -> taskRepository.createQuickTask(title="Buy groceries", description="", callback)

taskRepository.createQuickTask() -> Step 3: Prepare Request
    createQuickTask() -> Log: "Creating quick task: Buy groceries"
    createQuickTask() -> new HashMap<>(): Tạo request body
    createQuickTask() -> map.put("title", "Buy groceries")
    createQuickTask() -> map.put("description", "")
        Request body = {
          "title": "Buy groceries",
          "description": ""
        }
    createQuickTask() -> apiService.createQuickTask(map): POST /api/tasks/quick

apiService.createQuickTask() -> Step 4: Backend Processing
    createQuickTask() -> Backend: POST /api/tasks/quick với JWT token
        Backend -> AuthGuard: Validate JWT token
        AuthGuard --> Backend: userId = "029d5c02-..."
        Backend -> ProjectService: findDefaultProject(userId)
        ProjectService -> Database: SELECT * FROM projects WHERE user_id = ? AND is_default = true
        Database --> ProjectService: defaultProject {id: "87c2a...", name: "Default"}
        ProjectService --> Backend: defaultProject
        Backend -> BoardService: findDefaultBoard(projectId)
        BoardService -> Database: SELECT * FROM boards WHERE project_id = ? AND is_default = true
        Database --> BoardService: defaultBoard {id: "board-123", name: "To Do"}
        BoardService --> Backend: defaultBoard
        Backend -> TaskService: create({
            title: "Buy groceries",
            description: "",
            projectId: "87c2a...",
            boardId: "board-123",
            userId: "029d5c02-..."
        })
        TaskService -> Database: INSERT INTO tasks (...)
        Database --> TaskService: Task created với ID mới
        TaskService --> Backend: Task object
        Backend -> TransformInterceptor: Convert snake_case → camelCase
        TransformInterceptor --> Backend: TaskDTO (camelCase)
        Backend --> apiService.createQuickTask(): Response<TaskDTO>

apiService.createQuickTask() -> Callback.onResponse(): Nhận response
    onResponse() -> Check: response.isSuccessful() && body != null
        Check --> onResponse(): true (201 Created)
    onResponse() -> TaskDTO: body()
        TaskDTO = {
          "id": "task-uuid-123",
          "title": "Buy groceries",
          "description": "",
          "projectId": "87c2a...",
          "boardId": "board-123",
          "status": "TODO",
          "priority": "MEDIUM",
          "createdAt": "2025-10-22T06:30:00Z",
          "updatedAt": "2025-10-22T06:30:00Z"
        }
    onResponse() -> TaskMapper.toTask(taskDTO): Convert DTO → Domain Model
        toTask() -> new Task(): Tạo domain object
        toTask() -> setId(), setTitle(), etc: Map fields
        toTask() --> onResponse(): Task object
    onResponse() -> callback.onSuccess(task): Return to UI layer

callback.onSuccess(task) -> Step 5: Update UI
    onSuccess() -> runOnUiThread(): Chuyển về main thread
        runOnUiThread() -> Log: "✓ Quick task created successfully"
        runOnUiThread() -> Log: "  ID: task-uuid-123"
        runOnUiThread() -> Log: "  Title: Buy groceries"
        runOnUiThread() -> Log: "  Project: 87c2a..."
        runOnUiThread() -> Log: "  Board: board-123"

        runOnUiThread() -> Step 5.1: Save to Cache
        runOnUiThread() -> App.dependencyProvider.getTaskRepositoryWithCache()
            dependencyProvider --> runOnUiThread(): taskRepoCache
        runOnUiThread() -> taskRepoCache.saveTaskToCache(task)
            saveTaskToCache() -> TaskDao: insert(TaskEntity)
            TaskDao -> Room Database: INSERT INTO tasks_cache
            Room Database --> TaskDao: Success
            TaskDao --> saveTaskToCache(): Cached
            saveTaskToCache() --> runOnUiThread(): Done
        runOnUiThread() -> Log: "✓ Task saved to cache"

        runOnUiThread() -> Step 5.2: Update RecyclerView (Optimistic UI)
        runOnUiThread() -> taskAdapter.getTasks(): Lấy danh sách hiện tại
            taskAdapter.getTasks() --> runOnUiThread(): currentTasks[]
        runOnUiThread() -> Check: Task đã tồn tại chưa?
            for (task in currentTasks) {
                if (task.getId() == newTask.getId()) {
                    taskExists = true;
                }
            }
            Check --> runOnUiThread(): false (task mới)
        runOnUiThread() -> new ArrayList<>(): updatedTasks
        runOnUiThread() -> updatedTasks.add(0, newTask): Add vào đầu list
        runOnUiThread() -> updatedTasks.addAll(currentTasks): Add tasks cũ
        runOnUiThread() -> taskAdapter.setTasks(updatedTasks): Update adapter
            setTasks() -> notifyDataSetChanged(): Refresh UI
            notifyDataSetChanged() --> RecyclerView: Render lại
            RecyclerView --> User: Hiển thị task mới ở top
        runOnUiThread() -> recyclerView.setVisibility(VISIBLE): Show list
        runOnUiThread() -> recyclerView.smoothScrollToPosition(0): Scroll to top
            smoothScrollToPosition() --> User: Scroll mượt đến task mới
        runOnUiThread() -> Toast: "✅ Task created: Buy groceries"

        runOnUiThread() -> Step 5.3: Refresh từ Server (Verify)
        runOnUiThread() -> loadQuickTasks(): Reload toàn bộ list
            loadQuickTasks() -> taskRepository.getTasksByProject(projectId, callback)
            taskRepository.getTasksByProject() -> Backend: GET /api/tasks/by-project/:id
            Backend --> taskRepository: List<TaskDTO>
            taskRepository --> loadQuickTasks(): List<Task>
            loadQuickTasks() -> taskAdapter.setTasks(serverTasks): Update với data từ server
            taskAdapter --> RecyclerView: Render với data mới nhất
            RecyclerView --> User: Đảm bảo sync với server
```

### 2.4. Sequence Diagram - Error Handling

```
User -> etQuickTask: Press Enter với empty text
    etQuickTask -> onEditorAction(): getText()
        onEditorAction() -> trim(): ""
        onEditorAction() -> Check: isEmpty()?
            Check --> onEditorAction(): true
        onEditorAction() -> return false: Không làm gì, giữ focus

User -> etQuickTask: Nhập "Buy milk" + Press Enter
    etQuickTask -> createTask("Buy milk")
        createTask() -> taskRepository.createQuickTask(...)
            createQuickTask() -> apiService.createQuickTask()
                apiService -> Backend: POST /api/tasks/quick
                Backend -> Error: Network timeout / 500 Error
                Backend --> apiService: Exception
                apiService --> createQuickTask(): onFailure(throwable)

            createQuickTask() -> Callback.onFailure(): Handle error
                onFailure() -> Log: "Failed to create quick task: Network timeout"
                onFailure() -> callback.onError(error.getMessage())

        createTask() -> Callback.onError(): Nhận error
            onError() -> runOnUiThread(): Switch to main thread
                runOnUiThread() -> Log: "Failed to create task: Network timeout"
                runOnUiThread() -> Toast: "❌ Failed to create task. Please try again."
                runOnUiThread() -> Do NOT update RecyclerView: Không show task lỗi
                runOnUiThread() -> etQuickTask.setText("Buy milk"): Restore text
                    → User có thể retry
```

### 2.5. Key Components Giải thích

#### 2.5.1. InboxActivity

**Vai trò**: Activity quản lý danh sách quick tasks

**Properties quan trọng**:

- `etQuickTask`: EditText để nhập task title
- `fab`: FloatingActionButton để show/hide input
- `recyclerView`: Hiển thị danh sách tasks
- `taskAdapter`: Adapter quản lý data cho RecyclerView
- `taskRepository`: Repository để gọi API

**Methods chính**:

- `onCreate()`: Setup UI, FAB click listener
- `setupQuickTaskInput()`: Setup EditText với IME listener
- `createTask(title)`: Tạo task mới
- `loadQuickTasks()`: Load danh sách tasks từ server

#### 2.5.2. ITaskRepository (Interface)

**Vai trò**: Contract cho task operations

```java
public interface ITaskRepository {
    void createQuickTask(String title, String description, RepositoryCallback<Task> callback);
    void getTasksByProject(String projectId, RepositoryCallback<List<Task>> callback);

    interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
```

**Tại sao dùng Interface?**:

- Decouple UI khỏi implementation details
- Dễ test với mock repository
- Có thể swap implementation (API vs Local)

#### 2.5.3. TaskRepositoryImpl

**Vai trò**: Implementation thực tế của repository

```java
public void createQuickTask(String title, String description, RepositoryCallback<Task> callback) {
    // 1. Prepare request
    Map<String, String> quickTaskData = new HashMap<>();
    quickTaskData.put("title", title);
    quickTaskData.put("description", description);

    // 2. Call API
    apiService.createQuickTask(quickTaskData).enqueue(new Callback<TaskDTO>() {
        @Override
        public void onResponse(Call<TaskDTO> call, Response<TaskDTO> response) {
            if (response.isSuccessful() && response.body() != null) {
                // 3. Convert DTO to Domain Model
                Task task = TaskMapper.toTask(response.body());

                // 4. Return success
                callback.onSuccess(task);
            } else {
                callback.onError("Server error: " + response.code());
            }
        }

        @Override
        public void onFailure(Call<TaskDTO> call, Throwable t) {
            callback.onError("Network error: " + t.getMessage());
        }
    });
}
```

**Nhiệm vụ**:

1. Convert input parameters → Request format
2. Call Retrofit API
3. Handle response/errors
4. Convert DTO → Domain Model
5. Call callback với result

#### 2.5.4. TaskApiService (Retrofit)

**Vai trò**: Định nghĩa HTTP endpoints

```java
public interface TaskApiService {
    @POST("tasks/quick")
    Call<TaskDTO> createQuickTask(@Body Map<String, String> quickTaskData);

    @GET("tasks/by-project/{projectId}")
    Call<List<TaskDTO>> getTasksByProject(@Path("projectId") String projectId);
}
```

**Retrofit hoạt động**:

1. Parse interface annotations (@POST, @Body, etc)
2. Build HTTP request (method, headers, body)
3. Add authentication interceptor (JWT token)
4. Execute network call
5. Parse JSON response → DTO object
6. Return via Callback

#### 2.5.5. Optimistic UI Update

**Vai trò**: Update UI ngay lập tức trước khi server confirm

**Flow**:

```java
// 1. Add task to list immediately (Optimistic)
List<Task> updatedTasks = new ArrayList<>();
updatedTasks.add(newTask);        // ← Task mới ở đầu
updatedTasks.addAll(currentTasks); // ← Tasks cũ

taskAdapter.setTasks(updatedTasks); // ← UI update ngay
recyclerView.smoothScrollToPosition(0); // ← Scroll to new task

// 2. Then verify with server
loadQuickTasks(); // ← Reload từ server để sync
```

**Lợi ích**:

- UI responsive ngay lập tức (không chờ server)
- UX tốt hơn (no lag)
- Server refresh sau để đảm bảo sync

**Rủi ro**:

- Nếu server fail → Cần remove task khỏi UI
- Cần handle conflict (server có data khác)

### 2.6. Backend Logic Giải thích

#### Backend API: POST /api/tasks/quick

```typescript
// Controller
@Post('quick')
async createQuickTask(@Body() data: { title: string; description: string }, @Request() req) {
  const userId = req.user.id; // Từ JWT token

  // 1. Find default project
  const defaultProject = await this.projectService.findDefaultProject(userId);
  if (!defaultProject) {
    throw new NotFoundException('Default project not found');
  }

  // 2. Find default board in project
  const defaultBoard = await this.boardService.findDefaultBoard(defaultProject.id);
  if (!defaultBoard) {
    throw new NotFoundException('Default board not found');
  }

  // 3. Create task with defaults
  const task = await this.taskService.create({
    title: data.title,
    description: data.description,
    projectId: defaultProject.id,
    boardId: defaultBoard.id,
    userId: userId,
    status: 'TODO',
    priority: 'MEDIUM'
  });

  return task; // Auto transform to camelCase by TransformInterceptor
}
```

**Logic**:

1. **Authenticate**: JWT token → userId
2. **Find default project**: Mỗi user có 1 default project
3. **Find default board**: Trong project có 1 default board (thường "To Do")
4. **Create task**: Insert vào database với defaults
5. **Transform response**: snake_case → camelCase cho Android

---

## So sánh 2 Features

| Aspect                | Edit Profile                                                         | Create Quick Task         |
| --------------------- | -------------------------------------------------------------------- | ------------------------- |
| **Complexity**        | High (2-step upload, multiple APIs)                                  | Medium (1 API call)       |
| **Network Calls**     | 3-4 calls (getMe, requestUploadUrl, uploadToSupabase, updateProfile) | 1 call (createQuickTask)  |
| **External Services** | Firebase Auth, Supabase Storage, Backend                             | Backend only              |
| **UI Updates**        | Load then update                                                     | Optimistic update         |
| **Error Handling**    | Multiple points (upload, save)                                       | Single point (create)     |
| **Caching**           | No cache (always fresh)                                              | Save to Room cache        |
| **Image Processing**  | Glide with circle crop                                               | N/A                       |
| **Data Flow**         | UI → Firebase → Supabase → Backend → UI                              | UI → Backend → Cache → UI |

---

## Best Practices Được Áp Dụng

### 1. **Separation of Concerns**

- UI Layer (Activity) chỉ lo render
- Repository Layer lo business logic
- API Layer lo network calls
- DTO Layer lo data transfer

### 2. **Dependency Injection**

```java
ApiClient.get(App.authManager).create(AuthApi.class)
```

- Inject authenticated client
- Centralized auth token management

### 3. **Error Handling**

```java
try {
    // Network call
} catch (Exception e) {
    callback.onError(e.getMessage());
    Log.e(TAG, "Error", e);
}
```

- Always log errors
- Show user-friendly messages
- Handle all error cases

### 4. **Async Operations**

```java
runOnUiThread(() -> {
    // Update UI safely
});
```

- Network calls trên background thread
- UI updates trên main thread
- Prevent ANR (Application Not Responding)

### 5. **Callback Pattern**

```java
interface RepositoryCallback<T> {
    void onSuccess(T result);
    void onError(String error);
}
```

- Async communication giữa layers
- Type-safe với generics
- Clear success/error paths

---

## Kết luận

### Edit Profile Feature

- **Complex workflow**: Multi-step upload process
- **Multiple services**: Firebase, Supabase, Backend coordination
- **Image handling**: Glide for loading, caching, transforming
- **2-step upload**: Request URL → Upload file → Update profile
- **URL construction**: Backend path → Public URL via SupabaseConfig

### Create Quick Task Feature

- **Simple workflow**: Single API call
- **Smart defaults**: Auto-assign to default project/board
- **Optimistic UI**: Instant feedback, verify later
- **Caching**: Room database for offline access
- **Clean architecture**: Interface → Implementation → API

Cả 2 features đều follow:

- ✅ Clean Architecture principles
- ✅ SOLID principles
- ✅ Proper error handling
- ✅ Logging for debugging
- ✅ User feedback (Toast, Loading states)
