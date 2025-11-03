# Báo Cáo So Sánh Database Layer - Android vs Backend

**Ngày tạo:** 2025-10-19  
**Mục đích:** Kiểm tra tính khớp logic giữa Room Database (Android) và PostgreSQL Schema (Backend)

---

## 📊 TỔNG QUAN

### Backend Database (PostgreSQL - Prisma Schema)
- **Số lượng tables:** 20+ tables
- **Kiểu dữ liệu chính:** UUID, Timestamptz, Enums
- **Relationships:** Foreign keys với CASCADE delete

### Android Database (Room)
- **Số lượng entities:** 5 entities
- **Version:** 4 (với migration 3→4)
- **Mục đích:** Local cache cho offline support

---

## ✅ ĐÁNH GIÁ CHI TIẾT TỪNG ENTITY

### 1. **TaskEntity** ⚠️ THIẾU NHIỀU TRƯỜNG

#### Backend Schema (`tasks` table):
```prisma
model tasks {
  id                      String      @id @db.Uuid
  project_id              String      @db.Uuid
  board_id                String      @db.Uuid
  title                   String
  description             String?
  assignee_id             String?     @db.Uuid
  created_by              String?     @db.Uuid
  due_at                  DateTime?   @db.Timestamptz(6)
  start_at                DateTime?   @db.Timestamptz(6)
  priority                priority?   (ENUM)
  position                Decimal     @db.Decimal(10, 3)
  issue_key               String?     @unique @db.VarChar(32)
  type                    issue_type? (ENUM)
  status                  issue_status? (ENUM) @default(TO_DO)
  sprint_id               String?     @db.Uuid
  epic_id                 String?     @db.Uuid
  parent_task_id          String?     @db.Uuid
  story_points            Int?
  original_estimate_sec   Int?
  remaining_estimate_sec  Int?
  created_at              DateTime    @default(now())
  updated_at              DateTime    @default(now())
  deleted_at              DateTime?   (SOFT DELETE)
  
  // Relations
  attachments[]
  checklists[]
  issue_links[]
  task_comments[]
  task_labels[]
  time_entries[]
  watchers[]
}
```

#### Android Entity (`TaskEntity.java`):
```java
✅ id                     - String
✅ projectId              - String
✅ boardId                - String
✅ title                  - String
✅ description            - String
✅ issueKey               - String
✅ type                   - String
✅ status                 - String
✅ priority               - String
✅ position               - double
✅ assigneeId             - String
✅ createdBy              - String
✅ sprintId               - String
✅ epicId                 - String
✅ parentTaskId           - String
✅ startAt                - Date
✅ dueAt                  - Date
✅ storyPoints            - Integer
✅ originalEstimateSec    - Integer
✅ remainingEstimateSec   - Integer
✅ createdAt              - Date
✅ updatedAt              - Date

❌ THIẾU: deleted_at      - Date (SOFT DELETE field)
```

**Kết luận:** ✅ **CƠ BẢN KHỚP** - Chỉ thiếu `deleted_at` cho soft delete

---

### 2. **ProjectEntity** ⚠️ THIẾU TRƯỜNG QUAN TRỌNG

#### Backend Schema (`projects` table):
```prisma
model projects {
  id           String     @id @db.Uuid
  workspace_id String     @db.Uuid
  name         String
  description  String?
  key          String?    @db.VarChar(10)
  issue_seq    Int        @default(0)          ← THIẾU
  board_type   String     @default("KANBAN")
  created_at   DateTime   @default(now())      ← THIẾU
  updated_at   DateTime   @default(now())      ← THIẾU
}
```

#### Android Entity (`ProjectEntity.java`):
```java
✅ id           - String
✅ workspaceId  - String
✅ name         - String
✅ description  - String
✅ key          - String
✅ boardType    - String

❌ THIẾU: issue_seq    - Int (auto-increment cho issue key)
❌ THIẾU: created_at   - Date
❌ THIẾU: updated_at   - Date
```

**Kết luận:** ⚠️ **THIẾU TRƯỜNG** - Cần thêm `issue_seq`, `created_at`, `updated_at`

---

### 3. **WorkspaceEntity** ❌ SAI THIẾT KẾ NGHIÊM TRỌNG

#### Backend Schema (`workspaces` table):
```prisma
model workspaces {
  id          String         @id @db.Uuid
  name        String
  owner_id    String         @db.Uuid         ← SAI: Android dùng userId
  type        workspace_type @default(TEAM)   ← THIẾU HOÀN TOÀN
  created_at  DateTime       @default(now())
  updated_at  DateTime       @default(now())
}

enum workspace_type {
  PERSONAL
  TEAM
}
```

#### Android Entity (`WorkspaceEntity.java`):
```java
✅ id           - String
✅ name         - String
❌ userId       - String    (BACKEND: owner_id)
❌ THIẾU: type  - String    (workspace_type enum)
✅ description  - String    (KHÔNG CÓ Ở BACKEND)
✅ created_at   - Date
✅ updated_at   - Date
```

**Kết luận:** ❌ **SAI NGHIÊM TRỌNG** 
- Field name sai: `userId` → `ownerId`
- Thiếu field: `type` (PERSONAL/TEAM)
- Thừa field: `description` (không có ở backend)

---

### 4. **BoardEntity** ✅ KHỚP HOÀN TOÀN

#### Backend Schema (`boards` table):
```prisma
model boards {
  id         String   @id @db.Uuid
  project_id String   @db.Uuid
  name       String
  order      Int
  created_at DateTime @default(now())
  updated_at DateTime @default(now())
}
```

#### Android Entity (`BoardEntity.java`):
```java
✅ id         - String
✅ projectId  - String
✅ name       - String
✅ order      - int
✅ createdAt  - Date
✅ updatedAt  - Date
```

**Kết luận:** ✅ **HOÀN TOÀN KHỚP**

---

### 5. **CacheMetadata** ✅ LOCAL ONLY (OK)

```java
// Android only - for TTL cache management
✅ cacheKey     - String
✅ lastUpdated  - long
✅ itemCount    - int
```

**Kết luận:** ✅ **OK** - Đây là table local cho cache management, không sync với backend

---

## 🚨 CÁC VẤN ĐỀ NGHIÊM TRỌNG CẦN FIX

### ❌ **CRITICAL ISSUE #1: WorkspaceEntity sai thiết kế**
```
BACKEND:  owner_id + type (PERSONAL/TEAM)
ANDROID:  userId + description
```
**Impact:** Không sync được workspace từ API

**Fix Required:**
1. Rename `userId` → `ownerId`
2. Add field `type` (String)
3. Remove field `description` (hoặc để null, không map từ API)

---

### ⚠️ **ISSUE #2: ProjectEntity thiếu issue_seq**
```
BACKEND:  issue_seq (Int) - dùng để generate issue key
ANDROID:  KHÔNG CÓ
```
**Impact:** Không thể generate issue key offline

**Fix Required:**
Add field `issueSeq` (int) với default = 0

---

### ⚠️ **ISSUE #3: ProjectEntity thiếu timestamps**
```
BACKEND:  created_at, updated_at
ANDROID:  KHÔNG CÓ
```
**Impact:** Không track được thời gian tạo/cập nhật project

**Fix Required:**
Add fields `createdAt` và `updatedAt`

---

### ⚠️ **ISSUE #4: TaskEntity thiếu deleted_at**
```
BACKEND:  deleted_at (DateTime?) - soft delete
ANDROID:  KHÔNG CÓ
```
**Impact:** Không hỗ trợ soft delete, tasks bị xóa vĩnh viễn

**Fix Required:**
Add field `deletedAt` (Date, nullable)

---

## 📋 CÁC TABLE BACKEND CHƯA CÓ Ở ANDROID

### Quan trọng (nên implement):
1. ❌ **users** - User profile info
2. ❌ **memberships** - Workspace members & roles
3. ❌ **labels** - Task labels
4. ❌ **task_labels** - Task-Label mapping
5. ❌ **sprints** - Sprint management (Scrum)
6. ❌ **checklists** - Task checklists
7. ❌ **checklist_items** - Checklist items
8. ❌ **task_comments** - Task comments
9. ❌ **attachments** - File attachments

### Ít quan trọng (có thể bỏ qua):
10. ⚠️ events - Calendar events
11. ⚠️ participants - Event participants
12. ⚠️ time_entries - Time tracking
13. ⚠️ watchers - Task watchers
14. ⚠️ issue_links - Task relationships
15. ⚠️ notifications - Push notifications
16. ⚠️ user_devices - FCM tokens
17. ⚠️ integration_tokens - OAuth tokens
18. ⚠️ email_queue - Email system

---

## 🔧 KHUYẾN NGHỊ FIX

### Priority 1 - CRITICAL (Phải fix ngay):
1. **Fix WorkspaceEntity**
   - Rename `userId` → `ownerId`
   - Add field `type` (String)
   - Database migration required

2. **Fix ProjectEntity**
   - Add `issueSeq` (int)
   - Add `createdAt`, `updatedAt` (Date)
   - Database migration required

### Priority 2 - Important (Nên fix):
3. **Fix TaskEntity**
   - Add `deletedAt` (Date) cho soft delete
   - Database migration required

4. **Add Essential Entities**
   - UserEntity (basic profile cache)
   - LabelEntity + TaskLabelEntity
   - SprintEntity (cho Scrum support)

### Priority 3 - Nice to have:
5. **Add ChecklistEntity & ChecklistItemEntity**
6. **Add TaskCommentEntity**
7. **Add AttachmentEntity**

---

## 📦 MIGRATION PLAN

### Migration 4 → 5: Fix WorkspaceEntity
```sql
ALTER TABLE workspaces RENAME COLUMN userId TO ownerId;
ALTER TABLE workspaces ADD COLUMN type TEXT NOT NULL DEFAULT 'TEAM';
-- Keep description for backward compatibility (can be null)
```

### Migration 5 → 6: Fix ProjectEntity
```sql
ALTER TABLE projects ADD COLUMN issue_seq INTEGER NOT NULL DEFAULT 0;
ALTER TABLE projects ADD COLUMN created_at INTEGER NOT NULL DEFAULT 0;
ALTER TABLE projects ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0;
```

### Migration 6 → 7: Fix TaskEntity
```sql
ALTER TABLE tasks ADD COLUMN deleted_at INTEGER;
```

---

## ✅ KẾT LUẬN

### Tổng quan tình trạng:
- ✅ **BoardEntity**: Hoàn hảo
- ✅ **TaskEntity**: Tốt (chỉ thiếu soft delete)
- ⚠️ **ProjectEntity**: Thiếu 3 fields quan trọng
- ❌ **WorkspaceEntity**: Sai thiết kế, không khớp API
- ✅ **CacheMetadata**: OK (local only)

### Độ ưu tiên:
1. **CRITICAL**: Fix WorkspaceEntity trước (blocking API sync)
2. **HIGH**: Fix ProjectEntity (cần cho issue generation)
3. **MEDIUM**: Add core entities (Users, Labels, Sprints)
4. **LOW**: Add advanced features (Comments, Attachments, etc.)

### Trạng thái hiện tại:
**Database layer CÓ THỂ HOẠT ĐỘNG** nhưng **KHÔNG HOÀN TOÀN KHỚP VỚI BACKEND**.
Cần fix các critical issues để đảm bảo sync API hoạt động đúng.

