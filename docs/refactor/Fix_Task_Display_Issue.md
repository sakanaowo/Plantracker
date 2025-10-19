# Tóm Tắt Những Thay Đổi Để Fix Lỗi

## Vấn đề
App hiển thị "All item 1", "All item 2" thay vì tasks thực từ API.

## Nguyên nhân
1. `ProjectActivity` đang dùng `ListFrmAdapter` (cũ) thay vì `ListProjectAdapter` (mới)
2. Không truyền `project_id` từ WorkspaceActivity sang ProjectActivity
3. Key của Intent extras không khớp nhau (PROJECT_ID vs project_id)

## Các file đã sửa

### 1. ProjectActivity.java
**Thay đổi:**
- Import: `ListFrmAdapter` → `ListProjectAdapter`
- Lấy `project_id` từ Intent
- Tạo adapter với projectId: `new ListProjectAdapter(this, projectId)`

### 2. WorkspaceActivity.java
**Thay đổi:**
- Intent extras: `PROJECT_ID` → `project_id` (chữ thường)
- Intent extras: `PROJECT_NAME` → `project_name` (chữ thường)
- Thêm `workspace_id` vào Intent

### 3. TaskAdapter.java (trong folder adapter)
**Thay đổi:**
- Package: `com.example.tralalero.feature.home.ui.Home.project` → `com.example.tralalero.adapter`

## Kết quả mong đợi

Khi click vào một project:
1. WorkspaceActivity truyền `project_id` sang ProjectActivity
2. ProjectActivity tạo ListProjectAdapter với projectId
3. Mỗi tab (TO DO, IN PROGRESS, DONE) sẽ:
   - Tạo ListProject fragment với projectId và status tương ứng
   - Fragment gọi API: `GET /tasks?project_id={id}&status={status}`
   - Hiển thị danh sách tasks thực từ API

## Để test

1. Build lại project trong Android Studio (Ctrl + F9)
2. Chạy app
3. Click vào một project
4. Kiểm tra 3 tabs xem có hiển thị tasks từ API không
5. Xem Logcat với tag "ListProjectViewModel" để xem API calls
