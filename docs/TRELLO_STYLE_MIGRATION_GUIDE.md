# Hướng dẫn chuyển đổi giao diện Project sang Trello-style

## 1. Cấu trúc hiện tại vs Trello-style

### Hiện tại:
- Vertical list of boards
- Simple task cards

### Trello-style (ảnh 2):
- Horizontal scrolling boards
- Rich task cards với labels, dates, checklists, avatars

## 2. Các thay đổi cần thiết

### A. Layout thay đổi - activity_project.xml

```xml
<!-- Thay RecyclerView vertical bằng HorizontalScrollView -->
<HorizontalScrollView
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:scrollbars="none">
    
    <LinearLayout
        android:id="@+id/llBoardsContainer"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:orientation="horizontal"
        android:padding="8dp"/>
</HorizontalScrollView>
```

### B. Board Item Layout - item_board_column.xml (MỚI)

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="280dp"
    android:layout_height="match_parent"
    android:layout_marginEnd="12dp"
    android:orientation="vertical"
    android:background="#EBECF0"
    android:padding="8dp">

    <!-- Board Header -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:paddingBottom="8dp">
        
        <TextView
            android:id="@+id/tvBoardName"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="To Do"
            android:textSize="14sp"
            android:textStyle="bold"
            android:textColor="#172B4D"/>
        
        <ImageButton
            android:id="@+id/btnBoardMenu"
            android:layout_width="32dp"
            android:layout_height="32dp"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:src="@drawable/ic_more_vert"/>
    </LinearLayout>

    <!-- Tasks RecyclerView -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvTasks"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"/>

    <!-- Add Card Button -->
    <LinearLayout
        android:id="@+id/btnAddCard"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="8dp"
        android:gravity="center_vertical"
        android:background="?attr/selectableItemBackground">
        
        <ImageView
            android:layout_width="16dp"
            android:layout_height="16dp"
            android:src="@drawable/ic_add"
            android:tint="#5E6C84"/>
        
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="8dp"
            android:text="Add a card"
            android:textSize="14sp"
            android:textColor="#5E6C84"/>
    </LinearLayout>
</LinearLayout>
```

### C. Task Card Layout - item_task_card.xml (MỚI)

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="8dp"
    app:cardCornerRadius="4dp"
    app:cardElevation="1dp"
    app:cardBackgroundColor="#FFFFFF">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="12dp">

        <!-- Labels (colored bars) -->
        <LinearLayout
            android:id="@+id/llLabels"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginBottom="8dp"/>

        <!-- Task Title -->
        <TextView
            android:id="@+id/tvTaskTitle"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textSize="14sp"
            android:textColor="#172B4D"
            android:lineSpacingExtra="2dp"
            android:layout_marginBottom="8dp"/>

        <!-- Metadata Row -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical">

            <!-- Due Date -->
            <LinearLayout
                android:id="@+id/llDueDate"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:background="@drawable/bg_chip_green"
                android:paddingStart="6dp"
                android:paddingEnd="6dp"
                android:paddingTop="2dp"
                android:paddingBottom="2dp"
                android:layout_marginEnd="8dp"
                android:visibility="gone">

                <ImageView
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:src="@drawable/ic_calendar"
                    android:tint="#FFFFFF"/>

                <TextView
                    android:id="@+id/tvDueDate"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginStart="4dp"
                    android:text="Nov 6"
                    android:textSize="11sp"
                    android:textColor="#FFFFFF"/>
            </LinearLayout>

            <!-- Checklist Progress -->
            <LinearLayout
                android:id="@+id/llChecklist"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:background="@drawable/bg_chip_green"
                android:paddingStart="6dp"
                android:paddingEnd="6dp"
                android:paddingTop="2dp"
                android:paddingBottom="2dp"
                android:layout_marginEnd="8dp"
                android:visibility="gone">

                <ImageView
                    android:layout_width="12dp"
                    android:layout_height="12dp"
                    android:src="@drawable/ic_checklist"
                    android:tint="#FFFFFF"/>

                <TextView
                    android:id="@+id/tvChecklistProgress"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginStart="4dp"
                    android:text="3/3"
                    android:textSize="11sp"
                    android:textColor="#FFFFFF"/>
            </LinearLayout>

            <View
                android:layout_width="0dp"
                android:layout_height="1dp"
                android:layout_weight="1"/>

            <!-- Comment Count -->
            <LinearLayout
                android:id="@+id/llComments"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:layout_marginEnd="8dp"
                android:visibility="gone">

                <ImageView
                    android:layout_width="16dp"
                    android:layout_height="16dp"
                    android:src="@drawable/ic_comment"
                    android:tint="#5E6C84"/>

                <TextView
                    android:id="@+id/tvCommentCount"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginStart="4dp"
                    android:text="4"
                    android:textSize="12sp"
                    android:textColor="#5E6C84"/>
            </LinearLayout>

            <!-- Assignee Avatars -->
            <LinearLayout
                android:id="@+id/llAssignees"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"/>
        </LinearLayout>
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```

### D. Drawables cần thiết

#### bg_chip_green.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="#61BD4F"/>
    <corners android:radius="3dp"/>
</shape>
```

#### bg_chip_yellow.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="#F2D600"/>
    <corners android:radius="3dp"/>
</shape>
```

#### bg_label_bar.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <corners android:radius="4dp"/>
</shape>
```

### E. Java Code Changes

#### BoardColumnAdapter.java (MỚI)
```java
public class BoardColumnAdapter extends RecyclerView.Adapter<BoardColumnAdapter.ViewHolder> {
    private List<Board> boards;
    private Context context;
    private OnBoardActionListener listener;

    public interface OnBoardActionListener {
        void onAddCardClicked(Board board);
        void onBoardMenuClicked(Board board);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_board_column, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Board board = boards.get(position);
        holder.tvBoardName.setText(board.getName());
        
        // Setup tasks RecyclerView inside
        TaskCardAdapter taskAdapter = new TaskCardAdapter(context, board.getTasks());
        holder.rvTasks.setLayoutManager(new LinearLayoutManager(context));
        holder.rvTasks.setAdapter(taskAdapter);
        
        holder.btnAddCard.setOnClickListener(v -> 
            listener.onAddCardClicked(board));
        
        holder.btnBoardMenu.setOnClickListener(v -> 
            listener.onBoardMenuClicked(board));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBoardName;
        RecyclerView rvTasks;
        LinearLayout btnAddCard;
        ImageButton btnBoardMenu;

        ViewHolder(View itemView) {
            super(itemView);
            tvBoardName = itemView.findViewById(R.id.tvBoardName);
            rvTasks = itemView.findViewById(R.id.rvTasks);
            btnAddCard = itemView.findViewById(R.id.btnAddCard);
            btnBoardMenu = itemView.findViewById(R.id.btnBoardMenu);
        }
    }
}
```

#### TaskCardAdapter.java (MỚI)
```java
public class TaskCardAdapter extends RecyclerView.Adapter<TaskCardAdapter.ViewHolder> {
    private List<Task> tasks;
    private Context context;

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Task task = tasks.get(position);
        
        holder.tvTaskTitle.setText(task.getTitle());
        
        // Show labels
        holder.llLabels.removeAllViews();
        if (task.getLabels() != null && !task.getLabels().isEmpty()) {
            for (Label label : task.getLabels()) {
                View labelBar = createLabelBar(label);
                holder.llLabels.addView(labelBar);
            }
        }
        
        // Show due date
        if (task.getDueAt() != null) {
            holder.llDueDate.setVisibility(View.VISIBLE);
            holder.tvDueDate.setText(formatDate(task.getDueAt()));
            
            // Green if completed, red if overdue
            if (task.isCompleted()) {
                holder.llDueDate.setBackgroundResource(R.drawable.bg_chip_green);
            } else if (isOverdue(task.getDueAt())) {
                holder.llDueDate.setBackgroundResource(R.drawable.bg_chip_red);
            }
        }
        
        // Show checklist progress
        if (task.getChecklists() != null && !task.getChecklists().isEmpty()) {
            holder.llChecklist.setVisibility(View.VISIBLE);
            int total = 0, completed = 0;
            for (Checklist checklist : task.getChecklists()) {
                total += checklist.getItems().size();
                completed += checklist.getCompletedCount();
            }
            holder.tvChecklistProgress.setText(completed + "/" + total);
            
            if (completed == total) {
                holder.llChecklist.setBackgroundResource(R.drawable.bg_chip_green);
            }
        }
        
        // Show comments count
        if (task.getComments() != null && task.getComments().size() > 0) {
            holder.llComments.setVisibility(View.VISIBLE);
            holder.tvCommentCount.setText(String.valueOf(task.getComments().size()));
        }
        
        // Show assignee avatars
        holder.llAssignees.removeAllViews();
        if (task.getAssigneeId() != null) {
            ImageView avatar = createAvatarView(task.getAssigneeId());
            holder.llAssignees.addView(avatar);
        }
    }
    
    private View createLabelBar(Label label) {
        View bar = new View(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            40, // width in dp
            8   // height in dp
        );
        params.setMargins(0, 0, 4, 0);
        bar.setLayoutParams(params);
        bar.setBackgroundResource(R.drawable.bg_label_bar);
        bar.getBackground().setTint(Color.parseColor(label.getColor()));
        return bar;
    }
    
    private ImageView createAvatarView(String userId) {
        ImageView avatar = new ImageView(context);
        int size = (int) (28 * context.getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(0, 0, -8, 0); // Overlap avatars
        avatar.setLayoutParams(params);
        
        // Load avatar with Glide
        Glide.with(context)
            .load(getAvatarUrl(userId))
            .circleCrop()
            .into(avatar);
        
        return avatar;
    }
}
```

## 3. Migration Steps (Từng bước)

### Bước 1: Backup code hiện tại
### Bước 2: Tạo layouts mới (item_board_column, item_task_card)
### Bước 3: Tạo adapters mới (BoardColumnAdapter, TaskCardAdapter)
### Bước 4: Thay đổi activity_project.xml sang HorizontalScrollView
### Bước 5: Cập nhật ProjectActivity.java để sử dụng adapters mới
### Bước 6: Test từng phần (boards, tasks, metadata)
### Bước 7: Polish UI (colors, spacing, animations)

## 4. Tính năng bổ sung cần implement

1. **Drag & Drop** - Di chuyển cards giữa các boards
2. **Labels** - Quản lý và hiển thị labels
3. **Checklists** - Tích hợp checklist vào tasks
4. **Comments** - Đếm và hiển thị comments
5. **Assignees** - Hiển thị avatars members được assign
6. **Due dates** - Hiển thị và highlight due dates

Bạn có muốn tôi implement từng bước không?
