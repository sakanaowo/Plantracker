# 📊 BÁO CÁO: Tình trạng Backend cho Chức năng Invite Member vào Project

**Ngày:** October 26, 2025  
**Dự án:** Plantracker  
**Người kiểm tra:** AI Assistant  
**Branch:** fe2

---

## 🎯 TÓM TẮT NHANH

| Tiêu chí                  | Trạng thái           | Ghi chú                         |
| ------------------------- | -------------------- | ------------------------------- |
| **Workspace Members API** | ✅ Đã có             | Hoàn chỉnh, có thể dùng ngay    |
| **Project Members API**   | ❌ Chưa có           | Cần implement từ đầu            |
| **Database Schema**       | ❌ Thiếu             | Không có bảng `project_members` |
| **Search User API**       | ❌ Thiếu             | Cần để tìm user khi invite      |
| **Kết luận**              | ❌ **CHƯA SẴN SÀNG** | Cần 6-8 giờ để hoàn thiện       |

---

## 📋 MỤC LỤC

1. [Kiểm tra Workspace Members API](#1-kiểm-tra-workspace-members-api)
2. [Kiểm tra Project Members API](#2-kiểm-tra-project-members-api)
3. [Kiểm tra Database Schema](#3-kiểm-tra-database-schema)
4. [Kiểm tra Users API](#4-kiểm-tra-users-api)
5. [Các Giải pháp Đề xuất](#5-các-giải-pháp-đề-xuất)
6. [Checklist Implementation](#6-checklist-implementation)
7. [Kết luận & Khuyến nghị](#7-kết-luận--khuyến-nghị)

---

## 1. KIỂM TRA WORKSPACE MEMBERS API

### ✅ **Trạng thái: ĐÃ CÓ SẴN**

Backend đã implement đầy đủ API để quản lý members ở level workspace:

### **Endpoints:**

#### 1.1. Thêm Member

```typescript
POST /workspaces/:id/members
Authorization: Bearer {token}

Body:
{
  "userId": "uuid",
  "role": "MEMBER" | "ADMIN" | "OWNER"
}

Response:
{
  "id": "uuid",
  "user_id": "uuid",
  "workspace_id": "uuid",
  "role": "MEMBER",
  "created_at": "2025-10-26T..."
}
```

#### 1.2. Danh sách Members

```typescript
GET /workspaces/:id/members
Authorization: Bearer {token}

Response:
[
  {
    "id": "uuid",
    "user_id": "uuid",
    "workspace_id": "uuid",
    "role": "OWNER",
    "created_at": "2025-10-26T..."
  }
]
```

#### 1.3. Xóa Member

```typescript
DELETE /workspaces/:id/members/:userId
Authorization: Bearer {token}

Response: 204 No Content
```

### **Source Code:**

**Controller:** `src/modules/workspaces/workspaces.controller.ts`

```typescript
@Post(':id/members')
addMember(
  @CurrentUser('id') userId: string,
  @Param('id') id: string,
  @Body() dto: AddMemberDto,
) {
  return this.service.addMember(id, userId, dto);
}

@Get(':id/members')
listMembers(@CurrentUser('id') userId: string, @Param('id') id: string) {
  return this.service.listMembers(id, userId);
}

@Delete(':id/members/:userId')
removeMember(
  @CurrentUser('id') actorId: string,
  @Param('id') workspaceId: string,
  @Param('userId') targetUserId: string,
) {
  return this.service.removeMember(workspaceId, actorId, targetUserId);
}
```

**Service:** `src/modules/workspaces/workspaces.service.ts`

```typescript
async addMember(workspaceId: string, userId: string, dto: AddMemberDto) {
  await this.ensureOwnerOfWorkspace(workspaceId, userId);
  try {
    return await this.prisma.memberships.create({
      data: {
        workspace_id: workspaceId,
        user_id: dto.userId,
        role: dto.role,
      },
    });
  } catch (error: any) {
    if (error?.code === 'P2002')
      throw new ConflictException('User is already a member of the workspace');
    throw error;
  }
}
```

**DTO:** `src/modules/workspaces/dto/add-member.dto.ts`

```typescript
export class AddMemberDto {
  @IsUUID()
  userId: string;

  @IsEnum(role)
  role!: role; //owner | admin | member
}
```

### **Permission System:**

- ✅ Chỉ **OWNER** mới có quyền add member
- ✅ **ADMIN** có thể remove MEMBER (không remove được ADMIN/OWNER)
- ✅ Có validation duplicate member
- ✅ Có error handling đầy đủ

### **Ưu điểm:**

- Implementation hoàn chỉnh, production-ready
- Có đầy đủ validation và permission check
- Error handling tốt

### **Nhược điểm:**

- Chỉ hoạt động ở level workspace
- User trong workspace → truy cập **TẤT CẢ** projects
- Không phân quyền chi tiết per project

---

## 2. KIỂM TRA PROJECT MEMBERS API

### ❌ **Trạng thái: CHƯA CÓ**

Backend **KHÔNG CÓ** bất kỳ API nào để quản lý members ở level project.

### **Projects Controller hiện tại:**

**File:** `src/modules/projects/projects.controller.ts`

```typescript
@Controller("projects")
export class ProjectsController {
  constructor(private readonly svc: ProjectsService) {}

  @Get()
  list(@Query("workspaceId") workspaceId: string) {
    return this.svc.listByWorkSpace(workspaceId);
  }

  @Post()
  create(@Body() dto: CreateProjectDto) {
    return this.svc.create(dto);
  }

  @Patch(":id")
  update(@Param("id") id: string, @Body() dto: UpdateProjectDto) {
    return this.svc.update(id, dto);
  }
}
```

### **Thiếu Endpoints:**

- ❌ `POST /projects/:id/members` - Add member to project
- ❌ `GET /projects/:id/members` - List project members
- ❌ `DELETE /projects/:id/members/:userId` - Remove member from project

### **Projects Service hiện tại:**

**File:** `src/modules/projects/projects.service.ts`

Chỉ có methods:

- `listByWorkSpace(workspaceId: string)`
- `create(dto: CreateProjectDto)`
- `update(id: string, dto: UpdateProjectDto)`

### **Thiếu Methods:**

- ❌ `addMember(projectId, userId, dto)`
- ❌ `removeMember(projectId, userId, targetUserId)`
- ❌ `listMembers(projectId)`
- ❌ `ensureOwnerOfProject(projectId, userId)` - Permission helper

---

## 3. KIỂM TRA DATABASE SCHEMA

### ❌ **Trạng thái: THIẾU TABLE**

**File:** `prisma/schema.prisma`

### **Model `projects` hiện tại:**

```prisma
model projects {
  id            String          @id @default(dbgenerated("uuid_generate_v4()")) @db.Uuid
  workspace_id  String          @db.Uuid
  name          String
  description   String?
  key           String?         @db.VarChar(10)
  type          project_type    @default(PERSONAL)
  issue_seq     Int             @default(0)
  board_type    String          @default("KANBAN") @db.VarChar(10)
  created_at    DateTime        @default(now()) @db.Timestamptz(6)
  updated_at    DateTime        @default(now()) @db.Timestamptz(6)

  // Relations
  boards        boards[]
  events        events[]
  workspaces    workspaces      @relation(fields: [workspace_id], references: [id], onDelete: Cascade, onUpdate: NoAction)
  sprints       sprints[]
  tasks         tasks[]
  activity_logs activity_logs[]

  @@unique([workspace_id, key])
  @@index([workspace_id])
}
```

**Vấn đề:**

- ❌ Không có relation với `users`
- ❌ Không có bảng `project_members`
- ❌ Không có cách track ai là member của project

### **Model `memberships` hiện tại:**

```prisma
model memberships {
  id           String     @id @default(dbgenerated("uuid_generate_v4()")) @db.Uuid
  role         role
  user_id      String     @db.Uuid
  workspace_id String     @db.Uuid  // ← CHỈ CÓ workspace_id
  created_at   DateTime   @default(now()) @db.Timestamptz(6)
  users        users      @relation(fields: [user_id], references: [id], onDelete: Cascade, onUpdate: NoAction)
  workspaces   workspaces @relation(fields: [workspace_id], references: [id], onDelete: Cascade, onUpdate: NoAction)

  @@unique([user_id, workspace_id])
}
```

**Vấn đề:**

- ❌ Chỉ track workspace members
- ❌ Không có `project_id` field
- ❌ Không thể biết user nào thuộc project nào

### **Schema cần thêm:**

```prisma
model project_members {
  id         String        @id @default(dbgenerated("uuid_generate_v4()")) @db.Uuid
  project_id String        @db.Uuid
  user_id    String        @db.Uuid
  role       project_role  @default(MEMBER)
  created_at DateTime      @default(now()) @db.Timestamptz(6)

  projects   projects      @relation(fields: [project_id], references: [id], onDelete: Cascade, onUpdate: NoAction)
  users      users         @relation(fields: [user_id], references: [id], onDelete: Cascade, onUpdate: NoAction)

  @@unique([project_id, user_id])
  @@index([project_id])
  @@index([user_id])
}

enum project_role {
  OWNER
  MEMBER
}
```

**Lý do cần bảng riêng:**

1. **Separation of Concerns**: Workspace ≠ Project permissions
2. **Flexibility**: User có thể là member của workspace nhưng không phải tất cả projects
3. **Scalability**: Dễ mở rộng thêm roles (VIEWER, EDITOR, etc.)
4. **Audit**: Track được khi nào user join/leave project

---

## 4. KIỂM TRA USERS API

### ⚠️ **Trạng thái: THIẾU SEARCH FUNCTION**

### **Endpoints hiện có:**

**File:** `src/modules/users/users.controller.ts`

```typescript
@Controller('users')
export class UsersController {
  @Post('local/signup')     // ✅ Đăng ký email/password
  @Post('local/signin')     // ✅ Đăng nhập email/password
  @Post('firebase/auth')    // ✅ Google Sign-In
  @Get('me')                // ✅ Lấy thông tin user hiện tại
  @Put('me')                // ✅ Update profile
}
```

### **Service Methods hiện có:**

**File:** `src/modules/users/users.service.ts`

```typescript
export class UsersService {
  ensureFromFirebase(opts); // ✅ Sync Firebase user
  localSignup(data); // ✅ Đăng ký
  localLogin(data); // ✅ Đăng nhập
  firebaseAuth(uid, token); // ✅ Firebase auth
  getById(id: string); // ✅ Lấy user by ID
  updateMeById(id, data); // ✅ Update profile
}
```

### **❌ Thiếu Methods quan trọng:**

```typescript
// THIẾU: Tìm user by email (cần cho invite)
findByEmail(email: string): Promise<User | null>

// THIẾU: Search users trong workspace
findUsersByWorkspace(workspaceId: string): Promise<User[]>

// THIẾU: Search users by name/email (autocomplete)
searchUsers(query: string): Promise<User[]>
```

### **Tại sao cần Search User API:**

Khi invite member, cần:

1. User nhập **email** của người muốn mời
2. Backend tìm user có email đó
3. Nếu tìm thấy → Lấy `userId` để add vào project
4. Nếu không → Báo "User not found"

**Flow hiện tại không hoạt động vì:**

```typescript
// ❌ KHÔNG THỂ làm được
POST /projects/123/members
{
  "email": "friend@example.com",  // ← Backend không có API tìm user by email
  "role": "MEMBER"
}

// ✅ CẦN làm như này
// Step 1: Search user by email
GET /users/search?email=friend@example.com
→ Response: { id: "user-uuid-456", email: "friend@example.com" }

// Step 2: Add member using userId
POST /projects/123/members
{
  "userId": "user-uuid-456",  // ← Dùng ID từ step 1
  "role": "MEMBER"
}
```

### **Implementation cần thêm:**

**Service:**

```typescript
// users.service.ts
async findByEmail(email: string) {
  return this.prisma.users.findUnique({
    where: { email },
    select: {
      id: true,
      email: true,
      name: true,
      avatar_url: true
    },
  });
}

async searchUsers(query: string) {
  return this.prisma.users.findMany({
    where: {
      OR: [
        { email: { contains: query, mode: 'insensitive' } },
        { name: { contains: query, mode: 'insensitive' } },
      ],
    },
    select: {
      id: true,
      email: true,
      name: true,
      avatar_url: true
    },
    take: 10,
  });
}
```

**Controller:**

```typescript
// users.controller.ts
@Get('search')
@ApiBearerAuth()
@UseGuards(CombinedAuthGuard)
searchByEmail(@Query('email') email: string) {
  if (!email) {
    throw new BadRequestException('Email query parameter is required');
  }
  return this.users.findByEmail(email);
}
```

---

## 5. CÁC GIẢI PHÁP ĐỀ XUẤT

### **Option 1: Dùng Workspace Members (Tạm thời)** ⚠️

#### **Mô tả:**

Thay vì invite vào project, invite user vào workspace. User trong workspace tự động truy cập được tất cả projects.

#### **Ưu điểm:**

- ✅ Backend đã sẵn sàng 100%
- ✅ Không cần migration database
- ✅ Có thể implement ngay lập tức
- ✅ Đã có permission system

#### **Nhược điểm:**

- ❌ Không chi tiết - invite vào workspace, không phải project
- ❌ User có quyền truy cập **TẤT CẢ** projects trong workspace
- ❌ Không phân quyền per project
- ❌ **VẪN THIẾU** API search user by email

#### **Android Implementation:**

```kotlin
// InviteMemberDialog.kt
class InviteMemberDialog {
    fun inviteToWorkspace(workspaceId: String, email: String) {
        // Step 1: Search user by email
        // ❌ API này chưa có - cần implement
        apiService.searchUserByEmail(email).enqueue { response ->
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!

                // Step 2: Add to workspace
                val dto = AddMemberDto(
                    userId = user.id,
                    role = "MEMBER"
                )

                apiService.addWorkspaceMember(workspaceId, dto).enqueue {
                    // Success - User added to workspace
                }
            } else {
                // User not found
                showError("User with email $email not found")
            }
        }
    }
}
```

#### **Kết luận:**

- ⚠️ **KHÔNG KHUYẾN NGHỊ** cho long-term
- Có thể dùng nếu cần nhanh nhưng vẫn cần thêm Search User API

---

### **Option 2: Implement Project Members (Khuyến nghị)** ✅

#### **Mô tả:**

Implement đầy đủ hệ thống project members riêng biệt, độc lập với workspace members.

#### **Ưu điểm:**

- ✅ Phân quyền chi tiết per project
- ✅ User chỉ truy cập projects được invite
- ✅ Scalable - dễ thêm roles mới
- ✅ Best practice architecture
- ✅ Production-ready solution

#### **Nhược điểm:**

- ❌ Cần thời gian implement (6-8 giờ)
- ❌ Cần database migration
- ❌ Cần viết nhiều code

### **Implementation Plan:**

#### **A. Database Migration**

**File:** `prisma/migrations/XXXXXX_add_project_members/migration.sql`

```sql
-- CreateEnum
CREATE TYPE "project_role" AS ENUM ('OWNER', 'MEMBER');

-- CreateTable
CREATE TABLE "project_members" (
    "id" UUID NOT NULL DEFAULT uuid_generate_v4(),
    "project_id" UUID NOT NULL,
    "user_id" UUID NOT NULL,
    "role" "project_role" NOT NULL DEFAULT 'MEMBER',
    "created_at" TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "project_members_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE INDEX "project_members_project_id_idx" ON "project_members"("project_id");

-- CreateIndex
CREATE INDEX "project_members_user_id_idx" ON "project_members"("user_id");

-- CreateIndex
CREATE UNIQUE INDEX "project_members_project_id_user_id_key" ON "project_members"("project_id", "user_id");

-- AddForeignKey
ALTER TABLE "project_members" ADD CONSTRAINT "project_members_project_id_fkey"
    FOREIGN KEY ("project_id") REFERENCES "projects"("id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- AddForeignKey
ALTER TABLE "project_members" ADD CONSTRAINT "project_members_user_id_fkey"
    FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
```

**Update Prisma Schema:**

```prisma
model project_members {
  id         String        @id @default(dbgenerated("uuid_generate_v4()")) @db.Uuid
  project_id String        @db.Uuid
  user_id    String        @db.Uuid
  role       project_role  @default(MEMBER)
  created_at DateTime      @default(now()) @db.Timestamptz(6)

  projects   projects      @relation(fields: [project_id], references: [id], onDelete: Cascade, onUpdate: NoAction)
  users      users         @relation(fields: [user_id], references: [id], onDelete: Cascade, onUpdate: NoAction)

  @@unique([project_id, user_id])
  @@index([project_id])
  @@index([user_id])
}

model projects {
  // ... existing fields
  project_members project_members[]  // ← Add this relation
}

model users {
  // ... existing fields
  project_members project_members[]  // ← Add this relation
}

enum project_role {
  OWNER
  MEMBER
}
```

#### **B. DTO Classes**

**File:** `src/modules/projects/dto/add-project-member.dto.ts`

```typescript
import { IsEnum, IsOptional, IsUUID } from "class-validator";
import { project_role } from "@prisma/client";

export class AddProjectMemberDto {
  @IsUUID()
  userId: string;

  @IsEnum(project_role)
  @IsOptional()
  role?: project_role;
}
```

**File:** `src/modules/projects/dto/update-project-member.dto.ts`

```typescript
import { IsEnum } from "class-validator";
import { project_role } from "@prisma/client";

export class UpdateProjectMemberDto {
  @IsEnum(project_role)
  role: project_role;
}
```

#### **C. Service Methods**

**File:** `src/modules/projects/projects.service.ts`

```typescript
import {
  Injectable,
  ForbiddenException,
  NotFoundException,
  ConflictException,
} from "@nestjs/common";
import { AddProjectMemberDto } from "./dto/add-project-member.dto";

@Injectable()
export class ProjectsService {
  // ... existing methods

  // ========== PROJECT MEMBERS ==========

  /**
   * Check if user is owner of project
   */
  private async ensureOwnerOfProject(projectId: string, userId: string) {
    const member = await this.prisma.project_members.findUnique({
      where: {
        project_id_user_id: {
          project_id: projectId,
          user_id: userId,
        },
      },
    });

    if (!member || member.role !== "OWNER") {
      throw new ForbiddenException(
        "Only project owner can perform this action"
      );
    }
  }

  /**
   * Add member to project
   */
  async addMember(
    projectId: string,
    actorId: string,
    dto: AddProjectMemberDto
  ) {
    // Check permission
    await this.ensureOwnerOfProject(projectId, actorId);

    // Check if project exists
    const project = await this.prisma.projects.findUnique({
      where: { id: projectId },
    });

    if (!project) {
      throw new NotFoundException("Project not found");
    }

    // Check if user exists
    const user = await this.prisma.users.findUnique({
      where: { id: dto.userId },
    });

    if (!user) {
      throw new NotFoundException("User not found");
    }

    // Add member
    try {
      const member = await this.prisma.project_members.create({
        data: {
          project_id: projectId,
          user_id: dto.userId,
          role: dto.role ?? "MEMBER",
        },
        include: {
          users: {
            select: {
              id: true,
              email: true,
              name: true,
              avatar_url: true,
            },
          },
        },
      });

      return member;
    } catch (error: any) {
      if (error?.code === "P2002") {
        throw new ConflictException("User is already a member of this project");
      }
      throw error;
    }
  }

  /**
   * List project members
   */
  async listMembers(projectId: string) {
    return this.prisma.project_members.findMany({
      where: { project_id: projectId },
      include: {
        users: {
          select: {
            id: true,
            email: true,
            name: true,
            avatar_url: true,
          },
        },
      },
      orderBy: {
        created_at: "asc",
      },
    });
  }

  /**
   * Remove member from project
   */
  async removeMember(projectId: string, actorId: string, targetUserId: string) {
    // Check permission
    await this.ensureOwnerOfProject(projectId, actorId);

    // Cannot remove yourself if you're the only owner
    if (actorId === targetUserId) {
      const ownerCount = await this.prisma.project_members.count({
        where: {
          project_id: projectId,
          role: "OWNER",
        },
      });

      if (ownerCount === 1) {
        throw new ForbiddenException(
          "Cannot remove the only owner from project"
        );
      }
    }

    // Remove member
    try {
      return await this.prisma.project_members.delete({
        where: {
          project_id_user_id: {
            project_id: projectId,
            user_id: targetUserId,
          },
        },
      });
    } catch (error: any) {
      if (error?.code === "P2025") {
        throw new NotFoundException("Member not found in this project");
      }
      throw error;
    }
  }

  /**
   * Update member role
   */
  async updateMemberRole(
    projectId: string,
    actorId: string,
    targetUserId: string,
    newRole: project_role
  ) {
    await this.ensureOwnerOfProject(projectId, actorId);

    return this.prisma.project_members.update({
      where: {
        project_id_user_id: {
          project_id: projectId,
          user_id: targetUserId,
        },
      },
      data: {
        role: newRole,
      },
    });
  }
}
```

#### **D. Controller Endpoints**

**File:** `src/modules/projects/projects.controller.ts`

```typescript
import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  Patch,
  Post,
  Query,
  UseGuards,
} from "@nestjs/common";
import { ApiBearerAuth, ApiTags } from "@nestjs/swagger";
import { ProjectsService } from "./projects.service";
import { CreateProjectDto } from "./dto/create-project.dto";
import { UpdateProjectDto } from "./dto/update-project.dto";
import { AddProjectMemberDto } from "./dto/add-project-member.dto";
import { CurrentUser } from "src/auth/current-user.decorator";
import { CombinedAuthGuard } from "src/auth/combined-auth.guard";

@ApiTags("projects")
@ApiBearerAuth()
@UseGuards(CombinedAuthGuard)
@Controller("projects")
export class ProjectsController {
  constructor(private readonly svc: ProjectsService) {}

  // ... existing endpoints

  /**
   * List project members
   */
  @Get(":id/members")
  listMembers(@Param("id") id: string) {
    return this.svc.listMembers(id);
  }

  /**
   * Add member to project
   */
  @Post(":id/members")
  addMember(
    @CurrentUser("id") userId: string,
    @Param("id") id: string,
    @Body() dto: AddProjectMemberDto
  ) {
    return this.svc.addMember(id, userId, dto);
  }

  /**
   * Remove member from project
   */
  @Delete(":id/members/:userId")
  removeMember(
    @CurrentUser("id") actorId: string,
    @Param("id") projectId: string,
    @Param("userId") targetUserId: string
  ) {
    return this.svc.removeMember(projectId, actorId, targetUserId);
  }
}
```

#### **E. Users Search API**

**File:** `src/modules/users/users.service.ts`

```typescript
@Injectable()
export class UsersService {
  // ... existing methods

  /**
   * Find user by email (for invites)
   */
  async findByEmail(email: string) {
    return this.prisma.users.findUnique({
      where: { email },
      select: {
        id: true,
        email: true,
        name: true,
        avatar_url: true,
      },
    });
  }

  /**
   * Search users by email or name
   */
  async searchUsers(query: string, limit: number = 10) {
    return this.prisma.users.findMany({
      where: {
        OR: [
          { email: { contains: query, mode: "insensitive" } },
          { name: { contains: query, mode: "insensitive" } },
        ],
      },
      select: {
        id: true,
        email: true,
        name: true,
        avatar_url: true,
      },
      take: limit,
    });
  }
}
```

**File:** `src/modules/users/users.controller.ts`

```typescript
@Controller("users")
export class UsersController {
  // ... existing endpoints

  /**
   * Search user by email
   */
  @Get("search")
  @ApiBearerAuth()
  @UseGuards(CombinedAuthGuard)
  async searchByEmail(@Query("email") email: string) {
    if (!email) {
      throw new BadRequestException("Email query parameter is required");
    }

    const user = await this.users.findByEmail(email);
    if (!user) {
      throw new NotFoundException("User not found");
    }

    return user;
  }

  /**
   * Search users by query (autocomplete)
   */
  @Get("search/all")
  @ApiBearerAuth()
  @UseGuards(CombinedAuthGuard)
  async searchUsers(@Query("q") query: string, @Query("limit") limit?: number) {
    if (!query || query.trim().length < 2) {
      throw new BadRequestException("Query must be at least 2 characters");
    }

    return this.users.searchUsers(query, limit);
  }
}
```

#### **F. Android Integration**

**API Service:**

```kotlin
// ApiService.kt
interface ApiService {
    // Search user
    @GET("users/search")
    suspend fun searchUserByEmail(
        @Query("email") email: String
    ): Response<UserDto>

    // Add project member
    @POST("projects/{projectId}/members")
    suspend fun addProjectMember(
        @Path("projectId") projectId: String,
        @Body dto: AddProjectMemberDto
    ): Response<ProjectMemberDto>

    // List project members
    @GET("projects/{projectId}/members")
    suspend fun getProjectMembers(
        @Path("projectId") projectId: String
    ): Response<List<ProjectMemberDto>>

    // Remove project member
    @DELETE("projects/{projectId}/members/{userId}")
    suspend fun removeProjectMember(
        @Path("projectId") projectId: String,
        @Path("userId") userId: String
    ): Response<Unit>
}
```

**DTO:**

```kotlin
// AddProjectMemberDto.kt
data class AddProjectMemberDto(
    val userId: String,
    val role: String = "MEMBER"
)

// ProjectMemberDto.kt
data class ProjectMemberDto(
    val id: String,
    val projectId: String,
    val userId: String,
    val role: String,
    val createdAt: String,
    val user: UserDto
)
```

**Invite Dialog:**

```kotlin
// InviteMemberDialog.kt
class InviteMemberDialog : BottomSheetDialogFragment() {
    private fun inviteMember(email: String) {
        lifecycleScope.launch {
            try {
                // Step 1: Search user by email
                val searchResponse = apiService.searchUserByEmail(email)

                if (searchResponse.isSuccessful && searchResponse.body() != null) {
                    val user = searchResponse.body()!!

                    // Step 2: Add to project
                    val dto = AddProjectMemberDto(
                        userId = user.id,
                        role = "MEMBER"
                    )

                    val addResponse = apiService.addProjectMember(projectId, dto)

                    if (addResponse.isSuccessful) {
                        Toast.makeText(context, "Member added successfully", LENGTH_SHORT).show()
                        dismiss()
                        // Reload members list
                        onMemberAdded?.invoke()
                    } else {
                        showError("Failed to add member: ${addResponse.message()}")
                    }
                } else {
                    showError("User with email $email not found")
                }
            } catch (e: Exception) {
                showError("Error: ${e.message}")
            }
        }
    }
}
```

**Update ProjectMenuBottomSheet:**

```kotlin
// ProjectMenuBottomSheet.kt
private fun setupListeners() {
    btnInvite.setOnClickListener {
        // Check if current user is project owner
        if (isProjectOwner) {
            showInviteDialog()
        } else {
            Toast.makeText(context, "Only project owner can invite members", LENGTH_SHORT).show()
        }
    }
}

private fun showInviteDialog() {
    val dialog = InviteMemberDialog.newInstance(projectId)
    dialog.onMemberAdded = {
        loadProjectMembers()
    }
    dialog.show(childFragmentManager, "InviteMemberDialog")
}

private fun loadProjectMembers() {
    lifecycleScope.launch {
        val response = apiService.getProjectMembers(projectId)
        if (response.isSuccessful) {
            val members = response.body()
            memberAdapter.setMembers(members)
        }
    }
}
```

---

## 6. CHECKLIST IMPLEMENTATION

### **Backend Tasks:**

#### **Database:**

- [ ] Tạo migration file cho `project_members` table
- [ ] Thêm enum `project_role` (OWNER, MEMBER)
- [ ] Update Prisma schema
- [ ] Chạy migration: `npx prisma migrate dev`
- [ ] Generate Prisma Client: `npx prisma generate`

#### **DTOs:**

- [ ] Tạo `AddProjectMemberDto`
- [ ] Tạo `UpdateProjectMemberDto`
- [ ] Add validation decorators

#### **Service Layer:**

- [ ] Implement `ProjectsService.addMember()`
- [ ] Implement `ProjectsService.removeMember()`
- [ ] Implement `ProjectsService.listMembers()`
- [ ] Implement `ProjectsService.updateMemberRole()`
- [ ] Implement `ProjectsService.ensureOwnerOfProject()`
- [ ] Add error handling

#### **Controller Layer:**

- [ ] Add `POST /projects/:id/members` endpoint
- [ ] Add `GET /projects/:id/members` endpoint
- [ ] Add `DELETE /projects/:id/members/:userId` endpoint
- [ ] Add Swagger documentation

#### **Users Search:**

- [ ] Implement `UsersService.findByEmail()`
- [ ] Implement `UsersService.searchUsers()`
- [ ] Add `GET /users/search?email={email}` endpoint
- [ ] Add `GET /users/search/all?q={query}` endpoint

#### **Testing:**

- [ ] Test add member API
- [ ] Test remove member API
- [ ] Test list members API
- [ ] Test permission checks
- [ ] Test duplicate member error
- [ ] Test search user API

### **Frontend Tasks:**

#### **API Integration:**

- [ ] Add `searchUserByEmail()` to ApiService
- [ ] Add `addProjectMember()` to ApiService
- [ ] Add `getProjectMembers()` to ApiService
- [ ] Add `removeProjectMember()` to ApiService
- [ ] Create DTOs (AddProjectMemberDto, ProjectMemberDto)

#### **UI Components:**

- [ ] Tạo `InviteMemberDialog` layout
- [ ] Tạo `InviteMemberDialog` class
- [ ] Thêm email input field
- [ ] Thêm search/invite button
- [ ] Thêm loading state
- [ ] Thêm error handling

#### **ProjectMenuBottomSheet:**

- [ ] Update members RecyclerView
- [ ] Add invite button click handler
- [ ] Add permission check (only owner)
- [ ] Implement loadProjectMembers()
- [ ] Update UI when member added/removed

#### **Testing:**

- [ ] Test invite flow
- [ ] Test với email không tồn tại
- [ ] Test với duplicate member
- [ ] Test remove member
- [ ] Test permission (non-owner không invite được)

---

## 7. KẾT LUẬN & KHUYẾN NGHỊ

### **Tình trạng hiện tại:**

| Component                 | Status        | Note                 |
| ------------------------- | ------------- | -------------------- |
| **Workspace Members API** | ✅ Hoàn chỉnh | Production-ready     |
| **Project Members API**   | ❌ Chưa có    | Cần implement từ đầu |
| **Database Schema**       | ❌ Thiếu bảng | Cần migration        |
| **Search User API**       | ❌ Chưa có    | Cần cho cả 2 options |
| **Frontend UI**           | ⚠️ Có layout  | Cần connect API      |

### **Đánh giá tổng quan:**

**❌ Backend CHƯA SẴN SÀNG** để làm chức năng invite member vào project.

### **Khuyến nghị:**

#### **1. Ngắn hạn (1-2 ngày):**

Mock UI invite member với toast "Coming soon" hoặc disable button:

```kotlin
btnInvite.setOnClickListener {
    Toast.makeText(context, "Feature coming soon!", LENGTH_SHORT).show()
}
```

#### **2. Trung hạn (1 tuần):**

Implement **Option 2** đầy đủ:

- Database migration
- Backend API
- Frontend integration
- Testing

#### **3. Ưu tiên cao:**

Implement Search User API ngay (cần cho cả 2 options):

```typescript
GET /users/search?email={email}
```

### **Effort Estimate:**

| Task                         | Time       | Priority     |
| ---------------------------- | ---------- | ------------ |
| Database migration           | 30 phút    | HIGH         |
| Backend Service + Controller | 3 giờ      | HIGH         |
| Users Search API             | 1 giờ      | **CRITICAL** |
| Frontend UI                  | 2 giờ      | MEDIUM       |
| Testing + Bug fixes          | 2 giờ      | MEDIUM       |
| **TOTAL**                    | **~8 giờ** | -            |

### **Lộ trình đề xuất:**

**Sprint 1 (Tuần này):**

- [ ] Database migration
- [ ] Backend API cơ bản
- [ ] Users Search API
- [ ] Mock UI Frontend

**Sprint 2 (Tuần sau):**

- [ ] Frontend integration
- [ ] Testing đầy đủ
- [ ] Bug fixes
- [ ] Documentation

### **Rủi ro:**

| Risk                  | Impact | Mitigation                   |
| --------------------- | ------ | ---------------------------- |
| Migration conflict    | HIGH   | Backup DB trước khi migrate  |
| API breaking changes  | MEDIUM | Versioning API endpoints     |
| Permission bugs       | HIGH   | Test kỹ permission logic     |
| Frontend-Backend sync | MEDIUM | Dùng TypeScript/Kotlin types |

### **Next Steps:**

1. **Quyết định** chọn Option 1 (tạm thời) hay Option 2 (đầy đủ)
2. **Assign tasks** cho Backend/Frontend developers
3. **Tạo Prisma migration** và review schema
4. **Implement** theo checklist
5. **Testing** và deployment

---

## 📚 APPENDIX

### **A. API Reference**

#### **Workspace Members (Đã có):**

```
POST   /workspaces/:id/members          - Add member to workspace
GET    /workspaces/:id/members          - List workspace members
DELETE /workspaces/:id/members/:userId  - Remove member from workspace
```

#### **Project Members (Cần thêm):**

```
POST   /projects/:id/members             - Add member to project
GET    /projects/:id/members             - List project members
DELETE /projects/:id/members/:userId     - Remove member from project
PATCH  /projects/:id/members/:userId     - Update member role
```

#### **Users Search (Cần thêm):**

```
GET    /users/search?email={email}       - Find user by email
GET    /users/search/all?q={query}       - Search users (autocomplete)
```

### **B. Database Schema Comparison**

**Current:**

```
workspaces (1) ←→ (N) memberships ←→ (1) users
projects (N) → (1) workspaces
```

**After Migration:**

```
workspaces (1) ←→ (N) memberships ←→ (1) users
projects (1) ←→ (N) project_members ←→ (1) users
projects (N) → (1) workspaces
```

### **C. Permission Matrix**

| Action             | OWNER | MEMBER | Non-member |
| ------------------ | ----- | ------ | ---------- |
| View project       | ✅    | ✅     | ❌         |
| Edit project       | ✅    | ❌     | ❌         |
| Add member         | ✅    | ❌     | ❌         |
| Remove member      | ✅    | ❌     | ❌         |
| Change member role | ✅    | ❌     | ❌         |
| Delete project     | ✅    | ❌     | ❌         |

### **D. Error Codes Reference**

| Code    | Message                     | Description            |
| ------- | --------------------------- | ---------------------- |
| `P2002` | Unique constraint violation | User already member    |
| `P2025` | Record not found            | Member doesn't exist   |
| `403`   | Forbidden                   | Not project owner      |
| `404`   | Not Found                   | User/Project not found |
| `409`   | Conflict                    | Duplicate member       |

---

**Người tạo:** AI Assistant  
**Ngày cập nhật:** October 26, 2025  
**Version:** 1.0  
**Status:** Draft - Pending Review
