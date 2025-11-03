# 📎 Attachment Data Structure Documentation

## 🎯 Overview

Attachment trong ứng dụng có **3 layers**:

1. **Backend (PostgreSQL + Prisma)** - Database schema
2. **Backend API Response** - JSON response từ NestJS
3. **Android (DTO + Domain)** - Client-side models

---

## 🗄️ Backend Database Schema

### **Prisma Model**: `attachments`

```prisma
model attachments {
  id          String   @id @default(dbgenerated("uuid_generate_v4()")) @db.Uuid
  task_id     String   @db.Uuid
  url         String
  mime_type   String?
  size        Int?
  uploaded_by String?  @db.Uuid
  created_at  DateTime @default(now()) @db.Timestamptz(6)
  tasks       tasks    @relation(fields: [task_id], references: [id], onDelete: Cascade, onUpdate: NoAction)

  @@index([task_id, created_at], map: "idx_attachments_task")
}
```

### **Field Details**:

| Field         | Type      | Nullable | Description                                 |
| ------------- | --------- | -------- | ------------------------------------------- |
| `id`          | UUID      | ❌ No    | Primary key, auto-generated                 |
| `task_id`     | UUID      | ❌ No    | Foreign key to tasks table                  |
| `url`         | String    | ❌ No    | Firebase Storage URL (full path)            |
| `mime_type`   | String    | ✅ Yes   | File MIME type (e.g., "image/png")          |
| `size`        | Int       | ✅ Yes   | File size in bytes                          |
| `uploaded_by` | UUID      | ✅ Yes   | User ID who uploaded (foreign key to users) |
| `created_at`  | Timestamp | ❌ No    | Upload timestamp (auto-generated)           |

### **Important Notes**:

- ⚠️ **`fileName` NOT stored in database** - Extracted from `url` field
- 🔗 Cascade delete: Khi task bị xóa → tất cả attachments bị xóa
- 📊 Index: `task_id + created_at` để query nhanh

---

## 🌐 Backend API Response

### **NestJS Service** (`AttachmentsService`)

#### **Response Structure** (TypeScript):

```typescript
{
  id: string; // UUID
  taskId: string; // UUID (camelCase)
  url: string; // Firebase Storage URL
  fileName: string; // ⚠️ EXTRACTED from URL, not in DB
  mimeType: string; // nullable
  size: number; // nullable (bytes)
  uploadedBy: string; // UUID, nullable
  createdAt: Date; // ISO 8601 timestamp
}
```

#### **Example JSON Response**:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "taskId": "660e8400-e29b-41d4-a716-446655440001",
  "url": "https://firebasestorage.googleapis.com/v0/b/.../1698765432-document.pdf",
  "fileName": "document.pdf",
  "mimeType": "application/pdf",
  "size": 1245678,
  "uploadedBy": "770e8400-e29b-41d4-a716-446655440002",
  "createdAt": "2025-10-29T10:30:45.123Z"
}
```

#### **fileName Extraction Logic**:

```typescript
private extractFileName(url: string): string {
  const parts = url.split('/');
  const fileNameWithTimestamp = parts[parts.length - 1];

  // Remove timestamp prefix (e.g., "1698765432-document.pdf" → "document.pdf")
  const match = fileNameWithTimestamp.match(/^\d+-(.+)$/);
  return match ? match[1] : fileNameWithTimestamp;
}
```

**URL Format**: `.../{userId}/{taskId}/{timestamp}-{fileName}`
**Extracted**: `document.pdf`

---

## 📱 Android Client-Side Models

### **1. AttachmentDTO** (Data Layer)

**Path**: `data/remote/dto/task/AttachmentDTO.java`

```java
public class AttachmentDTO {
    public String id;
    public String taskId;

    @SerializedName("url")
    public String url;

    @SerializedName("file_name")  // ⚠️ Backend sends "fileName" (camelCase)
    public String fileName;

    @SerializedName("mime_type")
    public String mimeType;

    @SerializedName("size")
    public Integer size;

    @SerializedName("uploaded_by")
    public String uploadedBy;

    @SerializedName("created_at")
    public String createdAt;  // ISO 8601 String
}
```

**Purpose**:

- ✅ Receives JSON from backend API
- ✅ Uses `@SerializedName` for snake_case ↔ camelCase mapping
- ✅ **Public fields** (no setters/getters) - used by Gson
- ⚠️ `createdAt` is **String** (not Date) - API returns ISO 8601

---

### **2. Attachment** (Domain Layer)

**Path**: `domain/model/Attachment.java`

```java
public class Attachment {
    private final String id;
    private final String taskId;
    private final String url;
    private final String fileName;
    private final String mimeType;
    private final Integer size;
    private final String uploadedBy;
    private final Date createdAt;  // ⚠️ Converted to Date object

    public Attachment(String id, String taskId, String url, String fileName,
                      String mimeType, Integer size, String uploadedBy, Date createdAt) {
        this.id = id;
        this.taskId = taskId;
        this.url = url;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.size = size;
        this.uploadedBy = uploadedBy;
        this.createdAt = createdAt;
    }

    // Getters
    public String getId() { return id; }
    public String getTaskId() { return taskId; }
    public String getUrl() { return url; }
    public String getFileName() { return fileName; }
    public String getMimeType() { return mimeType; }
    public Integer getSize() { return size; }
    public String getUploadedBy() { return uploadedBy; }
    public Date getCreatedAt() { return createdAt; }

    // Utility methods
    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }

    public boolean isPdf() {
        return "application/pdf".equals(mimeType);
    }

    public boolean isDocument() {
        if (mimeType == null) return false;
        return mimeType.contains("document") ||
                mimeType.contains("word") ||
                mimeType.contains("excel") ||
                mimeType.contains("powerpoint");
    }

    public String getFormattedSize() {
        if (size == null) return "Unknown size";
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return (size / 1024) + " KB";
        return String.format("%.1f MB", size / (1024.0 * 1024.0));
    }
}
```

**Purpose**:

- ✅ Immutable domain model (private final fields)
- ✅ Business logic methods (isImage, isPdf, getFormattedSize)
- ✅ Type-safe (Date instead of String)
- ✅ Used in ViewModels, Use-cases, UI layer

---

## 🔄 Mapping Layer

### **AttachmentMapper**

**Path**: `data/mapper/AttachmentMapper.java`

```java
public class AttachmentMapper {

    /**
     * Convert DTO → Domain Model
     */
    public static Attachment toDomain(AttachmentDTO dto) {
        if (dto == null) return null;

        Date createdAt = null;
        if (dto.createdAt != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US
                );
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                createdAt = sdf.parse(dto.createdAt);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return new Attachment(
            dto.id,
            dto.taskId,
            dto.url,
            dto.fileName,
            dto.mimeType,
            dto.size,
            dto.uploadedBy,
            createdAt
        );
    }

    /**
     * Convert Domain Model → DTO
     */
    public static AttachmentDTO toDto(Attachment attachment) {
        if (attachment == null) return null;

        AttachmentDTO dto = new AttachmentDTO();
        dto.id = attachment.getId();
        dto.taskId = attachment.getTaskId();
        dto.url = attachment.getUrl();
        dto.fileName = attachment.getFileName();
        dto.mimeType = attachment.getMimeType();
        dto.size = attachment.getSize();
        dto.uploadedBy = attachment.getUploadedBy();

        if (attachment.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US
            );
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            dto.createdAt = sdf.format(attachment.getCreatedAt());
        }

        return dto;
    }

    /**
     * Convert List<DTO> → List<Domain>
     */
    public static List<Attachment> toDomainList(List<AttachmentDTO> dtoList) {
        if (dtoList == null) return new ArrayList<>();

        List<Attachment> attachments = new ArrayList<>();
        for (AttachmentDTO dto : dtoList) {
            attachments.add(toDomain(dto));
        }
        return attachments;
    }
}
```

---

## 📊 Data Flow Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                    BACKEND (NestJS)                          │
├──────────────────────────────────────────────────────────────┤
│  Database (PostgreSQL):                                      │
│  ┌────────────────────────────────────────────────────┐     │
│  │ id, task_id, url, mime_type, size, uploaded_by,    │     │
│  │ created_at                                          │     │
│  │ ⚠️ NO fileName field in DB                          │     │
│  └────────────────────────────────────────────────────┘     │
│                         ↓                                     │
│  Service Layer (TypeScript):                                 │
│  - extractFileName(url) → "document.pdf"                     │
│  - Map snake_case → camelCase                                │
│                         ↓                                     │
│  API Response (JSON):                                        │
│  {                                                           │
│    "id": "...",                                              │
│    "taskId": "...",          ← camelCase                     │
│    "fileName": "doc.pdf",    ← EXTRACTED from url            │
│    "mimeType": "...",        ← camelCase                     │
│    "uploadedBy": "...",      ← camelCase                     │
│    "createdAt": "2025-..."   ← ISO 8601 string               │
│  }                                                           │
└──────────────────────────────────────────────────────────────┘
                         ↓ HTTP Response
┌──────────────────────────────────────────────────────────────┐
│                    ANDROID CLIENT                            │
├──────────────────────────────────────────────────────────────┤
│  AttachmentDTO (Data Layer):                                │
│  ┌────────────────────────────────────────────────────┐     │
│  │ public String id;                                   │     │
│  │ public String taskId;                               │     │
│  │ @SerializedName("file_name")                        │     │
│  │ public String fileName;      ← Gson auto-maps       │     │
│  │ @SerializedName("mime_type")                        │     │
│  │ public String mimeType;                             │     │
│  │ public String createdAt;     ← Still String         │     │
│  └────────────────────────────────────────────────────┘     │
│                         ↓ AttachmentMapper                   │
│  Attachment (Domain Model):                                  │
│  ┌────────────────────────────────────────────────────┐     │
│  │ private final String id;                            │     │
│  │ private final String fileName;                      │     │
│  │ private final Date createdAt;   ← Parsed to Date    │     │
│  │                                                      │     │
│  │ + isImage(): boolean                                │     │
│  │ + isPdf(): boolean                                  │     │
│  │ + getFormattedSize(): String                        │     │
│  └────────────────────────────────────────────────────┘     │
│                         ↓ Used in                            │
│  UI Layer:                                                   │
│  - AttachmentsFragment                                       │
│  - AttachmentAdapter (RecyclerView)                         │
│  - CardDetailActivity                                        │
└──────────────────────────────────────────────────────────────┘
```

---

## 🔑 Key Differences: DTO vs Domain Model

| Aspect               | AttachmentDTO                | Attachment (Domain)           |
| -------------------- | ---------------------------- | ----------------------------- |
| **Package**          | `data.remote.dto`            | `domain.model`                |
| **Fields**           | Public                       | Private final (immutable)     |
| **Access**           | Direct field access          | Getter methods only           |
| **createdAt Type**   | `String` (ISO 8601)          | `Date` object                 |
| **Gson Annotations** | ✅ Yes (`@SerializedName`)   | ❌ No                         |
| **Business Logic**   | ❌ No                        | ✅ Yes (isImage, isPdf, etc.) |
| **Purpose**          | Network layer (JSON mapping) | Business logic & UI           |
| **Mutable**          | ✅ Yes (can assign fields)   | ❌ No (immutable)             |

---

## 🚀 Usage Examples

### **1. API Call (Retrofit)**:

```java
@GET("api/tasks/{taskId}/attachments")
Call<List<AttachmentDTO>> listAttachments(@Path("taskId") String taskId);
```

**Response**:

```json
[
  {
    "id": "550e8400-...",
    "taskId": "660e8400-...",
    "url": "https://firebasestorage.../1698765432-report.pdf",
    "fileName": "report.pdf",
    "mimeType": "application/pdf",
    "size": 2048576,
    "uploadedBy": "770e8400-...",
    "createdAt": "2025-10-29T10:30:45.123Z"
  }
]
```

---

### **2. Repository Layer** (Convert DTO → Domain):

```java
@Override
public void getTaskAttachments(String taskId, TaskCallback<List<Attachment>> callback) {
    apiService.getTaskAttachments(taskId).enqueue(new Callback<List<AttachmentDTO>>() {
        @Override
        public void onResponse(Call<List<AttachmentDTO>> call,
                               Response<List<AttachmentDTO>> response) {
            if (response.isSuccessful() && response.body() != null) {
                // Convert DTO → Domain
                List<Attachment> attachments = AttachmentMapper.toDomainList(response.body());
                callback.onSuccess(attachments);
            } else {
                callback.onError("Failed to load attachments");
            }
        }

        @Override
        public void onFailure(Call<List<AttachmentDTO>> call, Throwable t) {
            callback.onError(t.getMessage());
        }
    });
}
```

---

### **3. UI Layer** (Display in RecyclerView):

```java
// AttachmentAdapter.java
@Override
public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    Attachment attachment = attachments.get(position);

    holder.tvFileName.setText(attachment.getFileName());
    holder.tvFileSize.setText(attachment.getFormattedSize()); // "1.2 MB"

    // Set icon based on file type
    if (attachment.isImage()) {
        holder.ivFileIcon.setImageResource(R.drawable.ic_image);
        // Load thumbnail
        Glide.with(holder.itemView)
            .load(attachment.getUrl())
            .into(holder.ivFileIcon);
    } else if (attachment.isPdf()) {
        holder.ivFileIcon.setImageResource(R.drawable.ic_pdf);
    } else if (attachment.isDocument()) {
        holder.ivFileIcon.setImageResource(R.drawable.ic_document);
    } else {
        holder.ivFileIcon.setImageResource(R.drawable.ic_file);
    }

    // Format timestamp
    if (attachment.getCreatedAt() != null) {
        String timeAgo = DateUtils.getRelativeTimeSpanString(
            attachment.getCreatedAt().getTime(),
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString();
        holder.tvUploadTime.setText(timeAgo); // "2 hours ago"
    }
}
```

---

## ⚠️ Important Notes & Gotchas

### **1. fileName Field**:

- ❌ **NOT stored in database**
- ✅ **Extracted from `url` field** by backend
- 🔧 Backend logic: `extractFileName(url)` removes timestamp prefix
- 📝 Example: `1698765432-report.pdf` → `report.pdf`

### **2. Date Handling**:

- Backend sends: `"2025-10-29T10:30:45.123Z"` (ISO 8601 string)
- AttachmentDTO: Stores as `String`
- Domain Model: Converts to `Date` object via `SimpleDateFormat`
- UI: Use `DateUtils.getRelativeTimeSpanString()` for "2h ago"

### **3. Nullable Fields**:

- `mimeType`, `size`, `uploadedBy` can be **null**
- Always check before using:
  ```java
  if (attachment.getMimeType() != null) {
      // Use mimeType
  }
  ```

### **4. DTO Field Access**:

- AttachmentDTO uses **public fields**, NOT setters:

  ```java
  // ✅ CORRECT
  dto.id = "123";
  dto.fileName = "doc.pdf";

  // ❌ WRONG (no setter methods)
  dto.setId("123");  // Compile error!
  ```

### **5. Immutability**:

- Domain model is **immutable** (final fields)
- Cannot change after creation:
  ```java
  Attachment att = new Attachment(...);
  att.fileName = "new.pdf";  // ❌ Compile error!
  ```

---

## 🔍 File Type Detection

### **MIME Type Examples**:

| File Type      | MIME Type                                                                   | Detection Method                |
| -------------- | --------------------------------------------------------------------------- | ------------------------------- |
| **Images**     | `image/png`, `image/jpeg`, `image/gif`                                      | `mimeType.startsWith("image/")` |
| **PDF**        | `application/pdf`                                                           | `equals("application/pdf")`     |
| **Word**       | `application/vnd.openxmlformats-officedocument.wordprocessingml.document`   | `contains("word")`              |
| **Excel**      | `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`         | `contains("excel")`             |
| **PowerPoint** | `application/vnd.openxmlformats-officedocument.presentationml.presentation` | `contains("powerpoint")`        |
| **Text**       | `text/plain`                                                                | `startsWith("text/")`           |
| **Video**      | `video/mp4`, `video/quicktime`                                              | `startsWith("video/")`          |

---

## 📦 Upload Flow (2-Step Process)

### **Step 1: Request Upload URL**

**Request**:

```java
RequestAttachmentUploadDto dto = new RequestAttachmentUploadDto();
dto.fileName = "report.pdf";
dto.mimeType = "application/pdf";
dto.size = 2048576; // bytes

attachmentApiService.requestUploadUrl(taskId, dto);
```

**Response**:

```json
{
  "attachmentId": "550e8400-...",
  "uploadUrl": "https://firebasestorage.googleapis.com/.../upload?token=...",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600
}
```

### **Step 2: Upload File to Firebase**

```java
// Read file bytes
byte[] fileBytes = readFileBytes(fileUri);
RequestBody body = RequestBody.create(
    MediaType.parse(mimeType),
    fileBytes
);

// PUT to signed URL
attachmentApiService.uploadToSignedUrl(uploadUrl, body);
```

**Result**: File uploaded → Backend auto-updates attachment record with URL

---

## 🎯 Summary

### **Architecture Overview**:

```
Database (PostgreSQL)
    ↓ Prisma ORM
Backend Service (NestJS)
    ↓ REST API (JSON)
AttachmentDTO (Data Layer)
    ↓ AttachmentMapper
Attachment (Domain Model)
    ↓ Use-cases & ViewModel
UI (Fragments, Adapters)
```

### **Key Points**:

- ✅ 3-layer separation (Database → DTO → Domain)
- ✅ fileName extracted from URL (not in DB)
- ✅ DTO uses public fields (Gson mapping)
- ✅ Domain model is immutable with business logic
- ✅ 2-step upload via signed URLs
- ✅ Type-safe date handling (String → Date)
- ✅ MIME type detection for file icons

---

**Document Version**: 1.0  
**Last Updated**: October 29, 2025  
**Author**: Dev Team
