# 📱 Comments & Attachments - Visual Integration Guide

## 🎯 UI Flow Diagram

```
┌─────────────────────────────────────────────────┐
│         CardDetailActivity                      │
│  ┌───────────────────────────────────────────┐ │
│  │  Task Title: "Build login screen"         │ │
│  │  Board: "To Do"                            │ │
│  │  Description: ...                          │ │
│  │                                             │ │
│  │  [Members] [Labels] [Checklist]           │ │
│  │                                             │ │
│  │  ┌──────────────┐  ┌──────────────┐       │ │
│  │  │ Add Comment  │  │ Add Attachment│       │ │
│  │  └──────┬───────┘  └──────┬────────┘       │ │
│  │         │                  │                 │ │
│  └─────────┼──────────────────┼─────────────────┘ │
│            │                  │                   │
│            │                  │                   │
│       (1) Tap             (2) Tap                │
│            │                  │                   │
│            ▼                  ▼                   │
│  ┌─────────────────┐  ┌─────────────────┐       │
│  │ CommentsFragment│  │AttachmentsFragment│      │
│  │  (BottomSheet)  │  │  (BottomSheet)    │      │
│  └─────────────────┘  └─────────────────┘       │
└─────────────────────────────────────────────────┘
```

---

## 💬 Comments BottomSheet Detail

```
┌─────────────────────────────────────────────────┐
│  Comments                                    [X] │
├─────────────────────────────────────────────────┤
│                                                  │
│  ┌────────────────────────────────────────────┐ │
│  │  👤 John Doe              2h ago           │ │
│  │  Great work! Let's review tomorrow         │ │
│  │  [Edit] [Delete]                           │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│  ┌────────────────────────────────────────────┐ │
│  │  👤 Jane Smith            5h ago           │ │
│  │  I'll add the mockups @John                │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│  ┌────────────────────────────────────────────┐ │
│  │  👤 You                   1d ago            │ │
│  │  Started working on this (edited)          │ │
│  │  [Edit] [Delete]                           │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│  ... (scroll for more)                          │
│                                                  │
├─────────────────────────────────────────────────┤
│  ┌────────────────────────────────┐  ┌──────┐  │
│  │ Write a comment...             │  │ Send │  │
│  └────────────────────────────────┘  └──────┘  │
└─────────────────────────────────────────────────┘
```

**Features**:

- ✅ Scrollable list of comments
- ✅ Real-time timestamps ("2h ago")
- ✅ @mentions highlighted in blue
- ✅ Edit/Delete for own comments only
- ✅ Input box at bottom (always visible)
- ✅ Pagination on scroll up

---

## 📎 Attachments BottomSheet Detail

```
┌─────────────────────────────────────────────────┐
│  Attachments                                 [X] │
├─────────────────────────────────────────────────┤
│                                                  │
│  ┌────────────────────────────────────────────┐ │
│  │  📄 document.pdf          [⬇] [🗑]         │ │
│  │  1.2 MB • John Doe                          │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│  ┌────────────────────────────────────────────┐ │
│  │  🖼️ screenshot.png         [⬇] [🗑]         │ │
│  │  450 KB • Jane Smith                        │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│  ┌────────────────────────────────────────────┐ │
│  │  📊 report.xlsx           [⬇] [🗑]         │ │
│  │  2.5 MB • You                               │ │
│  │  ▓▓▓▓▓▓▓▓░░░░ 65% uploading...            │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│                                                  │
│                                     ┌──────────┐ │
│                                     │   📤    │ │
│                                     │  Upload  │ │
│                                     │   File   │ │
│                                     └──────────┘ │
└─────────────────────────────────────────────────┘
```

**Features**:

- ✅ File type icons (PDF, Image, Excel, etc.)
- ✅ File size and uploader name
- ✅ Download button (⬇)
- ✅ Delete button (🗑) for own files
- ✅ Upload progress bar during upload
- ✅ FAB button to add new file

---

## 🔄 Interaction Flow

### **Opening Comments**:

```
User Action          →  App Response
─────────────────────────────────────────────────
1. Tap "Add Comment" →  Validate task is saved
                     →  If not saved: Show toast
                     →  If saved: Continue

2. Show BottomSheet  →  CommentsFragment.newInstance(taskId)
                     →  fragment.show(...)
                     →  BottomSheet slides up

3. Load comments     →  TaskViewModel.loadTaskComments(taskId)
                     →  API call to backend
                     →  LiveData updates
                     →  RecyclerView shows data

4. User types        →  Input field accepts text
                     →  @mentions auto-detect

5. User taps Send    →  Validate not empty
                     →  TaskViewModel.addComment(...)
                     →  API POST to backend
                     →  Reload comments
                     →  New comment appears at top

6. User swipes down  →  BottomSheet dismisses
                     →  Returns to CardDetailActivity
```

---

### **Opening Attachments**:

```
User Action          →  App Response
─────────────────────────────────────────────────
1. Tap "Add Attachment" → Validate task is saved
                        → If not saved: Show toast
                        → If saved: Continue

2. Show BottomSheet     → AttachmentsFragment.newInstance(taskId)
                        → fragment.show(...)
                        → BottomSheet slides up

3. Load attachments     → TaskViewModel.loadTaskAttachments(taskId)
                        → API call to backend
                        → RecyclerView shows files

4. User taps Upload     → File picker intent
                        → Intent.ACTION_GET_CONTENT
                        → System file picker opens

5. User selects file    → onActivityResult receives Uri
                        → AttachmentUploader.uploadFile(...)

6. Upload process       → Step 1: Request upload URL
                        → Backend creates record
                        → Returns signed URL

                        → Step 2: Upload to Firebase
                        → PUT file bytes to signed URL
                        → Progress updates (10%, 30%, 50%, 100%)

                        → Step 3: Register in UI
                        → TaskViewModel.addAttachment(...)
                        → Reload list
                        → File appears in RecyclerView

7. User taps Download   → Get view URL from backend
                        → Open browser/viewer

8. User swipes down     → BottomSheet dismisses
```

---

## 🎨 State Diagrams

### **Comments Fragment States**:

```
        ┌──────────┐
        │  INIT    │
        └────┬─────┘
             │
             ▼
        ┌──────────┐
        │ LOADING  │ ← Show progress spinner
        └────┬─────┘
             │
        ┌────┴─────┐
        │          │
        ▼          ▼
   ┌─────────┐  ┌─────────┐
   │  EMPTY  │  │ SUCCESS │ ← Show comments list
   └─────────┘  └────┬────┘
        │             │
        │             ▼
        │        ┌─────────┐
        │        │ ADDING  │ ← User writing comment
        │        └────┬────┘
        │             │
        │             ▼
        │        ┌─────────┐
        └───────→│ RELOAD  │ ← Refresh list
                 └─────────┘
```

### **Attachments Fragment States**:

```
        ┌──────────┐
        │  INIT    │
        └────┬─────┘
             │
             ▼
        ┌──────────┐
        │ LOADING  │
        └────┬─────┘
             │
        ┌────┴─────┐
        │          │
        ▼          ▼
   ┌─────────┐  ┌─────────┐
   │  EMPTY  │  │ SUCCESS │ ← Show files
   └─────────┘  └────┬────┘
        │             │
        │             ▼
        │        ┌─────────┐
        │        │ PICKING │ ← File picker open
        │        └────┬────┘
        │             │
        │             ▼
        │        ┌──────────┐
        │        │UPLOADING │ ← Progress 0-100%
        │        └────┬─────┘
        │             │
        │        ┌────┴────┐
        │        │         │
        │        ▼         ▼
        │   ┌────────┐  ┌────────┐
        └──→│ ERROR  │  │SUCCESS │
            └────────┘  └────┬───┘
                             │
                             ▼
                        ┌─────────┐
                        │ RELOAD  │
                        └─────────┘
```

---

## 📊 Architecture Layers

```
┌─────────────────────────────────────────────────┐
│                 UI LAYER                         │
│  CardDetailActivity                              │
│    ↓ (opens)                                     │
│  CommentsFragment / AttachmentsFragment          │
│    ↓ (uses)                                      │
│  CommentAdapter / AttachmentAdapter              │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────┐
│            PRESENTATION LAYER                    │
│  TaskViewModel                                   │
│    - LiveData<List<Comment>>                    │
│    - LiveData<List<Attachment>>                 │
│    - loadTaskComments(taskId)                   │
│    - addComment(taskId, comment)                │
│    - loadTaskAttachments(taskId)                │
│    - addAttachment(taskId, attachment)          │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────┐
│              DOMAIN LAYER                        │
│  Use Cases:                                      │
│    - GetTaskCommentsUseCase                     │
│    - AddCommentUseCase                          │
│    - GetTaskAttachmentsUseCase                  │
│    - AddAttachmentUseCase                       │
│                                                  │
│  Repositories:                                   │
│    - ITaskRepository                            │
│    - ICommentRepository                         │
│    - IAttachmentRepository                      │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────┐
│               DATA LAYER                         │
│  TaskRepositoryImpl                             │
│    ↓ (uses)                                      │
│  TaskApiService / CommentApiService /           │
│  AttachmentApiService                           │
│    ↓ (calls)                                     │
│  Retrofit + OkHttp                              │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────┐
│              NETWORK LAYER                       │
│  Backend API (NestJS)                           │
│    - GET /tasks/:id/comments                    │
│    - POST /tasks/:id/comments                   │
│    - GET /tasks/:id/attachments                 │
│    - POST /tasks/:id/attachments/upload-url     │
│  Firebase Storage (for file upload)             │
└─────────────────────────────────────────────────┘
```

---

## 🔐 Security & Validation

### **CardDetailActivity Validation**:

```java
private void openCommentsBottomSheet() {
    // ⚠️ VALIDATION 1: Check edit mode
    if (!isEditMode) {
        Toast.makeText(this, "Please save the task first", ...).show();
        return; // ❌ BLOCKED
    }

    // ⚠️ VALIDATION 2: Check taskId exists
    if (taskId == null || taskId.isEmpty()) {
        Toast.makeText(this, "Please save the task first", ...).show();
        return; // ❌ BLOCKED
    }

    // ✅ PASSED - Open BottomSheet
    CommentsFragment.newInstance(taskId).show(...);
}
```

**Why this validation?**

- Prevents orphaned comments/attachments
- Ensures task exists in backend before adding related data
- User must save task first → gets taskId from backend → then can add comments/files

---

## 💡 Best Practices Implemented

### **1. BottomSheet UX**:

- ✅ Swipe to dismiss
- ✅ Tap outside to close
- ✅ Smooth slide-up animation
- ✅ Backdrop dimming for focus

### **2. Data Management**:

- ✅ Shared ViewModel across fragments
- ✅ LiveData Observer pattern
- ✅ Automatic UI updates on data changes
- ✅ No manual refresh needed

### **3. Error Handling**:

- ✅ Validation before opening fragments
- ✅ Toast messages for user feedback
- ✅ Network error callbacks
- ✅ Upload failure handling

### **4. Performance**:

- ✅ Lazy loading (fragments created only when opened)
- ✅ RecyclerView for efficient list rendering
- ✅ Image loading with Glide (cached)
- ✅ Pagination for comments (load on scroll)

---

## 🚀 Quick Start Guide

### **For Users**:

1. Open any task in CardDetailActivity
2. Make sure task is saved (if new task, tap "Add Card" first)
3. Tap **"Add Comment"** button → Comments BottomSheet opens
4. Tap **"Add Attachment"** button → Attachments BottomSheet opens
5. Interact and swipe down to close

### **For Developers**:

```java
// Open Comments from any Activity
CommentsFragment fragment = CommentsFragment.newInstance(taskId);
fragment.show(getSupportFragmentManager(), "CommentsBottomSheet");

// Open Attachments from any Activity
AttachmentsFragment fragment = AttachmentsFragment.newInstance(taskId);
fragment.show(getSupportFragmentManager(), "AttachmentsBottomSheet");
```

---

## 📱 Screenshots Mock

### Before (Old UI):

```
┌────────────────────┐
│ Add Comment        │  ← Single dialog, basic input
└────────────────────┘
```

### After (New UI):

```
┌────────────────────────────────┐
│ Comments              ╳        │  ← Full-featured BottomSheet
│ ┌──────────────────────────┐  │
│ │ Comment 1                 │  │
│ │ Comment 2                 │  │
│ │ Comment 3...              │  │
│ └──────────────────────────┘  │
│ [Write comment...]      [Send]│
└────────────────────────────────┘
```

---

**Implementation Complete** ✅  
**Ready for Production** 🚀  
**Zero Compile Errors** ✨
