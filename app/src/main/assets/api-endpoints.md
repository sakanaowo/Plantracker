# PlanTracker API Endpoints

Base URL: `http://localhost:3000/api`

## Users

### POST /users/local/signup
- **Auth:** Public
- **Description:** Register a new user with email/password. Also provisions a Firebase account and persists the profile.
- **Body:**
  ```json
  {
    "email": "user@example.com",
    "password": "string (6-100 chars)",
    "name": "optional display name"
  }
  ```
- **Response:** Returns the persisted user plus Firebase ID/refresh tokens.

### POST /users/local/signin
- **Auth:** Public
- **Description:** Sign in with email/password via Firebase.
- **Body:**
  ```json
  {
    "email": "user@example.com",
    "password": "string (6-100 chars)"
  }
  ```
- **Response:** Returns the user profile along with `token`, `refreshToken`, and `expiresIn`.

### POST /users/firebase/sync
- **Auth:** Bearer token (Firebase ID token)
- **Description:** Syncs the Firebase profile into the local database. Only available to Firebase-sourced users.
- **Body:** None
- **Response:** `{ "user": { ...synced user row... } }`

### GET /users/me
- **Auth:** Bearer token
- **Description:** Fetch the current authenticated user. Resolves either by Firebase UID or local user ID, depending on the token source.
- **Body:** None

### PUT /users/me
- **Auth:** Bearer token
- **Description:** Update the authenticated user's profile.
- **Body:**
  ```json
  {
    "name": "optional string (<=100 chars)",
    "email": "optional valid email",
    "avatar_url": "optional HTTPS URL"
  }
  ```

## Workspaces
_All workspace routes require a Bearer token._

### POST /workspaces/users/{userId}/personal
- **Description:** Create (or ensure) the personal workspace for the specified user. Caller must match `{userId}`.
- **Body:**
  ```json
  {
    "name": "optional override for personal workspace name"
  }
  ```

### POST /workspaces
- **Description:** Create a new workspace owned by the caller.
- **Body:**
  ```json
  {
    "name": "required string (<=100 chars)",
    "type": "optional workspace_type enum"
  }
  ```

### GET /workspaces
- **Description:** List workspaces the caller belongs to.

### GET /workspaces/{id}
- **Description:** Fetch workspace details the caller has access to.

### PATCH /workspaces/{id}
- **Description:** Update workspace metadata.
- **Body:**
  ```json
  {
    "name": "optional string (<=100 chars)",
    "type": "optional workspace_type enum"
  }
  ```

### DELETE /workspaces/{id}
- **Description:** Remove a workspace (requires proper permissions).

### GET /workspaces/{id}/members
- **Description:** List members of the workspace.

### POST /workspaces/{id}/members
- **Description:** Add a member to the workspace.
- **Body:**
  ```json
  {
    "userId": "UUID of the user to add",
    "role": "role enum (owner | admin | member)"
  }
  ```

### DELETE /workspaces/{id}/members/{userId}
- **Description:** Remove a member from the workspace.

> `workspace_type` and `role` enums come from the Prisma schema. Check `@prisma/client` definitions for the accepted values.

## Timers

_All timer routes require a Bearer token._

### POST /timers/start

- **Auth:** Bearer token
- **Description:** Start a new timer for a task. If there's already a running timer for the authenticated user, it will be automatically stopped before starting the new one.
- **Body:**

  ```json
  {
    "taskId": "UUID of the task",
    "startAt": "optional ISO 8601 datetime (defaults to now)",
    "note": "optional string"
  }
  ```

- **Response:** Returns the created `time_entries` record with `end_at: null` indicating the timer is running.

### PATCH /timers/{timerId}/stop

- **Auth:** Bearer token
- **Description:** Stop a running timer. Only the owner of the timer can stop it.
- **Params:**
  - `timerId`: UUID of the timer to stop
- **Body:**

  ```json
  {
    "endAt": "optional ISO 8601 datetime (defaults to now)",
    "note": "optional string to update the note"
  }
  ```

- **Response:** Returns the updated `time_entries` record with `end_at` and `duration_sec` calculated.
- **Errors:**
  - `404`: Timer not found
  - `403`: User doesn't own this timer
  - `409`: Timer already stopped or end time is before start time

### PATCH /timers/{timerId}/note

- **Auth:** Bearer token
- **Description:** Update the note of a time entry. Only the owner can update.
- **Params:**
  - `timerId`: UUID of the timer
- **Body:**

  ```json
  {
    "note": "string (required)"
  }
  ```

- **Response:** Returns the updated `time_entries` record.
- **Errors:**
  - `404`: Timer not found
  - `403`: User doesn't own this timer
# Bao cao Endpoint API

## Tong quan

- Base path mac dinh: `/api`.
- CombinedAuthGuard duoc dang ky toan cuc; mac dinh tat ca endpoint yeu cau header `Authorization: Bearer <Firebase ID token>`.
- Cac endpoint danh dau `Public` trong bang ben duoi co the truy cap khong can token.
- Payload nhan/tra ve theo dinh dang JSON va duoc kiem tra boi `ValidationPipe` cua NestJS.

## Health

| Method | URL | Auth | Chuc nang | Tham so / Body |
| --- | --- | --- | --- | --- |
| GET | /api/health/db | Bearer | Kiem tra ket noi database va tra ve thoi diem hien tai | none |

## Users

| Method | URL | Auth | Chuc nang | Tham so / Body |
| --- | --- | --- | --- | --- |
| POST | /api/users/local/signup | Public | Dang ky tai khoan bang email/password, khoi tao user trong Firebase va DB | Body: { email, password, name? } |
| POST | /api/users/local/signin | Public | Dang nhap bang email/password, nhan Firebase ID token | Body: { email, password } |
| POST | /api/users/firebase/auth | Public | Hoan tat dang nhap Google: xac thuc `idToken`, dong bo ho so vao DB | Body: { idToken } |
| GET | /api/users/me | Bearer | Lay ho so nguoi dung tu token dang dang nhap | none |
| PUT | /api/users/me | Bearer | Cap nhat ten hoac anh dai dien cua nguoi dung | Body: { name?, avatar_url? } |

## Workspaces

| Method | URL | Auth | Chuc nang | Tham so / Body |
| --- | --- | --- | --- | --- |
| POST | /api/workspaces/users/{userId}/personal | Bearer | Dam bao nguoi dung co workspace ca nhan (chi tao cho chinh minh) | Params: userId; Body: { name? } |
| POST | /api/workspaces | Bearer | Tao workspace moi thuoc ve nguoi dung hien tai | Body: { name, type? } |
| GET | /api/workspaces | Bearer | Liet ke cac workspace nguoi dung tham gia | none |
| GET | /api/workspaces/{id} | Bearer | Lay chi tiet workspace neu nguoi dung co quyen | Params: id |
| PATCH | /api/workspaces/{id} | Bearer | Cap nhat ten hoac loai workspace | Params: id; Body: { name?, type? } |
| DELETE | /api/workspaces/{id} | Bearer | Xoa workspace (yeu cau quyen phu hop) | Params: id |
| GET | /api/workspaces/{id}/members | Bearer | Liet ke thanh vien trong workspace | Params: id |
| POST | /api/workspaces/{id}/members | Bearer | Them thanh vien moi vao workspace voi vai tro chi dinh | Params: id; Body: { userId, role } |
| DELETE | /api/workspaces/{id}/members/{userId} | Bearer | Loai bo thanh vien khoi workspace | Params: id, userId |

## Projects

| Method | URL | Auth | Chuc nang | Tham so / Body |
| --- | --- | --- | --- | --- |
| GET | /api/projects | Bearer | Liet ke project trong mot workspace cu the | Query: workspaceId (bat buoc) |
| POST | /api/projects | Bearer | Tao project moi trong workspace | Body: { name, workspace_id, key?, description? } |
| PATCH | /api/projects/{id} | Bearer | Cap nhat thong tin project | Params: id; Body: { name?, key?, description? } |

## Boards

| Method | URL | Auth | Chuc nang | Tham so / Body |
| --- | --- | --- | --- | --- |
| GET | /api/boards | Bearer | Liet ke board cua mot project | Query: projectId (bat buoc) |
| POST | /api/boards | Bearer | Tao board moi trong project | Body: { projectId, name, order? } |
| PATCH | /api/boards/{id} | Bearer | Cap nhat ten hoac thu tu board | Params: id; Body: { name?, order? } |
| DELETE | /api/boards/{id} | Bearer | Xoa board khoi project | Params: id |

## Tasks

| Method | URL | Auth | Chuc nang | Tham so / Body |
| --- | --- | --- | --- | --- |
| GET | /api/tasks/by-board/{boardId} | Bearer | Liet ke cac task thuoc board | Params: boardId |
| GET | /api/tasks/{id} | Bearer | Lay chi tiet mot task | Params: id |
| POST | /api/tasks | Bearer | Tao task moi trong board/project | Body: { projectId, boardId, title, assigneeId? } |
| POST | /api/tasks/{id}/move | Bearer | Chuyen task sang board khac va sap xep lai thu tu | Params: id; Body: { toBoardId, beforeId?, afterId? } |
| PATCH | /api/tasks/{id} | Bearer | Cap nhat noi dung task | Params: id; Body: { title?, description?, assigneeId? } |
| DELETE | /api/tasks/{id} | Bearer | Xoa mem task khoi he thong | Params: id |

## Timers

| Method | URL | Auth | Chuc nang | Tham so / Body |
| --- | --- | --- | --- | --- |
| POST | /api/timers/start | Bearer | Bat dau timer moi cho task (tu dong dung timer dang chay neu co) | Body: { taskId, startAt?, note? } |
| PATCH | /api/timers/{timerId}/stop | Bearer | Dung timer va cap nhat thoi gian ket thuc | Params: timerId; Body: { startAt?, endAt?, note? } |
| PATCH | /api/timers/{timerId}/note | Bearer | Cap nhat ghi chu cho time entry | Params: timerId; Body: { note } |

## Storage

| Method | URL | Auth | Chuc nang | Tham so / Body |
| --- | --- | --- | --- | --- |
| POST | /api/storage/upload-url | Bearer | Sinh URL ky voi quyen upload len storage | Body: { fileName } |
| GET | /api/storage/view-url | Bearer | Sinh URL ky de xem/tai file tu storage | Query: path (duong dan doi tuong) |

## Ghi chu them

- Tat ca route duoc tiep can thong qua `http(s)://<host>:<port>/api/...`. Moi request can kem header `Content-Type: application/json` neu gui body JSON.
- CombinedAuthGuard su dung Firebase Admin SDK: bearer token phai la Firebase ID token hop le tu client.
- Backend da kich hoat Swagger tai `/api/docs` de xem chi tiet schema (neu server dang chay).
