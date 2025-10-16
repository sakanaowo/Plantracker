# Hướng Dẫn Sử Dụng Task List Cho Project

## Tổng Quan

Đã cập nhật hệ thống để lấy danh sách tasks tương ứng với mỗi project từ API backend.

## Các File Đã Tạo/Cập Nhật

### 1. **TaskApiService.java** (Mới)
- **Đường dẫn**: `network/api/TaskApiService.java`
- **Mục đích**: Interface Retrofit để gọi API lấy tasks
- **Endpoints**:
  - `GET /tasks?project_id={id}&status={status}` - Lấy tasks theo project và status
  - `GET /tasks?project_id={id}` - Lấy tất cả tasks của project

### 2. **ListProjectViewModel.java** (Mới)
- **Đường dẫn**: `feature/home/ui/Home/project/ListProjectViewModel.java`
- **Mục đích**: ViewModel quản lý logic business và data
- **Tính năng**:
  - LiveData cho danh sách tasks
  - LiveData cho trạng thái loading
  - LiveData cho error messages
  - Methods: `loadTasks(projectId, status)` và `loadAllTasks(projectId)`

### 3. **TaskAdapter.java** (Mới)
- **Đường dẫn**: `feature/home/ui/Home/project/TaskAdapter.java`
- **Mục đích**: RecyclerView Adapter để hiển thị danh sách tasks
- **Hiển thị**:
  - Task title (bắt buộc)
  - Task description (optional)
  - Priority với màu sắc khác nhau:
    - HIGH → Đỏ
    - MEDIUM → Cam
    - LOW → Xanh lá
  - Due date (optional)

### 4. **item_task.xml** (Mới)
- **Đường dẫn**: `res/layout/item_task.xml`
- **Mục đích**: Layout cho mỗi task item trong RecyclerView
- **Thiết kế**: CardView với các TextView cho title, description, priority, và due date

### 5. **ListProject.java** (Cập nhật)
- **Đường dẫn**: `feature/home/ui/Home/project/ListProject.java`
- **Thay đổi**:
  - Thêm support cho `projectId` parameter
  - Tích hợp ViewModel
  - Sử dụng TaskAdapter thay vì SimpleAdapter
  - Map status từ UI ("TO DO", "IN PROGRESS", "DONE") sang API format ("TO_DO", "IN_PROGRESS", "DONE")
  - Thêm error handling và loading states
  - Thêm method `refreshTasks()` để refresh data

### 6. **ListProjectAdapter.java** (Cập nhật)
- **Đường dẫn**: `feature/home/ui/Home/project/ListProjectAdapter.java`
- **Thay đổi**:
  - Thêm field `projectId`
  - Thêm constructor nhận `projectId`
  - Thêm method `setProjectId()` để cập nhật projectId
  - Truyền projectId vào mỗi ListProject fragment

## Cách Sử Dụng

### Trong Activity (ví dụ: ProjectDetailActivity)

```java
// Tạo adapter với projectId
String projectId = "your-project-id-here";
ListProjectAdapter adapter = new ListProjectAdapter(this, projectId);

// Hoặc set projectId sau
ListProjectAdapter adapter = new ListProjectAdapter(this);
adapter.setProjectId(projectId);

// Set adapter cho ViewPager2
ViewPager2 viewPager = findViewById(R.id.viewPager);
viewPager.setAdapter(adapter);

// Setup TabLayout
TabLayout tabLayout = findViewById(R.id.tabLayout);
new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
    switch (position) {
        case 0:
            tab.setText("TO DO");
            break;
        case 1:
            tab.setText("IN PROGRESS");
            break;
        case 2:
            tab.setText("DONE");
            break;
    }
}).attach();
```

### Refresh Tasks

```java
// Nếu cần refresh tasks sau khi update
// Bạn có thể tạo interface callback hoặc sử dụng event bus
// Hoặc access fragment trực tiếp:
Fragment fragment = getSupportFragmentManager()
    .findFragmentByTag("f" + viewPager.getCurrentItem());
    
if (fragment instanceof ListProject) {
    ((ListProject) fragment).refreshTasks();
}
```

## API Contract Cần Có

Backend cần implement các endpoints sau:

### GET /tasks
**Query Parameters:**
- `project_id` (required): ID của project
- `status` (optional): Status của task (TO_DO, IN_PROGRESS, DONE)

**Response:**
```json
[
  {
    "id": "uuid",
    "project_id": "uuid",
    "board_id": "uuid",
    "title": "Task title",
    "description": "Task description",
    "status": "TO_DO",
    "priority": "HIGH",
    "due_at": "2025-10-15T10:00:00Z",
    "assignee_id": "uuid",
    "created_at": "2025-10-08T10:00:00Z"
  }
]
```

## Lưu Ý

1. **Project ID**: Đảm bảo truyền đúng projectId khi tạo ListProjectAdapter
2. **API Base URL**: Kiểm tra ApiClient đã cấu hình đúng base URL
3. **Authentication**: Ensure App.authManager đã được initialize và có valid token
4. **Error Handling**: Hiện tại errors được hiển thị qua Toast, có thể customize thêm
5. **Loading State**: ProgressBar chưa được implement trong UI, có thể thêm vào layout nếu cần
6. **Empty State**: Chưa có empty state view, có thể thêm TextView để hiển thị khi không có tasks

## Tính Năng Có Thể Mở Rộng

1. **Pull-to-refresh**: Thêm SwipeRefreshLayout
2. **Pagination**: Implement paging cho danh sách tasks dài
3. **Search/Filter**: Thêm search và filter tasks
4. **Drag & Drop**: Cho phép kéo thả tasks giữa các status
5. **Task Detail**: Navigate đến màn hình chi tiết task khi click
6. **Offline Support**: Cache tasks với Room database
7. **Real-time Updates**: Sử dụng WebSocket hoặc Firebase để sync real-time

## Testing

Để test tính năng:

1. Tạo một project với ID hợp lệ
2. Thêm một số tasks vào project đó trong database
3. Mở project detail activity với projectId
4. Kiểm tra xem tasks hiển thị đúng ở các tab tương ứng với status
5. Verify API calls trong Logcat (tag: "ListProjectViewModel")

## Troubleshooting

- **Không load được tasks**: Kiểm tra Logcat xem có error gì, verify API endpoint và authentication
- **Tasks hiển thị sai tab**: Kiểm tra status mapping trong `loadTasksForStatus()`
- **Crash khi click task**: Implement proper navigation trong TaskAdapter's click listener
