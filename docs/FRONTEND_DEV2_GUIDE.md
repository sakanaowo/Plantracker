# 📱 FRONTEND DEV 2 - IMPLEMENTATION GUIDE

**Developer**: Frontend Dev 2  
**Features**: Comments (#8) + Attachments (#9)  
**Timeline**: 1 day (8 hours)  
**Status**: Ready to start

---

## 🎯 YOUR MISSION

Bạn sẽ implement 2 features có tính năng tương tác cao:
1. **Comments** - Chat-like commenting system với @mentions
2. **Attachments** - File upload/download với Firebase Storage

**Lưu ý quan trọng**:
- ✅ KHÔNG phụ thuộc vào Dev 1
- ✅ KHÔNG chờ Backend (mock data ngay từ đầu)
- ✅ Work song song, integration cuối ngày

---

## 📋 HOUR 1: SETUP & MOCK DATA (08:00 - 09:00)

### **Step 1.1: Create Comments DTOs** (15 min)

**File**: `data/remote/dto/comment/CommentDTO.java`
```java
package com.example.tralalero.data.remote.dto.comment;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CommentDTO {
    @SerializedName("id")
    private String id;

    @SerializedName("taskId")
    private String taskId;

    @SerializedName("userId")
    private String userId;

    @SerializedName("content")
    private String content;

    @SerializedName("mentions")
    private List<String> mentions;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("user")
    private UserInfo user;

    // Nested class for user info
    public static class UserInfo {
        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("avatarUrl")
        private String avatarUrl;

        // Getters & Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getMentions() { return mentions; }
    public void setMentions(List<String> mentions) { this.mentions = mentions; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }
}
```

**File**: `data/remote/dto/comment/CreateCommentDTO.java`
```java
package com.example.tralalero.data.remote.dto.comment;

import com.google.gson.annotations.SerializedName;

public class CreateCommentDTO {
    @SerializedName("content")
    private String content;

    public CreateCommentDTO(String content) {
        this.content = content;
    }

    public String getContent() { return content; }
}
```

**File**: `data/remote/dto/comment/CommentListResponse.java`
```java
package com.example.tralalero.data.remote.dto.comment;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CommentListResponse {
    @SerializedName("data")
    private List<CommentDTO> data;

    @SerializedName("nextCursor")
    private String nextCursor;

    @SerializedName("hasMore")
    private boolean hasMore;

    // Getters & Setters
    public List<CommentDTO> getData() { return data; }
    public void setData(List<CommentDTO> data) { this.data = data; }
    public String getNextCursor() { return nextCursor; }
    public void setNextCursor(String nextCursor) { this.nextCursor = nextCursor; }
    public boolean isHasMore() { return hasMore; }
    public void setHasMore(boolean hasMore) { this.hasMore = hasMore; }
}
```

---

### **Step 1.2: Create Attachments DTOs** (15 min)

**File**: `data/remote/dto/attachment/AttachmentDTO.java`
```java
package com.example.tralalero.data.remote.dto.attachment;

import com.google.gson.annotations.SerializedName;

public class AttachmentDTO {
    @SerializedName("id")
    private String id;

    @SerializedName("taskId")
    private String taskId;

    @SerializedName("fileName")
    private String fileName;

    @SerializedName("fileSize")
    private long fileSize;

    @SerializedName("mimeType")
    private String mimeType;

    @SerializedName("storagePath")
    private String storagePath;

    @SerializedName("uploadedBy")
    private String uploadedBy;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("user")
    private UserInfo user;

    public static class UserInfo {
        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }
}
```

**File**: `data/remote/dto/attachment/RequestUploadDTO.java`
```java
package com.example.tralalero.data.remote.dto.attachment;

import com.google.gson.annotations.SerializedName;

public class RequestUploadDTO {
    @SerializedName("fileName")
    private String fileName;

    @SerializedName("fileSize")
    private long fileSize;

    @SerializedName("mimeType")
    private String mimeType;

    public RequestUploadDTO(String fileName, long fileSize, String mimeType) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
    }

    public String getFileName() { return fileName; }
    public long getFileSize() { return fileSize; }
    public String getMimeType() { return mimeType; }
}
```

**File**: `data/remote/dto/attachment/UploadUrlResponse.java`
```java
package com.example.tralalero.data.remote.dto.attachment;

import com.google.gson.annotations.SerializedName;

public class UploadUrlResponse {
    @SerializedName("uploadUrl")
    private String uploadUrl;

    @SerializedName("attachmentId")
    private String attachmentId;

    @SerializedName("storagePath")
    private String storagePath;

    // Getters & Setters
    public String getUploadUrl() { return uploadUrl; }
    public void setUploadUrl(String uploadUrl) { this.uploadUrl = uploadUrl; }
    public String getAttachmentId() { return attachmentId; }
    public void setAttachmentId(String attachmentId) { this.attachmentId = attachmentId; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
}
```

**File**: `data/remote/dto/attachment/ViewUrlResponse.java`
```java
package com.example.tralalero.data.remote.dto.attachment;

import com.google.gson.annotations.SerializedName;

public class ViewUrlResponse {
    @SerializedName("url")
    private String url;

    @SerializedName("expiresAt")
    private String expiresAt;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
}
```

---

### **Step 1.3: Create API Services** (20 min)

**File**: `data/remote/api/CommentApiService.java`
```java
package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.comment.*;
import retrofit2.Call;
import retrofit2.http.*;

public interface CommentApiService {
    @POST("tasks/{taskId}/comments")
    Call<CommentDTO> createComment(
        @Path("taskId") String taskId,
        @Body CreateCommentDTO dto
    );

    @GET("tasks/{taskId}/comments")
    Call<CommentListResponse> listComments(
        @Path("taskId") String taskId,
        @Query("limit") Integer limit,
        @Query("cursor") String cursor,
        @Query("sortOrder") String sortOrder // "asc" or "desc"
    );

    @PATCH("comments/{commentId}")
    Call<CommentDTO> updateComment(
        @Path("commentId") String commentId,
        @Body CreateCommentDTO dto
    );

    @DELETE("comments/{commentId}")
    Call<Void> deleteComment(
        @Path("commentId") String commentId
    );
}
```

**File**: `data/remote/api/AttachmentApiService.java`
```java
package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.attachment.*;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

public interface AttachmentApiService {
    @POST("tasks/{taskId}/attachments/upload-url")
    Call<UploadUrlResponse> requestUploadUrl(
        @Path("taskId") String taskId,
        @Body RequestUploadDTO dto
    );

    @GET("tasks/{taskId}/attachments")
    Call<List<AttachmentDTO>> listAttachments(
        @Path("taskId") String taskId
    );

    @GET("attachments/{attachmentId}/view-url")
    Call<ViewUrlResponse> getViewUrl(
        @Path("attachmentId") String attachmentId
    );

    @DELETE("attachments/{attachmentId}")
    Call<Void> deleteAttachment(
        @Path("attachmentId") String attachmentId
    );

    // For uploading to Firebase Storage signed URL
    @PUT
    Call<Void> uploadToSignedUrl(
        @Url String url,
        @Body okhttp3.RequestBody file
    );
}
```

---

### **Step 1.4: Test DTOs** (10 min)

```java
// Test comment serialization
Gson gson = new Gson();

CreateCommentDTO commentDto = new CreateCommentDTO("Hello @John");
String json = gson.toJson(commentDto);
Log.d("DTOTest", "Comment JSON: " + json);

// Test attachment upload request
RequestUploadDTO uploadDto = new RequestUploadDTO(
    "document.pdf",
    1024000,
    "application/pdf"
);
String uploadJson = gson.toJson(uploadDto);
Log.d("DTOTest", "Upload JSON: " + uploadJson);
```

---

## 💬 HOUR 2-3: COMMENTS UI (09:00 - 11:00)

### **Step 2.1: Comments List Layout** (45 min)

**File**: `res/layout/fragment_comments.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background">

    <!-- Comments List -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvComments"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:padding="16dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toTopOf="@id/llCommentInput"
        app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager"/>

    <!-- Empty State -->
    <TextView
        android:id="@+id/tvNoComments"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="No comments yet\nBe the first to comment!"
        android:textSize="16sp"
        android:textColor="@color/text_secondary"
        android:textAlignment="center"
        android:visibility="gone"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"/>

    <!-- Loading Indicator -->
    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:visibility="gone"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"/>

    <!-- Comment Input Area -->
    <LinearLayout
        android:id="@+id/llCommentInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="12dp"
        android:background="@color/surface"
        android:elevation="4dp"
        app:layout_constraintBottom_toBottomOf="parent">

        <EditText
            android:id="@+id/etCommentInput"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:hint="Write a comment..."
            android:background="@drawable/bg_comment_input"
            android:padding="12dp"
            android:maxLines="4"
            android:inputType="textMultiLine|textCapSentences"/>

        <ImageButton
            android:id="@+id/btnSendComment"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:src="@drawable/ic_send"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:tint="@color/primary"
            android:layout_marginStart="8dp"/>

    </LinearLayout>

</androidx.constraintlayout.widget.ConstraintLayout>
```

**File**: `res/layout/item_comment.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="12dp">

    <de.hdodenhof.circleimageview.CircleImageView
        android:id="@+id/ivAvatar"
        android:layout_width="40dp"
        android:layout_height="40dp"
        android:src="@drawable/ic_person"
        app:civ_border_width="1dp"
        app:civ_border_color="@color/border"/>

    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:orientation="vertical"
        android:layout_marginStart="12dp">

        <!-- Comment Header -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal">

            <TextView
                android:id="@+id/tvAuthorName"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="John Doe"
                android:textStyle="bold"
                android:textSize="14sp"
                android:textColor="@color/text_primary"/>

            <TextView
                android:id="@+id/tvTimestamp"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="2h ago"
                android:textSize="12sp"
                android:textColor="@color/text_secondary"/>

        </LinearLayout>

        <!-- Comment Content -->
        <TextView
            android:id="@+id/tvCommentContent"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="This is a comment content"
            android:textSize="14sp"
            android:textColor="@color/text_primary"
            android:layout_marginTop="4dp"/>

        <!-- Comment Actions -->
        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginTop="8dp">

            <TextView
                android:id="@+id/tvEdit"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Edit"
                android:textSize="12sp"
                android:textColor="@color/primary"
                android:padding="4dp"
                android:visibility="gone"/>

            <TextView
                android:id="@+id/tvDelete"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Delete"
                android:textSize="12sp"
                android:textColor="@color/error"
                android:padding="4dp"
                android:layout_marginStart="12dp"
                android:visibility="gone"/>

        </LinearLayout>

        <!-- Edit Indicator -->
        <TextView
            android:id="@+id/tvEditedIndicator"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="(edited)"
            android:textSize="11sp"
            android:textColor="@color/text_secondary"
            android:layout_marginTop="4dp"
            android:visibility="gone"/>

    </LinearLayout>

</LinearLayout>
```

---

### **Step 2.2: Comment Adapter với @Mentions Support** (45 min)

**File**: `feature/task/comments/CommentAdapter.java`
```java
package com.example.tralalero.feature.task.comments;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tralalero.R;
import com.example.tralalero.data.remote.dto.comment.CommentDTO;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    
    private List<CommentDTO> comments = new ArrayList<>();
    private String currentUserId;
    private OnCommentActionListener listener;

    public interface OnCommentActionListener {
        void onEditComment(CommentDTO comment);
        void onDeleteComment(CommentDTO comment);
    }

    public CommentAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setOnCommentActionListener(OnCommentActionListener listener) {
        this.listener = listener;
    }

    public void setComments(List<CommentDTO> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    public void addComment(CommentDTO comment) {
        comments.add(0, comment); // Add to top
        notifyItemInserted(0);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommentDTO comment = comments.get(position);
        
        // Set author info
        holder.tvAuthorName.setText(comment.getUser().getName());
        
        // Load avatar
        if (comment.getUser().getAvatarUrl() != null) {
            Glide.with(holder.itemView.getContext())
                .load(comment.getUser().getAvatarUrl())
                .placeholder(R.drawable.ic_person)
                .into(holder.ivAvatar);
        }
        
        // Set content with @mentions highlighting
        SpannableString spannableContent = highlightMentions(comment.getContent());
        holder.tvCommentContent.setText(spannableContent);
        
        // Set timestamp
        holder.tvTimestamp.setText(formatTimestamp(comment.getCreatedAt()));
        
        // Show edited indicator
        if (comment.getUpdatedAt() != null && 
            !comment.getCreatedAt().equals(comment.getUpdatedAt())) {
            holder.tvEditedIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.tvEditedIndicator.setVisibility(View.GONE);
        }
        
        // Show edit/delete actions only for own comments
        boolean isOwnComment = comment.getUserId().equals(currentUserId);
        holder.tvEdit.setVisibility(isOwnComment ? View.VISIBLE : View.GONE);
        holder.tvDelete.setVisibility(isOwnComment ? View.VISIBLE : View.GONE);
        
        // Set click listeners
        holder.tvEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditComment(comment);
        });
        
        holder.tvDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteComment(comment);
        });
    }

    private SpannableString highlightMentions(String content) {
        SpannableString spannable = new SpannableString(content);
        
        // Find all @mentions using regex
        Pattern pattern = Pattern.compile("@\\w+");
        Matcher matcher = pattern.matcher(content);
        
        int mentionColor = ContextCompat.getColor(
            holder.itemView.getContext(), 
            R.color.primary
        );
        
        while (matcher.find()) {
            spannable.setSpan(
                new ForegroundColorSpan(mentionColor),
                matcher.start(),
                matcher.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        
        return spannable;
    }

    private String formatTimestamp(String timestamp) {
        // Convert ISO timestamp to "2h ago" format
        // You can use a library like PrettyTime for this
        return "2h ago"; // Simplified for now
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvAuthorName, tvTimestamp, tvCommentContent;
        TextView tvEdit, tvDelete, tvEditedIndicator;

        ViewHolder(View view) {
            super(view);
            ivAvatar = view.findViewById(R.id.ivAvatar);
            tvAuthorName = view.findViewById(R.id.tvAuthorName);
            tvTimestamp = view.findViewById(R.id.tvTimestamp);
            tvCommentContent = view.findViewById(R.id.tvCommentContent);
            tvEdit = view.findViewById(R.id.tvEdit);
            tvDelete = view.findViewById(R.id.tvDelete);
            tvEditedIndicator = view.findViewById(R.id.tvEditedIndicator);
        }
    }
}
```

---

### **Step 2.3: Pagination Logic** (30 min)

**File**: `feature/task/comments/CommentsFragment.java`
```java
package com.example.tralalero.feature.task.comments;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tralalero.R;

public class CommentsFragment extends Fragment {
    
    private RecyclerView rvComments;
    private EditText etCommentInput;
    private ImageButton btnSendComment;
    private ProgressBar progressBar;
    private TextView tvNoComments;
    
    private CommentAdapter adapter;
    private String nextCursor = null;
    private boolean isLoading = false;
    private boolean hasMore = true;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerView();
        setupScrollListener();
        loadComments(null); // Initial load
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true); // Show newest at bottom (chat-like)
        rvComments.setLayoutManager(layoutManager);
        
        adapter = new CommentAdapter(getCurrentUserId());
        rvComments.setAdapter(adapter);
        
        adapter.setOnCommentActionListener(new CommentAdapter.OnCommentActionListener() {
            @Override
            public void onEditComment(CommentDTO comment) {
                showEditDialog(comment);
            }

            @Override
            public void onDeleteComment(CommentDTO comment) {
                confirmDelete(comment);
            }
        });
    }

    private void setupScrollListener() {
        rvComments.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                LinearLayoutManager layoutManager = 
                    (LinearLayoutManager) recyclerView.getLayoutManager();
                
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = 
                        layoutManager.findFirstVisibleItemPosition();
                    
                    // Load more when scrolling up (to older comments)
                    if (!isLoading && hasMore && 
                        firstVisibleItemPosition == 0) {
                        loadComments(nextCursor);
                    }
                }
            }
        });
    }

    private void loadComments(String cursor) {
        if (isLoading) return;
        
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);
        
        // Call API
        commentRepository.listComments(taskId, 20, cursor, "desc",
            new RepositoryCallback<CommentListResponse>() {
                @Override
                public void onSuccess(CommentListResponse response) {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    
                    if (response.getData().isEmpty() && cursor == null) {
                        tvNoComments.setVisibility(View.VISIBLE);
                    } else {
                        tvNoComments.setVisibility(View.GONE);
                        adapter.addComments(response.getData());
                    }
                    
                    nextCursor = response.getNextCursor();
                    hasMore = response.isHasMore();
                }

                @Override
                public void onError(String error) {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), 
                        "Failed to load comments", 
                        Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
}
```

---

## 📎 HOUR 4-5: ATTACHMENTS UI (11:00 - 13:00)

### **Step 4.1: Attachments List Layout** (45 min)

**File**: `res/layout/fragment_attachments.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvAttachments"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:padding="16dp"/>

    <TextView
        android:id="@+id/tvNoAttachments"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:text="No attachments yet"
        android:textSize="16sp"
        android:visibility="gone"/>

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fabAddAttachment"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:layout_margin="16dp"
        android:src="@drawable/ic_attach_file"/>

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

**File**: `res/layout/item_attachment.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardElevation="2dp"
    app:cardCornerRadius="8dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="16dp"
        android:gravity="center_vertical">

        <!-- File Type Icon -->
        <ImageView
            android:id="@+id/ivFileIcon"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:src="@drawable/ic_file"
            android:scaleType="centerInside"/>

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical"
            android:layout_marginStart="12dp">

            <TextView
                android:id="@+id/tvFileName"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="document.pdf"
                android:textSize="16sp"
                android:textStyle="bold"
                android:textColor="@color/text_primary"
                android:maxLines="1"
                android:ellipsize="middle"/>

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_marginTop="4dp">

                <TextView
                    android:id="@+id/tvFileSize"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="1.2 MB"
                    android:textSize="12sp"
                    android:textColor="@color/text_secondary"/>

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text=" • "
                    android:textColor="@color/text_secondary"/>

                <TextView
                    android:id="@+id/tvUploadedBy"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="John Doe"
                    android:textSize="12sp"
                    android:textColor="@color/text_secondary"/>

            </LinearLayout>

            <!-- Upload Progress (only visible during upload) -->
            <ProgressBar
                android:id="@+id/progressUpload"
                style="@style/Widget.AppCompat.ProgressBar.Horizontal"
                android:layout_width="match_parent"
                android:layout_height="4dp"
                android:layout_marginTop="8dp"
                android:visibility="gone"/>

        </LinearLayout>

        <!-- Action Buttons -->
        <ImageButton
            android:id="@+id/btnDownload"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:src="@drawable/ic_download"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:padding="8dp"/>

        <ImageButton
            android:id="@+id/btnDelete"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:src="@drawable/ic_delete"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:padding="8dp"
            android:layout_marginStart="4dp"/>

    </LinearLayout>

</com.google.android.material.card.MaterialCardView>
```

---

### **Step 4.2: File Upload Logic (2-Step Process)** (60 min)

**File**: `feature/task/attachments/AttachmentUploader.java`
```java
package com.example.tralalero.feature.task.attachments;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import com.example.tralalero.data.remote.api.AttachmentApiService;
import com.example.tralalero.data.remote.dto.attachment.*;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.*;

public class AttachmentUploader {
    
    private AttachmentApiService apiService;
    private Context context;

    public interface UploadCallback {
        void onProgress(int percent);
        void onSuccess(AttachmentDTO attachment);
        void onError(String error);
    }

    public AttachmentUploader(AttachmentApiService apiService, Context context) {
        this.apiService = apiService;
        this.context = context;
    }

    public void uploadFile(String taskId, Uri fileUri, UploadCallback callback) {
        try {
            // Step 1: Get file info
            String fileName = getFileName(fileUri);
            long fileSize = getFileSize(fileUri);
            String mimeType = getMimeType(fileUri);
            
            callback.onProgress(10); // 10% - File info retrieved
            
            // Step 2: Request upload URL from backend
            RequestUploadDTO uploadRequest = new RequestUploadDTO(
                fileName, fileSize, mimeType
            );
            
            apiService.requestUploadUrl(taskId, uploadRequest)
                .enqueue(new Callback<UploadUrlResponse>() {
                    @Override
                    public void onResponse(Call<UploadUrlResponse> call,
                                         Response<UploadUrlResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onProgress(30); // 30% - Upload URL received
                            
                            String uploadUrl = response.body().getUploadUrl();
                            String attachmentId = response.body().getAttachmentId();
                            
                            // Step 3: Upload file to Firebase Storage
                            uploadToFirebase(uploadUrl, fileUri, attachmentId, callback);
                        } else {
                            callback.onError("Failed to get upload URL");
                        }
                    }

                    @Override
                    public void onFailure(Call<UploadUrlResponse> call, Throwable t) {
                        callback.onError("Network error: " + t.getMessage());
                    }
                });
                
        } catch (Exception e) {
            callback.onError("Error reading file: " + e.getMessage());
        }
    }

    private void uploadToFirebase(String uploadUrl, Uri fileUri, 
                                  String attachmentId, UploadCallback callback) {
        try {
            // Read file into byte array
            InputStream inputStream = context.getContentResolver()
                .openInputStream(fileUri);
            
            byte[] fileBytes = readBytes(inputStream);
            String mimeType = getMimeType(fileUri);
            
            RequestBody requestBody = RequestBody.create(
                MediaType.parse(mimeType),
                fileBytes
            );
            
            callback.onProgress(50); // 50% - Starting upload
            
            // Upload to Firebase Storage signed URL
            apiService.uploadToSignedUrl(uploadUrl, requestBody)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            callback.onProgress(100); // 100% - Upload complete
                            
                            // Create attachment DTO for UI
                            AttachmentDTO attachment = new AttachmentDTO();
                            attachment.setId(attachmentId);
                            attachment.setFileName(getFileName(fileUri));
                            attachment.setFileSize(fileBytes.length);
                            attachment.setMimeType(mimeType);
                            
                            callback.onSuccess(attachment);
                        } else {
                            callback.onError("Upload failed: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        callback.onError("Upload failed: " + t.getMessage());
                    }
                });
                
        } catch (Exception e) {
            callback.onError("Error uploading file: " + e.getMessage());
        }
    }

    private String getFileName(Uri uri) {
        // Extract filename from URI
        String path = uri.getPath();
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private long getFileSize(Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        return inputStream.available();
    }

    private String getMimeType(Uri uri) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    private byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        
        return buffer.toByteArray();
    }
}
```

---

### **Step 4.3: File Picker Integration** (30 min)

**File**: `feature/task/attachments/AttachmentsFragment.java`
```java
package com.example.tralalero.feature.task.attachments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class AttachmentsFragment extends Fragment {
    
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private AttachmentUploader uploader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize file picker launcher
        filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && 
                    result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    uploadFile(fileUri);
                }
            }
        );
        
        uploader = new AttachmentUploader(apiService, requireContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        fabAddAttachment.setOnClickListener(v -> openFilePicker());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // All file types
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        
        // Optional: Restrict to specific types
        String[] mimeTypes = {
            "image/*", 
            "application/pdf", 
            "application/msword",
            "application/vnd.ms-excel"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        
        filePickerLauncher.launch(Intent.createChooser(intent, "Select File"));
    }

    private void uploadFile(Uri fileUri) {
        // Add placeholder item to show upload progress
        AttachmentDTO placeholder = createPlaceholder(fileUri);
        adapter.addAttachment(placeholder);
        
        uploader.uploadFile(taskId, fileUri, new AttachmentUploader.UploadCallback() {
            @Override
            public void onProgress(int percent) {
                // Update progress in adapter
                adapter.updateProgress(placeholder.getId(), percent);
            }

            @Override
            public void onSuccess(AttachmentDTO attachment) {
                // Replace placeholder with real attachment
                adapter.replaceAttachment(placeholder.getId(), attachment);
                Toast.makeText(getContext(), 
                    "File uploaded successfully", 
                    Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                // Remove placeholder
                adapter.removeAttachment(placeholder.getId());
                Toast.makeText(getContext(), 
                    "Upload failed: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
}
```

---

## 🔧 HOUR 6: REPOSITORY LAYER (14:00 - 15:00)

### **Step 6.1: Comment Repository** (30 min)

```java
public interface ICommentRepository {
    void createComment(String taskId, String content, 
        RepositoryCallback<CommentDTO> callback);
    
    void listComments(String taskId, int limit, String cursor, String sortOrder,
        RepositoryCallback<CommentListResponse> callback);
    
    void updateComment(String commentId, String content,
        RepositoryCallback<CommentDTO> callback);
    
    void deleteComment(String commentId,
        RepositoryCallback<Void> callback);
}
```

---

### **Step 6.2: Attachment Repository** (30 min)

```java
public interface IAttachmentRepository {
    void requestUploadUrl(String taskId, RequestUploadDTO dto,
        RepositoryCallback<UploadUrlResponse> callback);
    
    void listAttachments(String taskId,
        RepositoryCallback<List<AttachmentDTO>> callback);
    
    void getViewUrl(String attachmentId,
        RepositoryCallback<ViewUrlResponse> callback);
    
    void deleteAttachment(String attachmentId,
        RepositoryCallback<Void> callback);
}
```

---

## 🧠 HOUR 7: VIEWMODEL LAYER (15:00 - 16:00)

Similar pattern như Dev 1, implement ViewModels cho Comments và Attachments.

---

## ✅ HOUR 8: INTEGRATION & TESTING (16:00 - 17:00)

### **Step 8.1: Test Comments Flow** (30 min)

- [ ] Open task detail
- [ ] Write comment với @mention
- [ ] Verify @mention highlighted
- [ ] Edit own comment
- [ ] Delete own comment
- [ ] Test pagination (scroll to load more)

---

### **Step 8.2: Test Attachments Flow** (30 min)

- [ ] Click add attachment
- [ ] Select file from device
- [ ] Watch upload progress
- [ ] Verify file appears in list
- [ ] Click download → Verify opens
- [ ] Delete attachment

---

## 📊 DELIVERABLES CHECKLIST

### **Comments Feature**:
- [ ] 4 DTOs created
- [ ] CommentApiService interface
- [ ] Comment layouts (fragment, item)
- [ ] CommentAdapter với @mentions
- [ ] Pagination logic
- [ ] CommentRepository
- [ ] CommentsViewModel
- [ ] CommentsFragment
- [ ] All flows tested

### **Attachments Feature**:
- [ ] 5 DTOs created
- [ ] AttachmentApiService interface
- [ ] Attachment layouts
- [ ] AttachmentAdapter
- [ ] AttachmentUploader (2-step)
- [ ] File picker integration
- [ ] AttachmentRepository
- [ ] AttachmentsViewModel
- [ ] AttachmentsFragment
- [ ] All flows tested

---

## 🐛 COMMON ISSUES & FIXES

### **Issue 1: File Upload Permission Denied**
```
Error: Permission denied reading file
```
**Fix**: Add permission to `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
```

---

### **Issue 2: Large File Upload Timeout**
```
Error: SocketTimeoutException
```
**Fix**: Increase timeout:
```java
OkHttpClient client = new OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .build();
```

---

### **Issue 3: @Mentions Not Parsing**
```
@John shows as plain text
```
**Fix**: Use Pattern.compile():
```java
Pattern pattern = Pattern.compile("@\\w+");
Matcher matcher = pattern.matcher(content);
```

---

## 💡 BEST PRACTICES

1. **File Size Validation**:
```java
if (fileSize > 10 * 1024 * 1024) { // 10MB limit
    Toast.makeText(context, "File too large (max 10MB)", Toast.LENGTH_SHORT).show();
    return;
}
```

2. **MIME Type Icons**:
```java
private int getFileIcon(String mimeType) {
    if (mimeType.startsWith("image/")) return R.drawable.ic_image;
    if (mimeType.equals("application/pdf")) return R.drawable.ic_pdf;
    if (mimeType.contains("word")) return R.drawable.ic_word;
    return R.drawable.ic_file;
}
```

3. **Comment Timestamps**:
Use library `org.ocpsoft.prettytime:prettytime:5.0.2.Final`:
```java
PrettyTime prettyTime = new PrettyTime();
String relativeTime = prettyTime.format(new Date(timestamp));
```

---

## 📞 HELP & SUPPORT

**File Upload Issues?**
- Backend docs: `POSTMAN_WEEK1_TESTING_GUIDE.md`
- Test 2-step upload with Postman first
- Check Firebase Storage permissions

**Pagination Not Working?**
- Verify `nextCursor` from API response
- Check scroll direction (reverse layout for chat)

---

**Good luck Dev 2! 💪**

**Status**: ✅ Ready to code  
**Next**: Start at 08:00 tomorrow 🚀
