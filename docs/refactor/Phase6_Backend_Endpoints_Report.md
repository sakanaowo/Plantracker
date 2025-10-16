# BÁO CÁO BỔ SUNG BACKEND ENDPOINTS - PHASE 6
**Ngày:** 16/10/2025  
**Người tạo:** System Analysis  
**Mục đích:** Hỗ trợ Frontend demo đầy đủ tính năng

---

## 📊 TỔNG QUAN

### Endpoints hiện có: ✅ 26 endpoints
### Endpoints cần bổ sung: ⚠️ 15 endpoints
### Độ ưu tiên: 🔴 HIGH (cho demo), 🟡 MEDIUM (nice to have)

---

## 🔴 PRIORITY 1: CẦN THIẾT CHO DEMO (7 endpoints)

### 1. **Get Workspace Projects** ✅ ĐÃ CÓ
```
GET /api/projects?workspaceId={id}
```
**Status:** Có sẵn, không cần thêm

### 2. **Get Project Boards** ✅ ĐÃ CÓ
```
GET /api/boards?projectId={id}
```
**Status:** Có sẵn, không cần thêm

### 3. **Task Comments** - CẦN BỔ SUNG

#### 3.1 Get Task Comments
```typescript
GET /api/tasks/{taskId}/comments

Response: {
  "comments": [
    {
      "id": "uuid",
      "task_id": "uuid",
      "user_id": "uuid",
      "body": "This is a comment",
      "created_at": "2025-10-16T...",
      "user": {
        "id": "uuid",
        "name": "John Doe",
        "avatar_url": "https://..."
      }
    }
  ]
}
```

**Backend Implementation:**
```typescript
// tasks.controller.ts
@Get(':id/comments')
async getTaskComments(@Param('id') taskId: string) {
  const comments = await this.prisma.task_comments.findMany({
    where: { task_id: taskId },
    include: {
      users: {
        select: {
          id: true,
          name: true,
          avatar_url: true
        }
      }
    },
    orderBy: { created_at: 'desc' }
  });
  
  return { comments };
}
```

#### 3.2 Create Task Comment
```typescript
POST /api/tasks/{taskId}/comments

Body: {
  "body": "Comment text here"
}

Response: {
  "comment": {
    "id": "uuid",
    "task_id": "uuid",
    "user_id": "uuid",
    "body": "Comment text here",
    "created_at": "2025-10-16T..."
  }
}
```

**Backend Implementation:**
```typescript
// tasks.controller.ts
@Post(':id/comments')
async createTaskComment(
  @Param('id') taskId: string,
  @Body() dto: CreateCommentDto,
  @CurrentUser() user: User
) {
  // Verify task exists and user has access
  const task = await this.prisma.tasks.findUnique({
    where: { id: taskId }
  });
  
  if (!task) {
    throw new NotFoundException('Task not found');
  }
  
  const comment = await this.prisma.task_comments.create({
    data: {
      task_id: taskId,
      user_id: user.id,
      body: dto.body
    }
  });
  
  return { comment };
}
```

### 4. **User Search for Assignment** - CẦN BỔ SUNG

```typescript
GET /api/workspaces/{workspaceId}/members

Response: {
  "members": [
    {
      "id": "uuid",
      "name": "John Doe",
      "email": "john@example.com",
      "avatar_url": "https://...",
      "role": "MEMBER"
    }
  ]
}
```

**Backend Implementation:**
```typescript
// workspaces.controller.ts
@Get(':id/members')
async getWorkspaceMembers(@Param('id') workspaceId: string) {
  const members = await this.prisma.memberships.findMany({
    where: { workspace_id: workspaceId },
    include: {
      users: {
        select: {
          id: true,
          name: true,
          email: true,
          avatar_url: true
        }
      }
    }
  });
  
  return {
    members: members.map(m => ({
      ...m.users,
      role: m.role
    }))
  };
}
```

**Note:** Endpoint này ĐÃ CÓ trong api-endpoints.md:
```
GET /api/workspaces/{id}/members
```
Cần verify implementation!

### 5. **Assign/Unassign Task** - CẦN BỔ SUNG

```typescript
PATCH /api/tasks/{taskId}/assign

Body: {
  "assigneeId": "uuid" // or null to unassign
}

Response: {
  "task": { ...updated task... }
}
```

**Backend Implementation:**
```typescript
// tasks.controller.ts
@Patch(':id/assign')
async assignTask(
  @Param('id') taskId: string,
  @Body() dto: AssignTaskDto
) {
  const task = await this.prisma.tasks.update({
    where: { id: taskId },
    data: { assignee_id: dto.assigneeId }
  });
  
  return { task };
}
```

---

## 🟡 PRIORITY 2: NICE TO HAVE (8 endpoints)

### 1. **Task Attachments**

```typescript
GET  /api/tasks/{taskId}/attachments
POST /api/tasks/{taskId}/attachments
DELETE /api/attachments/{id}
```

**Implementation:**
```typescript
// tasks.controller.ts
@Get(':id/attachments')
async getAttachments(@Param('id') taskId: string) {
  const attachments = await this.prisma.attachments.findMany({
    where: { task_id: taskId },
    orderBy: { created_at: 'desc' }
  });
  
  return { attachments };
}

@Post(':id/attachments')
async addAttachment(
  @Param('id') taskId: string,
  @Body() dto: CreateAttachmentDto,
  @CurrentUser() user: User
) {
  const attachment = await this.prisma.attachments.create({
    data: {
      task_id: taskId,
      url: dto.url,
      mime_type: dto.mimeType,
      size: dto.size,
      uploaded_by: user.id
    }
  });
  
  return { attachment };
}
```

### 2. **Checklists**

```typescript
GET    /api/tasks/{taskId}/checklists
POST   /api/tasks/{taskId}/checklists
PATCH  /api/checklist-items/{itemId}
DELETE /api/checklists/{id}
```

**Implementation:**
```typescript
@Get(':id/checklists')
async getChecklists(@Param('id') taskId: string) {
  const checklists = await this.prisma.checklists.findMany({
    where: { task_id: taskId },
    include: {
      checklist_items: {
        orderBy: { position: 'asc' }
      }
    }
  });
  
  return { checklists };
}

@Post(':id/checklists')
async createChecklist(
  @Param('id') taskId: string,
  @Body() dto: CreateChecklistDto
) {
  const checklist = await this.prisma.checklists.create({
    data: {
      task_id: taskId,
      title: dto.title
    }
  });
  
  // Create items if provided
  if (dto.items && dto.items.length > 0) {
    await this.prisma.checklist_items.createMany({
      data: dto.items.map((item, index) => ({
        checklist_id: checklist.id,
        content: item.content,
        position: index
      }))
    });
  }
  
  return { checklist };
}
```

### 3. **Labels Management**

```typescript
GET    /api/workspaces/{workspaceId}/labels
POST   /api/workspaces/{workspaceId}/labels
PATCH  /api/labels/{id}
DELETE /api/labels/{id}
```

### 4. **Task Labels**

```typescript
POST   /api/tasks/{taskId}/labels/{labelId}
DELETE /api/tasks/{taskId}/labels/{labelId}
```

---

## 🔧 DTO DEFINITIONS (Backend)

### CreateCommentDto
```typescript
export class CreateCommentDto {
  @IsString()
  @MinLength(1)
  @MaxLength(5000)
  body: string;
}
```

### AssignTaskDto
```typescript
export class AssignTaskDto {
  @IsOptional()
  @IsUUID()
  assigneeId?: string | null;
}
```

### CreateAttachmentDto
```typescript
export class CreateAttachmentDto {
  @IsUrl()
  url: string;
  
  @IsOptional()
  @IsString()
  mimeType?: string;
  
  @IsOptional()
  @IsInt()
  size?: number;
}
```

### CreateChecklistDto
```typescript
export class CreateChecklistDto {
  @IsString()
  @MinLength(1)
  @MaxLength(200)
  title: string;
  
  @IsOptional()
  @IsArray()
  items?: {
    content: string;
  }[];
}
```

---

## 📱 ANDROID - API SERVICE INTERFACES

Sau khi backend bổ sung endpoints, cần update các file sau:

### 1. **TaskApiService.java**

```java
public interface TaskApiService {
    // Existing methods...
    
    // Comments
    @GET("tasks/{taskId}/comments")
    Call<CommentListResponse> getTaskComments(@Path("taskId") String taskId);
    
    @POST("tasks/{taskId}/comments")
    Call<CommentResponse> createComment(
        @Path("taskId") String taskId,
        @Body CreateCommentRequest request
    );
    
    // Assignment
    @PATCH("tasks/{taskId}/assign")
    Call<TaskResponse> assignTask(
        @Path("taskId") String taskId,
        @Body AssignTaskRequest request
    );
    
    // Attachments
    @GET("tasks/{taskId}/attachments")
    Call<AttachmentListResponse> getAttachments(@Path("taskId") String taskId);
    
    @POST("tasks/{taskId}/attachments")
    Call<AttachmentResponse> addAttachment(
        @Path("taskId") String taskId,
        @Body CreateAttachmentRequest request
    );
    
    // Checklists
    @GET("tasks/{taskId}/checklists")
    Call<ChecklistListResponse> getChecklists(@Path("taskId") String taskId);
    
    @POST("tasks/{taskId}/checklists")
    Call<ChecklistResponse> createChecklist(
        @Path("taskId") String taskId,
        @Body CreateChecklistRequest request
    );
}
```

### 2. **WorkspaceApiService.java**

```java
public interface WorkspaceApiService {
    // Existing methods...
    
    // Members (verify if already exists)
    @GET("workspaces/{id}/members")
    Call<MemberListResponse> getWorkspaceMembers(@Path("id") String workspaceId);
    
    @POST("workspaces/{id}/members")
    Call<MemberResponse> addMember(
        @Path("id") String workspaceId,
        @Body AddMemberRequest request
    );
    
    @DELETE("workspaces/{id}/members/{userId}")
    Call<Void> removeMember(
        @Path("id") String workspaceId,
        @Path("userId") String userId
    );
}
```

---

## 📦 DOMAIN MODELS CẦN BỔ SUNG (Android)

### 1. **Comment.java**

```java
package com.example.tralalero.domain.model;

public class Comment {
    private final String id;
    private final String taskId;
    private final String userId;
    private final String body;
    private final String userName;
    private final String userAvatarUrl;
    private final String createdAt;
    
    public Comment(String id, String taskId, String userId, String body,
                   String userName, String userAvatarUrl, String createdAt) {
        this.id = id;
        this.taskId = taskId;
        this.userId = userId;
        this.body = body;
        this.userName = userName;
        this.userAvatarUrl = userAvatarUrl;
        this.createdAt = createdAt;
    }
    
    // Getters...
    public String getId() { return id; }
    public String getTaskId() { return taskId; }
    public String getUserId() { return userId; }
    public String getBody() { return body; }
    public String getUserName() { return userName; }
    public String getUserAvatarUrl() { return userAvatarUrl; }
    public String getCreatedAt() { return createdAt; }
}
```

### 2. **Attachment.java**

```java
package com.example.tralalero.domain.model;

public class Attachment {
    private final String id;
    private final String taskId;
    private final String url;
    private final String mimeType;
    private final Integer size;
    private final String uploadedBy;
    private final String createdAt;
    
    // Constructor & Getters...
}
```

### 3. **Checklist.java**

```java
package com.example.tralalero.domain.model;

import java.util.List;

public class Checklist {
    private final String id;
    private final String taskId;
    private final String title;
    private final List<ChecklistItem> items;
    private final String createdAt;
    
    // Constructor & Getters...
}
```

### 4. **ChecklistItem.java**

```java
package com.example.tralalero.domain.model;

public class ChecklistItem {
    private final String id;
    private final String checklistId;
    private final String content;
    private final boolean isDone;
    private final int position;
    
    // Constructor & Getters...
}
```

### 5. **Member.java**

```java
package com.example.tralalero.domain.model;

public class Member {
    private final String id;
    private final String name;
    private final String email;
    private final String avatarUrl;
    private final String role;
    
    // Constructor & Getters...
}
```

---

## 🔄 REPOSITORY UPDATES

### TaskRepository Interface

```java
public interface ITaskRepository {
    // Existing methods...
    
    // Comments
    Result<List<Comment>> getTaskComments(String taskId);
    Result<Comment> addComment(String taskId, String body);
    
    // Assignment
    Result<Task> assignTask(String taskId, String assigneeId);
    Result<Task> unassignTask(String taskId);
    
    // Attachments
    Result<List<Attachment>> getAttachments(String taskId);
    Result<Attachment> addAttachment(String taskId, String url, String mimeType, Integer size);
    
    // Checklists
    Result<List<Checklist>> getChecklists(String taskId);
    Result<Checklist> createChecklist(String taskId, String title, List<String> items);
    Result<ChecklistItem> toggleChecklistItem(String itemId, boolean isDone);
}
```

---

## 📅 TIMELINE IMPLEMENTATION

### Backend (1-2 giờ):
1. **30 phút**: Implement Task Comments (GET, POST)
2. **20 phút**: Implement Assign/Unassign Task
3. **20 phút**: Verify Workspace Members endpoint
4. **30 phút**: Implement Attachments (optional)
5. **20 phút**: Testing với Postman

### Android (1 giờ):
1. **20 phút**: Tạo domain models (Comment, Attachment, etc.)
2. **20 phút**: Update API services
3. **20 phút**: Update repositories

---

## 🧪 TESTING CHECKLIST

### Backend Testing (Postman):
- [ ] GET /api/tasks/{id}/comments → Returns empty array
- [ ] POST /api/tasks/{id}/comments → Creates comment
- [ ] GET /api/tasks/{id}/comments → Returns new comment
- [ ] PATCH /api/tasks/{id}/assign → Assigns user
- [ ] GET /api/tasks/{id} → Shows assignee_id
- [ ] GET /api/workspaces/{id}/members → Returns members list

### Android Testing:
- [ ] TaskRepository.getTaskComments() works
- [ ] TaskRepository.addComment() works
- [ ] TaskRepository.assignTask() works
- [ ] Display comments in UI
- [ ] Add comment từ UI

---

## 📝 SUMMARY

### Endpoints ưu tiên cao (CHO DEMO):
1. ✅ Task Comments (GET, POST) - **BẮT BUỘC**
2. ✅ Assign Task (PATCH) - **BẮT BUỘC**
3. ✅ Workspace Members (GET) - **CẦN VERIFY**

### Endpoints có thể bỏ qua (cho sau):
- Attachments
- Checklists
- Labels
- Advanced filtering

### Tổng thời gian estimate:
- **Backend**: 1-2 giờ
- **Android**: 1 giờ
- **Total**: 2-3 giờ

---

**Recommendation:** Tập trung vào 3 endpoints ưu tiên cao trước, demo được comment & assignment là đủ ấn tượng!

