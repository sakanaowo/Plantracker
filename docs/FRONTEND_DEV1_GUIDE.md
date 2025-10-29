# 📱 FRONTEND DEV 1 - IMPLEMENTATION GUIDE

**Developer**: Frontend Dev 1  
**Features**: Team Members (#7) + Labels (#10)  
**Timeline**: 1 day (8 hours)  
**Status**: Ready to start

---

## 🎯 YOUR MISSION

Bạn sẽ implement 2 features hoàn toàn độc lập:
1. **Team Members** - Quản lý thành viên trong project
2. **Labels** - Quản lý nhãn cho tasks

**Lưu ý quan trọng**:
- ✅ KHÔNG phụ thuộc vào Dev 2
- ✅ KHÔNG chờ Backend (mock data ngay từ đầu)
- ✅ Work song song, integration cuối ngày

---

## 📋 HOUR 1: SETUP & MOCK DATA (08:00 - 09:00)

### **Step 1.1: Create Team Members DTOs** (15 min)

**File**: `data/remote/dto/member/MemberDTO.java`
```java
package com.example.tralalero.data.remote.dto.member;

import com.google.gson.annotations.SerializedName;

public class MemberDTO {
    @SerializedName("id")
    private String id;

    @SerializedName("userId")
    private String userId;

    @SerializedName("role")
    private String role; // OWNER, ADMIN, MEMBER, VIEWER

    @SerializedName("user")
    private UserInfo user;

    @SerializedName("addedBy")
    private String addedBy;

    @SerializedName("createdAt")
    private String createdAt;

    // Nested class for user info
    public static class UserInfo {
        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("email")
        private String email;

        @SerializedName("avatarUrl")
        private String avatarUrl;

        // Getters & Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }
    public String getAddedBy() { return addedBy; }
    public void setAddedBy(String addedBy) { this.addedBy = addedBy; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
```

**File**: `data/remote/dto/member/InviteMemberDTO.java`
```java
package com.example.tralalero.data.remote.dto.member;

import com.google.gson.annotations.SerializedName;

public class InviteMemberDTO {
    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    public InviteMemberDTO(String email, String role) {
        this.email = email;
        this.role = role;
    }

    // Getters
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
```

**File**: `data/remote/dto/member/UpdateMemberRoleDTO.java`
```java
package com.example.tralalero.data.remote.dto.member;

import com.google.gson.annotations.SerializedName;

public class UpdateMemberRoleDTO {
    @SerializedName("role")
    private String role;

    public UpdateMemberRoleDTO(String role) {
        this.role = role;
    }

    public String getRole() { return role; }
}
```

---

### **Step 1.2: Create Labels DTOs** (15 min)

**File**: `data/remote/dto/label/LabelDTO.java`
```java
package com.example.tralalero.data.remote.dto.label;

import com.google.gson.annotations.SerializedName;

public class LabelDTO {
    @SerializedName("id")
    private String id;

    @SerializedName("projectId")
    private String projectId;

    @SerializedName("name")
    private String name;

    @SerializedName("color")
    private String color;

    @SerializedName("taskCount")
    private Integer taskCount;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Integer getTaskCount() { return taskCount; }
    public void setTaskCount(Integer taskCount) { this.taskCount = taskCount; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
```

**File**: `data/remote/dto/label/CreateLabelDTO.java`
```java
package com.example.tralalero.data.remote.dto.label;

import com.google.gson.annotations.SerializedName;

public class CreateLabelDTO {
    @SerializedName("name")
    private String name;

    @SerializedName("color")
    private String color;

    public CreateLabelDTO(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public String getName() { return name; }
    public String getColor() { return color; }
}
```

**File**: `data/remote/dto/label/AssignLabelDTO.java`
```java
package com.example.tralalero.data.remote.dto.label;

import com.google.gson.annotations.SerializedName;

public class AssignLabelDTO {
    @SerializedName("labelId")
    private String labelId;

    public AssignLabelDTO(String labelId) {
        this.labelId = labelId;
    }

    public String getLabelId() { return labelId; }
}
```

---

### **Step 1.3: Create Mock API Services** (20 min)

**File**: `data/remote/api/MemberApiService.java`
```java
package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.member.*;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

public interface MemberApiService {
    @POST("projects/{projectId}/members/invite")
    Call<MemberDTO> inviteMember(
        @Path("projectId") String projectId,
        @Body InviteMemberDTO dto
    );

    @GET("projects/{projectId}/members")
    Call<MemberListResponse> getMembers(
        @Path("projectId") String projectId
    );

    @PATCH("projects/{projectId}/members/{memberId}")
    Call<MemberDTO> updateMemberRole(
        @Path("projectId") String projectId,
        @Path("memberId") String memberId,
        @Body UpdateMemberRoleDTO dto
    );

    @DELETE("projects/{projectId}/members/{memberId}")
    Call<Void> removeMember(
        @Path("projectId") String projectId,
        @Path("memberId") String memberId
    );

    // Response wrapper
    class MemberListResponse {
        public List<MemberDTO> data;
        public int count;
    }
}
```

**File**: `data/remote/api/LabelApiService.java`
```java
package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.label.*;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

public interface LabelApiService {
    @POST("projects/{projectId}/labels")
    Call<LabelDTO> createLabel(
        @Path("projectId") String projectId,
        @Body CreateLabelDTO dto
    );

    @GET("projects/{projectId}/labels")
    Call<List<LabelDTO>> getProjectLabels(
        @Path("projectId") String projectId
    );

    @PATCH("labels/{labelId}")
    Call<LabelDTO> updateLabel(
        @Path("labelId") String labelId,
        @Body CreateLabelDTO dto
    );

    @DELETE("labels/{labelId}")
    Call<Void> deleteLabel(
        @Path("labelId") String labelId
    );

    @POST("tasks/{taskId}/labels")
    Call<AssignLabelResponse> assignLabel(
        @Path("taskId") String taskId,
        @Body AssignLabelDTO dto
    );

    @GET("tasks/{taskId}/labels")
    Call<List<LabelDTO>> getTaskLabels(
        @Path("taskId") String taskId
    );

    @DELETE("tasks/{taskId}/labels/{labelId}")
    Call<Void> removeLabel(
        @Path("taskId") String taskId,
        @Path("labelId") String labelId
    );

    // Response wrapper
    class AssignLabelResponse {
        public boolean success;
        public LabelDTO label;
    }
}
```

---

### **Step 1.4: Test DTOs with Logcat** (10 min)

Tạo test để verify DTOs serialize đúng:

```java
// Test serialization
Gson gson = new Gson();

// Test Member DTO
MemberDTO member = new MemberDTO();
member.setRole("ADMIN");
String json = gson.toJson(member);
Log.d("DTOTest", "Member JSON: " + json);

// Test Label DTO
LabelDTO label = new LabelDTO();
label.setName("Bug");
label.setColor("#FF6B6B");
String labelJson = gson.toJson(label);
Log.d("DTOTest", "Label JSON: " + labelJson);
```

---

## 📱 HOUR 2-3: TEAM MEMBERS UI (09:00 - 11:00)

### **Step 2.1: Members List Layout** (45 min)

**File**: `res/layout/fragment_members.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvMembers"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:padding="16dp"
        app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager"/>

    <TextView
        android:id="@+id/tvNoMembers"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:text="No members yet"
        android:textSize="16sp"
        android:textColor="@color/text_secondary"
        android:visibility="gone"/>

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fabInviteMember"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:layout_margin="16dp"
        android:src="@drawable/ic_person_add"
        app:tint="@color/white"/>

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

**File**: `res/layout/item_member.xml`
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

        <de.hdodenhof.circleimageview.CircleImageView
            android:id="@+id/ivAvatar"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:src="@drawable/ic_person"
            app:civ_border_width="2dp"
            app:civ_border_color="@color/primary"/>

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical"
            android:layout_marginStart="12dp">

            <TextView
                android:id="@+id/tvMemberName"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="John Doe"
                android:textSize="16sp"
                android:textStyle="bold"
                android:textColor="@color/text_primary"/>

            <TextView
                android:id="@+id/tvMemberEmail"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="john@example.com"
                android:textSize="14sp"
                android:textColor="@color/text_secondary"
                android:layout_marginTop="4dp"/>

        </LinearLayout>

        <com.google.android.material.chip.Chip
            android:id="@+id/chipRole"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="MEMBER"
            android:textColor="@color/white"
            app:chipBackgroundColor="@color/primary"
            android:layout_marginStart="8dp"/>

        <ImageButton
            android:id="@+id/btnMoreOptions"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:src="@drawable/ic_more_vert"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:padding="8dp"/>

    </LinearLayout>

</com.google.android.material.card.MaterialCardView>
```

---

### **Step 2.2: Invite Member Dialog** (45 min)

**File**: `res/layout/dialog_invite_member.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="24dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Invite Member"
        android:textSize="20sp"
        android:textStyle="bold"
        android:textColor="@color/text_primary"/>

    <com.google.android.material.textfield.TextInputLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:hint="Email">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/etEmail"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:inputType="textEmailAddress"/>

    </com.google.android.material.textfield.TextInputLayout>

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Role"
        android:textSize="14sp"
        android:layout_marginTop="16dp"/>

    <Spinner
        android:id="@+id/spinnerRole"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:padding="12dp"
        android:background="@drawable/bg_spinner"/>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginTop="24dp"
        android:gravity="end">

        <Button
            android:id="@+id/btnCancel"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Cancel"
            style="@style/Widget.MaterialComponents.Button.TextButton"/>

        <Button
            android:id="@+id/btnInvite"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Send Invite"
            android:layout_marginStart="8dp"/>

    </LinearLayout>

</LinearLayout>
```

**File**: `feature/project/members/InviteMemberDialog.java`
```java
package com.example.tralalero.feature.project.members;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.tralalero.R;

public class InviteMemberDialog extends DialogFragment {
    
    private EditText etEmail;
    private Spinner spinnerRole;
    private OnInviteListener listener;

    public interface OnInviteListener {
        void onInvite(String email, String role);
    }

    public void setOnInviteListener(OnInviteListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
            .inflate(R.layout.dialog_invite_member, null);

        etEmail = view.findViewById(R.id.etEmail);
        spinnerRole = view.findViewById(R.id.spinnerRole);

        // Setup role spinner
        String[] roles = {"MEMBER", "ADMIN", "VIEWER"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            getContext(),
            android.R.layout.simple_spinner_item,
            roles
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btnInvite).setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String role = spinnerRole.getSelectedItem().toString();
            
            if (email.isEmpty()) {
                etEmail.setError("Email required");
                return;
            }
            
            if (listener != null) {
                listener.onInvite(email, role);
            }
            dismiss();
        });

        return new AlertDialog.Builder(requireContext())
            .setView(view)
            .create();
    }
}
```

---

### **Step 2.3: Member Adapter** (30 min)

**File**: `feature/project/members/MemberAdapter.java`
```java
package com.example.tralalero.feature.project.members;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tralalero.R;
import com.example.tralalero.data.remote.dto.member.MemberDTO;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
    
    private List<MemberDTO> members = new ArrayList<>();
    private OnMemberActionListener listener;

    public interface OnMemberActionListener {
        void onChangeRole(MemberDTO member);
        void onRemoveMember(MemberDTO member);
    }

    public void setOnMemberActionListener(OnMemberActionListener listener) {
        this.listener = listener;
    }

    public void setMembers(List<MemberDTO> members) {
        this.members = members;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MemberDTO member = members.get(position);
        
        holder.tvName.setText(member.getUser().getName());
        holder.tvEmail.setText(member.getUser().getEmail());
        holder.chipRole.setText(member.getRole());
        
        // Set role color
        int roleColor = getRoleColor(member.getRole());
        holder.chipRole.setChipBackgroundColorResource(roleColor);
        
        // Load avatar
        if (member.getUser().getAvatarUrl() != null) {
            Glide.with(holder.itemView.getContext())
                .load(member.getUser().getAvatarUrl())
                .placeholder(R.drawable.ic_person)
                .into(holder.ivAvatar);
        }
        
        // More options menu
        holder.btnMoreOptions.setOnClickListener(v -> showOptionsMenu(v, member));
    }

    private void showOptionsMenu(View anchor, MemberDTO member) {
        PopupMenu popup = new PopupMenu(anchor.getContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.menu_member_options, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_change_role) {
                if (listener != null) listener.onChangeRole(member);
                return true;
            } else if (item.getItemId() == R.id.action_remove) {
                if (listener != null) listener.onRemoveMember(member);
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    private int getRoleColor(String role) {
        switch (role) {
            case "OWNER": return R.color.role_owner;
            case "ADMIN": return R.color.role_admin;
            case "VIEWER": return R.color.role_viewer;
            default: return R.color.role_member;
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvName, tvEmail;
        Chip chipRole;
        ImageButton btnMoreOptions;

        ViewHolder(View view) {
            super(view);
            ivAvatar = view.findViewById(R.id.ivAvatar);
            tvName = view.findViewById(R.id.tvMemberName);
            tvEmail = view.findViewById(R.id.tvMemberEmail);
            chipRole = view.findViewById(R.id.chipRole);
            btnMoreOptions = view.findViewById(R.id.btnMoreOptions);
        }
    }
}
```

---

## 🏷️ HOUR 4-5: LABELS UI (11:00 - 13:00)

### **Step 4.1: Labels List Layout** (45 min)

**File**: `res/layout/fragment_labels.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvLabels"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:padding="16dp"/>

    <TextView
        android:id="@+id/tvNoLabels"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:text="No labels yet"
        android:textSize="16sp"
        android:visibility="gone"/>

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fabCreateLabel"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:layout_margin="16dp"
        android:src="@drawable/ic_add"/>

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

**File**: `res/layout/item_label.xml`
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

        <View
            android:id="@+id/vColorIndicator"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:background="@drawable/shape_circle"/>

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical"
            android:layout_marginStart="12dp">

            <TextView
                android:id="@+id/tvLabelName"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Bug"
                android:textSize="16sp"
                android:textStyle="bold"/>

            <TextView
                android:id="@+id/tvTaskCount"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="5 tasks"
                android:textSize="14sp"
                android:textColor="@color/text_secondary"
                android:layout_marginTop="4dp"/>

        </LinearLayout>

        <ImageButton
            android:id="@+id/btnDeleteLabel"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:src="@drawable/ic_delete"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:padding="8dp"/>

    </LinearLayout>

</com.google.android.material.card.MaterialCardView>
```

---

### **Step 4.2: Create Label Dialog với Color Picker** (45 min)

**File**: `res/layout/dialog_create_label.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="24dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Create Label"
        android:textSize="20sp"
        android:textStyle="bold"/>

    <com.google.android.material.textfield.TextInputLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:hint="Label name">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/etLabelName"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"/>

    </com.google.android.material.textfield.TextInputLayout>

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Select Color"
        android:textSize="14sp"
        android:layout_marginTop="16dp"/>

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvColorPalette"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"/>

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Preview"
        android:textSize="14sp"
        android:layout_marginTop="16dp"/>

    <com.google.android.material.chip.Chip
        android:id="@+id/chipPreview"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Label Preview"
        android:textColor="@color/white"
        android:layout_marginTop="8dp"/>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginTop="24dp"
        android:gravity="end">

        <Button
            android:id="@+id/btnCancel"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Cancel"
            style="@style/Widget.MaterialComponents.Button.TextButton"/>

        <Button
            android:id="@+id/btnCreate"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Create"
            android:layout_marginStart="8dp"/>

    </LinearLayout>

</LinearLayout>
```

**File**: `feature/project/labels/ColorPaletteAdapter.java`
```java
package com.example.tralalero.feature.project.labels;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tralalero.R;

public class ColorPaletteAdapter extends RecyclerView.Adapter<ColorPaletteAdapter.ViewHolder> {
    
    // 20 predefined colors from backend
    private static final String[] COLORS = {
        "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8",
        "#F7DC6F", "#BB8FCE", "#85C1E2", "#F8B4D9", "#52B788",
        "#FFD93D", "#6BCF7F", "#95E1D3", "#F38181", "#AA96DA",
        "#FCBAD3", "#A8D8EA", "#FFAAA5", "#FFD3B6", "#DCEDC1"
    };
    
    private int selectedPosition = 0;
    private OnColorSelectedListener listener;

    public interface OnColorSelectedListener {
        void onColorSelected(String color);
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.listener = listener;
    }

    public String getSelectedColor() {
        return COLORS[selectedPosition];
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_color, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String color = COLORS[position];
        holder.vColor.setBackgroundColor(Color.parseColor(color));
        
        // Show selection indicator
        holder.vSelection.setVisibility(
            position == selectedPosition ? View.VISIBLE : View.GONE
        );
        
        holder.itemView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onColorSelected(color);
            }
        });
    }

    @Override
    public int getItemCount() {
        return COLORS.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View vColor, vSelection;

        ViewHolder(View view) {
            super(view);
            vColor = view.findViewById(R.id.vColor);
            vSelection = view.findViewById(R.id.vSelection);
        }
    }
}
```

---

## 🔧 HOUR 6: REPOSITORY LAYER (14:00 - 15:00)

### **Step 6.1: Member Repository** (30 min)

**File**: `data/repository/member/IMemberRepository.java`
```java
package com.example.tralalero.data.repository.member;

import com.example.tralalero.domain.model.Member;
import com.example.tralalero.common.RepositoryCallback;
import java.util.List;

public interface IMemberRepository {
    void inviteMember(String projectId, String email, String role, 
        RepositoryCallback<Member> callback);
    
    void getProjectMembers(String projectId, 
        RepositoryCallback<List<Member>> callback);
    
    void updateMemberRole(String projectId, String memberId, String role,
        RepositoryCallback<Member> callback);
    
    void removeMember(String projectId, String memberId,
        RepositoryCallback<Void> callback);
}
```

**File**: `data/repository/member/MemberRepositoryImpl.java`
```java
package com.example.tralalero.data.repository.member;

import com.example.tralalero.data.remote.api.MemberApiService;
import com.example.tralalero.data.remote.dto.member.*;
import com.example.tralalero.data.mapper.MemberMapper;
import com.example.tralalero.domain.model.Member;
import com.example.tralalero.common.RepositoryCallback;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;
import java.util.stream.Collectors;

public class MemberRepositoryImpl implements IMemberRepository {
    
    private MemberApiService apiService;

    public MemberRepositoryImpl(MemberApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void inviteMember(String projectId, String email, String role,
                            RepositoryCallback<Member> callback) {
        InviteMemberDTO dto = new InviteMemberDTO(email, role);
        
        apiService.inviteMember(projectId, dto).enqueue(new Callback<MemberDTO>() {
            @Override
            public void onResponse(Call<MemberDTO> call, Response<MemberDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Member member = MemberMapper.toDomain(response.body());
                    callback.onSuccess(member);
                } else {
                    callback.onError("Failed to invite member");
                }
            }

            @Override
            public void onFailure(Call<MemberDTO> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    @Override
    public void getProjectMembers(String projectId,
                                 RepositoryCallback<List<Member>> callback) {
        apiService.getMembers(projectId).enqueue(
            new Callback<MemberApiService.MemberListResponse>() {
                @Override
                public void onResponse(Call<MemberApiService.MemberListResponse> call,
                                     Response<MemberApiService.MemberListResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Member> members = response.body().data.stream()
                            .map(MemberMapper::toDomain)
                            .collect(Collectors.toList());
                        callback.onSuccess(members);
                    } else {
                        callback.onError("Failed to load members");
                    }
                }

                @Override
                public void onFailure(Call<MemberApiService.MemberListResponse> call,
                                    Throwable t) {
                    callback.onError(t.getMessage());
                }
            }
        );
    }

    // Similar implementation for updateMemberRole() and removeMember()
}
```

---

### **Step 6.2: Label Repository** (30 min)

Similar pattern như MemberRepository, implement:
- `createLabel(projectId, name, color, callback)`
- `getProjectLabels(projectId, callback)`
- `assignLabel(taskId, labelId, callback)`
- `removeLabel(taskId, labelId, callback)`
- `deleteLabel(labelId, callback)`

---

## 🧠 HOUR 7: VIEWMODEL LAYER (15:00 - 16:00)

### **Step 7.1: Members ViewModel** (30 min)

```java
package com.example.tralalero.feature.project.members;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.tralalero.domain.model.Member;
import com.example.tralalero.data.repository.member.IMemberRepository;
import java.util.List;

public class MembersViewModel extends ViewModel {
    
    private IMemberRepository repository;
    private MutableLiveData<List<Member>> membersLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public MembersViewModel(IMemberRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Member>> getMembers() {
        return membersLiveData;
    }

    public LiveData<Boolean> getLoading() {
        return loadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void loadMembers(String projectId) {
        loadingLiveData.setValue(true);
        
        repository.getProjectMembers(projectId, new RepositoryCallback<List<Member>>() {
            @Override
            public void onSuccess(List<Member> data) {
                loadingLiveData.setValue(false);
                membersLiveData.setValue(data);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void inviteMember(String projectId, String email, String role) {
        loadingLiveData.setValue(true);
        
        repository.inviteMember(projectId, email, role, 
            new RepositoryCallback<Member>() {
                @Override
                public void onSuccess(Member data) {
                    loadingLiveData.setValue(false);
                    // Reload members
                    loadMembers(projectId);
                }

                @Override
                public void onError(String error) {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(error);
                }
            }
        );
    }

    // Similar methods for updateRole() and removeMember()
}
```

---

### **Step 7.2: Labels ViewModel** (30 min)

Similar pattern như MembersViewModel.

---

## ✅ HOUR 8: INTEGRATION & TESTING (16:00 - 17:00)

### **Step 8.1: Replace Mock với Real API** (30 min)

```java
// In RetrofitClient.java or ApiModule.java
public class RetrofitClient {
    private static Retrofit retrofit = null;
    
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:3000/api/") // Use 10.0.2.2 for localhost
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit;
    }
    
    public static MemberApiService getMemberApiService() {
        return getClient().create(MemberApiService.class);
    }
    
    public static LabelApiService getLabelApiService() {
        return getClient().create(LabelApiService.class);
    }
}
```

---

### **Step 8.2: Manual Testing Checklist** (30 min)

**Team Members**:
- [ ] Open project members screen
- [ ] Click invite → Enter email + select role → Send
- [ ] Verify member appears in list
- [ ] Long press member → Change role
- [ ] Long press member → Remove member
- [ ] Check Logcat for API calls

**Labels**:
- [ ] Open project labels screen
- [ ] Click create → Enter name + select color
- [ ] Verify label appears
- [ ] Open task → Click add label → Select labels
- [ ] Verify labels show on task
- [ ] Remove label from task
- [ ] Delete label → Verify removed from all tasks

---

## 📊 DELIVERABLES CHECKLIST

### **Team Members Feature**:
- [ ] 3 DTOs created
- [ ] MemberApiService interface
- [ ] 3 layouts (fragment, item, dialog)
- [ ] MemberAdapter implemented
- [ ] InviteMemberDialog implemented
- [ ] MemberRepository implemented
- [ ] MembersViewModel implemented
- [ ] MembersFragment integrated
- [ ] All flows tested

### **Labels Feature**:
- [ ] 3 DTOs created
- [ ] LabelApiService interface
- [ ] 3 layouts (fragment, item, dialog)
- [ ] LabelAdapter implemented
- [ ] ColorPaletteAdapter implemented
- [ ] LabelRepository implemented
- [ ] LabelsViewModel implemented
- [ ] LabelsFragment integrated
- [ ] Label picker dialog working
- [ ] All flows tested

---

## 🐛 COMMON ISSUES & FIXES

### **Issue 1: Network Error**
```
Error: Unable to resolve host "localhost"
```
**Fix**: Use `10.0.2.2` instead of `localhost` for Android emulator

---

### **Issue 2: 401 Unauthorized**
```
Response code: 401
```
**Fix**: Add Bearer token to Retrofit:
```java
OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(chain -> {
        Request request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer " + token)
            .build();
        return chain.proceed(request);
    })
    .build();
```

---

### **Issue 3: Colors not showing**
```
Label color is white instead of selected color
```
**Fix**: Use Color.parseColor():
```java
int color = Color.parseColor(label.getColor());
chipLabel.setChipBackgroundColor(ColorStateList.valueOf(color));
```

---

## 💡 BEST PRACTICES

1. **Logging**: Add logs everywhere
```java
android.util.Log.d("MembersVM", "Loading members for project: " + projectId);
android.util.Log.d("MembersVM", "Received " + members.size() + " members");
```

2. **Error Handling**: Always show user-friendly messages
```java
Toast.makeText(context, "Failed to invite member", Toast.LENGTH_SHORT).show();
```

3. **Loading States**: Show progress indicators
```java
progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
```

4. **Empty States**: Show helpful messages
```java
tvNoMembers.setVisibility(members.isEmpty() ? View.VISIBLE : View.GONE);
```

---

## 📞 HELP & SUPPORT

**Stuck?**
1. Check backend docs: `POSTMAN_WEEK1_TESTING_GUIDE.md`
2. Test API with Postman first
3. Add logging to trace issues
4. Ask in `#week1-frontend` Slack

**Integration Questions?**
- Backend API base URL: `http://10.0.2.2:3000/api`
- Token: Get from login response
- All endpoints require `Authorization: Bearer {token}`

---

**Good luck! You got this! 💪**

**Status**: ✅ Ready to code  
**Next**: Start at 08:00 tomorrow 🚀
