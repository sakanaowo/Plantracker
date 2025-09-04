# Personas & Use case đơn giản:

## Team:

### Phân công:

- **Leader:** lên kế hoạch, đặt lịch họp,…
- **Teammate:** Bật timer khi làm task, kiểm tra task trong ngày, nhận lịch họp, …

### Flow

- **Leader:** Tạo project → tạo task → kéo thả cột (Todo/Doing/Done)
- **Teammate:** Mở task → bật timer → Stop → note Timesheet
- Đề xuất lịch họp: system quét busy/free của team → đề xuất 2/3 slot 1 tuần → leader chọn slot → system tự gửi email + tạo event gg calendar

## Personal:

- user

# Kiến trúc (chưa đầy đủ)

### **Context:**

Mobile App (Android Java) ⇄ Backend Service ⇄ PostgreSQL/Redis ⇄ Email Provider ⇄ Google Calendar API ⇄ FCM.?

### **Container:**

- **Android App (Java, MVVM):**
    - UI (XML + Material), ViewModel (Lifecycle), Repository, Room (offline), Retrofit/OkHttp, WorkManager (đồng bộ), FCM.
- **Backend (Spring Boot 3, Java 17):**
    - Modules: Auth, Projects/Tasks, TimeTracking, CalendarIntegration, Mailer, Suggestion.
    - PostgreSQL (data chính), Redis (cache free/busy 5–10 phút), Scheduler (Quartz/Spring Scheduling), S3/Cloud Storage (nếu cần file).
- **Integrations:**
    - Google OAuth2 (scope Calendar), SendGrid/SMTP, FCM Server.

### **Component (backend):**

- **Auth** (OAuth-link Google);
- **Project/Task Service** (Kanban, labels, assignees);
- **Time Service** (start/stop, hợp nhất time entries);
- **Calendar Service** (free/busy, create event);
- **Suggest Service** (heuristic + ràng buộc);
- **Mailer** (queue + retry);
- **Notification** (push).

# Data model (chưa đầy đủ):

| table | attribute |
| --- | --- |
| user | id, name, email, password_hash, avatar_url, created_at |
| **workspaces** | id, name, owner_id, created_at |
| memberships | id, user_id, workspace_id, role |
| project | id, workspace_id, name, description, created_at |
| board (kanban) | id, project_id, name, order |
| tasks | id, project_id, board_id, title, description, assignee_id, due_at, priority, created_at |
| label | id, workspace_id, name, color |
| task label | task_id, label_id |
| time entry | id, task_id, user_id, start_at, end_at, duration, note |
| events | id, project_id, title, start_at,end_at, created_by |
| participant | event_id, user_id, provider, access_token, expired_at |
| integration token (gg calendar34) | id, user_id, provider, access_token, refresh_token, expires_at |
| email queue | id, to_email, subject, body, status, retry_counter, scheduled_at |
| notification |  |

# API Contract v1 (base)

## Auth:

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/google/link (OAuth code)`→integration_token

## Project/Task:

- `GET /project?workspace_id=`
- `POST /projects` {workspace_id,name,desc}
- `GET /tasks?project_id=`
- `POST /tasks` {project_id,board_id,title,desc,due_at,assignee_id}
- `PATCH /tasks/{id}` (move board_id, update fields)

## Time tracking:

- `POST /time/start` {task_id} → {entry_id,start_at}
- `POST /time/stop` {entry_id} → {end_at,duration_sec}
- `GET /time/entries?from=&to=&project_id=`

## Calendar & Scheduling

- `POST /schedule/suggest` {participant_emails[], duration_min, date_range, working_hours, buffer_min}
    
    → `{slots:[{start_at,end_at,score}]}`
    
- `POST /events` {title,start_at,end_at,participants[],location}
    
    → tạo event (DB + Google Calendar nếu người tạo đã link Google).
    

## Email:

- `POST /mail/invite` {to[], subject, body, event_id} → queued

> Chuẩn hoá: JSON; HTTP 2xx/4xx/5xx; lỗi trả `{code,message,details?}`; Idempotency-Key cho POST quan trọng (event/mail).
> 

# Bảo mật:

- **OAuth Google Calendar**: lưu refresh_token; gọi Google bằng access_token được làm mới trên server; **không** để client Android gọi Google trực tiếp.
- **Role**: owner/admin/member trên workspace; kiểm tra quyền theo workspace_id/project_id.
- **Rate limit**: endpoints mail/schedule.
- **Logs**: ẩn PII, không log tokens.

## Tech

- **NestJS + PostgreSQL + Prisma + Redis** (queue/cache) + BullMQ (job) + googleapis (Calendar) + Nodemailer/SendGrid (email) + firebase-admin (push)
- Module:
    - `auth` (GG OAuth link), `users` , `workspaces`, `projects` (boards, tasks, labels)
    - `time`  (time entries), `schedule`  (Đề xuất slot), `Calendar` (free/busy + tạo event)
    - `mailer` (queue gửi mail), `notifications`  (FCM + in-app), `integration`  (OAuth Token)
- Thành phần kĩ thuật:
    - **HTTP**: Nest (Controllers/DTO/Guards/Interceptors).
    - **DB**: PostgreSQL + **Prisma** (migrate nhanh, typed client).
    - **Queue/Job**: **BullMQ** (+ Redis) cho gửi email, push, đồng bộ Calendar.
    - **Scheduler**: `@nestjs/schedule` quét `scheduled_at` để đẩy nhắc họp.
    - **Auth**: `@nestjs/jwt`, `passport-jwt`, `passport-google-oauth20` (server-side OAuth).
    - **Email**: Nodemailer hoặc SendGrid SDK.
    - **Google Calendar**: `googleapis` (OAuth2 + FreeBusy + Events).
    - **Push**: `firebase-admin` gửi FCM theo `user_devices`.
- Server: Render ?