# Feature: Horizontal Task Movement Between Boards

## Tổng quan

Di chuyển task giữa các boards (TO DO ↔ IN PROGRESS ↔ DONE) bằng nút ← → thay vì drag-and-drop lên/xuống trong cùng board.

## Thay đổi Files

### 1. Layout: `board_detail_item.xml`

**Thay đổi:**

- ❌ Removed: `btnMoveUp` (arrow_up_float)
- ❌ Removed: `btnMoveDown` (arrow_down_float)
- ✅ Added: `btnMoveLeft` (ic_media_previous)
- ✅ Added: `btnMoveRight` (ic_media_next)
- Changed orientation: `vertical` → `horizontal`

```xml
<!-- Move buttons container -->
<LinearLayout
    android:orientation="horizontal">  <!-- Was: vertical -->

    <ImageButton
        android:id="@+id/btnMoveLeft"
        android:src="@android:drawable/ic_media_previous"
        android:contentDescription="Move task to previous board" />

    <ImageButton
        android:id="@+id/btnMoveRight"
        android:src="@android:drawable/ic_media_next"
        android:contentDescription="Move task to next board" />

</LinearLayout>
```

### 2. Adapter: `TaskAdapter.java`

#### Interface Update

**Before:**

```java
public interface OnTaskMoveListener {
    void onMoveUp(int position);
    void onMoveDown(int position);
}
```

**After:**

```java
public interface OnTaskMoveListener {
    void onMoveLeft(Task task, int position);
    void onMoveRight(Task task, int position);
}
```

#### ViewHolder Update

**Before:**

```java
ImageButton btnMoveUp;
ImageButton btnMoveDown;

btnMoveUp = itemView.findViewById(R.id.btnMoveUp);
btnMoveDown = itemView.findViewById(R.id.btnMoveDown);
```

**After:**

```java
ImageButton btnMoveLeft;
ImageButton btnMoveRight;

btnMoveLeft = itemView.findViewById(R.id.btnMoveLeft);
btnMoveRight = itemView.findViewById(R.id.btnMoveRight);
```

#### Click Listeners

```java
btnMoveLeft.setOnClickListener(v -> {
    if (moveListener != null) {
        int position = getAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            moveListener.onMoveLeft(task, position);
        }
    }
});

btnMoveRight.setOnClickListener(v -> {
    if (moveListener != null) {
        int position = getAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            moveListener.onMoveRight(task, position);
        }
    }
});
```

### 3. Board Adapter: `BoardAdapter.java`

#### New Interface

```java
public interface OnTaskBoardChangeListener {
    /**
     * Move task to adjacent board
     * @param task The task to move
     * @param currentBoard The current board containing the task
     * @param direction -1 for left (previous board), +1 for right (next board)
     */
    void onMoveTaskToBoard(Task task, Board currentBoard, int direction);
}
```

#### Listener Implementation

**Before:**

```java
taskAdapter.setOnTaskMoveListener(new TaskAdapter.OnTaskMoveListener() {
    @Override
    public void onMoveUp(int position) {
        // Move within same board
        taskAdapter.moveItem(position, position - 1);
        listener.onTaskPositionChanged(...);
    }

    @Override
    public void onMoveDown(int position) {
        // Move within same board
        taskAdapter.moveItem(position, position + 1);
        listener.onTaskPositionChanged(...);
    }
});
```

**After:**

```java
taskAdapter.setOnTaskMoveListener(new TaskAdapter.OnTaskMoveListener() {
    @Override
    public void onMoveLeft(Task task, int position) {
        // Move to previous board
        if (listener instanceof OnTaskBoardChangeListener) {
            ((OnTaskBoardChangeListener) listener).onMoveTaskToBoard(task, board, -1);
        }
    }

    @Override
    public void onMoveRight(Task task, int position) {
        // Move to next board
        if (listener instanceof OnTaskBoardChangeListener) {
            ((OnTaskBoardChangeListener) listener).onMoveTaskToBoard(task, board, +1);
        }
    }
});
```

### 4. Activity: `ProjectActivity.java`

#### Interface Implementation

```java
public class ProjectActivity extends AppCompatActivity
    implements BoardAdapter.OnBoardActionListener,
               BoardAdapter.OnTaskPositionChangeListener,
               BoardAdapter.OnTaskBoardChangeListener {  // NEW
```

#### Method Implementation

```java
@Override
public void onMoveTaskToBoard(Task task, Board currentBoard, int direction) {
    // 1. Find current board index
    int currentBoardIndex = -1;
    for (int i = 0; i < boards.size(); i++) {
        if (boards.get(i).getId().equals(currentBoard.getId())) {
            currentBoardIndex = i;
            break;
        }
    }

    // 2. Calculate target board index
    int targetBoardIndex = currentBoardIndex + direction;

    // 3. Check bounds
    if (targetBoardIndex < 0) {
        Toast.makeText(this, "Cannot move left - already at first board", Toast.LENGTH_SHORT).show();
        return;
    }

    if (targetBoardIndex >= boards.size()) {
        Toast.makeText(this, "Cannot move right - already at last board", Toast.LENGTH_SHORT).show();
        return;
    }

    // 4. Get target board
    Board targetBoard = boards.get(targetBoardIndex);

    // 5. Move task via ViewModel
    taskViewModel.moveTaskToBoard(task.getId(), targetBoard.getId(), 0.0);

    // 6. Reload both boards
    new Handler(Looper.getMainLooper()).postDelayed(() -> {
        loadTasksForBoard(currentBoard.getId());
        loadTasksForBoard(targetBoard.getId());
        Toast.makeText(this, "Moved to " + targetBoard.getName(), Toast.LENGTH_SHORT).show();
    }, 500);
}
```

## Flow Hoạt Động

### Sequence Diagram: Move Task Right

```
User clicks btnMoveRight (→)
    ↓
TaskAdapter.ViewHolder.btnMoveRight.onClick()
    ↓
TaskAdapter.OnTaskMoveListener.onMoveRight(task, position)
    ↓
BoardAdapter.setOnTaskMoveListener callback
    ↓
OnTaskBoardChangeListener.onMoveTaskToBoard(task, currentBoard, +1)
    ↓
ProjectActivity.onMoveTaskToBoard(task, currentBoard, +1)
    ↓
1. Find currentBoardIndex in boards list
    ↓
2. Calculate targetBoardIndex = currentBoardIndex + 1
    ↓
3. Check: targetBoardIndex < boards.size()?
    ├─ No → Show Toast "Cannot move right"
    └─ Yes → Continue
    ↓
4. Get targetBoard = boards.get(targetBoardIndex)
    ↓
5. taskViewModel.moveTaskToBoard(taskId, targetBoardId, 0.0)
    ↓
   TaskViewModel.moveTaskToBoard()
    ↓
   MoveTaskToBoardUseCase.execute()
    ↓
   Backend API: PATCH /api/tasks/:id/move-to-board
    ↓
   Database: UPDATE tasks SET board_id = targetBoardId, position = 0.0
    ↓
6. After 500ms delay:
    ├─ loadTasksForBoard(currentBoard.getId())  // Remove from source
    ├─ loadTasksForBoard(targetBoard.getId())   // Add to target
    └─ Toast "Moved to [TARGET_BOARD_NAME]"
```

### Board Navigation Map

```
Index   Board Name       Can Move Left?   Can Move Right?
─────────────────────────────────────────────────────────
  0     TO DO            ❌ No            ✅ Yes → IN PROGRESS
  1     IN PROGRESS      ✅ Yes → TO DO   ✅ Yes → DONE
  2     DONE             ✅ Yes → IN PROGRESS  ❌ No
```

### Direction Mapping

```java
direction = -1  →  Move LEFT (previous board)
direction = +1  →  Move RIGHT (next board)

targetBoardIndex = currentBoardIndex + direction
```

## API Integration

### Existing API Used

```
PATCH /api/tasks/:taskId/move-to-board

Request Body:
{
  "targetBoardId": "uuid-of-target-board",
  "position": 0.0
}

Response:
{
  "id": "task-uuid",
  "boardId": "target-board-uuid",
  "position": 0.0,
  ...
}
```

### ViewModel Method

```java
taskViewModel.moveTaskToBoard(
    taskId,          // UUID của task cần move
    targetBoardId,   // UUID của board đích
    0.0              // Position (0.0 = top of target board)
);
```

## UX Improvements

### Visual Feedback

1. **Progress Indicator:** ProgressBar shows during API call
2. **Toast Messages:**
   - Success: "Moved to [BOARD_NAME]"
   - Boundary: "Cannot move left/right - already at first/last board"
3. **Instant UI Update:** Both source and target boards refresh after 500ms

### Boundary Handling

- **Leftmost Board (TO DO):**
  - btnMoveLeft click → Toast warning
  - No API call made
- **Rightmost Board (DONE):**
  - btnMoveRight click → Toast warning
  - No API call made

### Task Position

- Always moves to **top of target board** (position = 0.0)
- Consistent and predictable behavior
- Users can manually reorder within board if needed

## Testing Checklist

### Basic Movement

- [ ] Move task from TO DO → IN PROGRESS (click →)
- [ ] Move task from IN PROGRESS → DONE (click →)
- [ ] Move task from IN PROGRESS → TO DO (click ←)
- [ ] Move task from DONE → IN PROGRESS (click ←)

### Boundary Cases

- [ ] Click ← on task in TO DO → Shows "Cannot move left"
- [ ] Click → on task in DONE → Shows "Cannot move right"
- [ ] Verify no API call made for boundary violations

### UI Updates

- [ ] Task disappears from source board immediately
- [ ] Task appears at top of target board
- [ ] Both boards refresh correctly
- [ ] Progress spinner shows briefly
- [ ] Toast shows correct target board name

### Edge Cases

- [ ] Move task when target board is empty
- [ ] Move task when source board has only 1 task
- [ ] Rapid clicking (should be handled by API)
- [ ] Network error handling (should show error toast)

## Comparison: Before vs After

### Before (Vertical Movement)

- **Action:** Drag task up/down
- **Scope:** Within same board
- **Purpose:** Reorder priority
- **API:** Update task position within board
- **UX:** Long-press and drag gesture

### After (Horizontal Movement)

- **Action:** Click ← or → button
- **Scope:** Between different boards
- **Purpose:** Change workflow stage
- **API:** Move task to different board
- **UX:** Single tap gesture

## Code Metrics

### Files Modified: 4

1. `board_detail_item.xml` - Layout update
2. `TaskAdapter.java` - Interface & button handlers
3. `BoardAdapter.java` - New interface & listener
4. `ProjectActivity.java` - Implementation logic

### Lines Added: ~120

- Layout: ~10 lines (button definitions)
- TaskAdapter: ~40 lines (new listeners)
- BoardAdapter: ~30 lines (interface + listener)
- ProjectActivity: ~40 lines (onMoveTaskToBoard method)

### Lines Removed: ~90

- Old move up/down logic
- Position calculation methods
- Within-board swap logic

### Net Change: +30 lines

- Simpler logic (no position calculation)
- Uses existing API (moveTaskToBoard)
- Cleaner separation of concerns

## Future Enhancements

### 1. Visual Improvements

- Disable/hide ← button in first board
- Disable/hide → button in last board
- Add animation when task moves
- Show preview of target board

### 2. UX Enhancements

- Undo action (Snackbar with "Undo")
- Batch move (select multiple tasks)
- Keyboard shortcuts (Alt+← / Alt+→)
- Swipe gesture support

### 3. Performance

- Optimistic UI update (remove from source immediately)
- Lazy load target board if not visible
- Cache board list to avoid repeated lookups

### 4. Accessibility

- Announce board change to screen readers
- Keyboard navigation support
- High contrast button colors
- Larger touch targets for accessibility mode
