# 🎯 ACTION PLAN: HOÀN THIỆN ROOM DATABASE CACHING
## Báo cáo cho Team FE (Person 1 & Person 2)

**Thời gian:** 2-3 ngày làm việc  
**Mục tiêu:** Hoàn thiện cache implementation và integrate vào UI  
**Priority:** 🔴 CRITICAL

---

## 📊 HIỆN TRẠNG (WHAT'S DONE)

### ✅ ĐÃ HOÀN THÀNH TỐT:
- ✅ AppDatabase, TaskEntity, ProjectEntity, WorkspaceEntity
- ✅ TaskDao, ProjectDao, WorkspaceDao với đầy đủ queries
- ✅ 14 Mappers (TaskEntityMapper, ProjectEntityMapper, etc.)
- ✅ TaskRepositoryImplWithCache - cache pattern mẫu
- ✅ ProjectRepositoryImplWithCache
- ✅ DependencyProvider đã integrate vào App.java
- ✅ Logout đã clear cache properly

### ⚠️ CẦN HOÀN THIỆN:
- ❌ **CRITICAL:** BoardEntity & BoardDao chưa có
- ❌ **CRITICAL:** WorkspaceEntity dùng `int id` (phải là `String id`)
- ❌ **CRITICAL:** Chưa có Activity/Fragment nào dùng cache
- ⚠️ Chỉ 2/5 repository có cache (Task, Project done; Workspace, Board, Sprint chưa)
- ⚠️ Chưa test thực tế trên device

---

## 🚨 CRITICAL ISSUES CẦN FIX NGAY

### ❌ ISSUE #1: WorkspaceEntity ID Type Bug (Person 1)

**Vấn đề:**
```java
// FILE: WorkspaceEntity.java
@PrimaryKey
private int id;  // ❌ WRONG! Backend trả về Stringvvv
```

**Backend response thực tế:**
```json
{
  "id": "cm3m0qyo40000jxqp85rqbg5f"  // String CUID, không phải int
}
```

**Tác động:**
- `NumberFormatException` khi parse
- WorkspaceEntityMapper return id = 0 → tất cả workspace có id = 0
- Duplicate key error trong database

**FIX:**
```java
@PrimaryKey
@NonNull
private String id;  // ✅ CORRECT
```

**Files cần sửa:**
1. `WorkspaceEntity.java` - đổi int → String
2. `WorkspaceDao.java` - đổi method signatures
3. `WorkspaceEntityMapper.java` - remove parseId(), dùng trực tiếp String

---

### ❌ ISSUE #2: Thiếu BoardEntity & BoardDao (Person 1)

**Tại sao quan trọng:**
- Board chứa các cột status của Kanban (TODO, IN_PROGRESS, DONE)
- InboxActivity cần load boards để hiển thị tasks theo columns
- ProjectActivity cần boards để render Kanban board
- Không có Board cache → InboxActivity vẫn phải chờ API

**Schema backend (từ schema.prisma):**
```prisma
model Board {
  id        String   @id @default(cuid())
  projectId String
  name      String
  order     Int
  createdAt DateTime @default(now())
  updatedAt DateTime @updatedAt
}
```

**Cần tạo:**
1. **BoardEntity.java**
2. **BoardDao.java**
3. **BoardEntityMapper.java**
4. Update **AppDatabase.java** version 2 → 3

---

### ❌ ISSUE #3: UI không dùng cache (Person 2)

**Hiện tại:**
```java
// InboxActivity.java - VẪN GỌI API TRỰC TIẾP
taskRepository.getAllTasks(callback);  // ❌ No cache
```

**Cần đổi thành:**
```java
// Use cached repository
App.dependencyProvider.getTaskRepositoryWithCache()
    .getAllTasks(new TaskCallback() {
        @Override
        public void onSuccess(List<Task> tasks) {
            showTasks(tasks);  // ⚡ 30ms from cache
        }
        
        @Override
        public void onCacheEmpty() {
            showLoading();  // First time, fetch from API
        }
    });
```

**Activities cần integrate:**
1. InboxActivity - cache tasks & boards
2. ProjectActivity - cache project & boards
3. HomeActivity - cache workspaces & projects

---

## 📅 PHÂN CÔNG CHI TIẾT 2-3 NGÀY

### 👤 PERSON 1: Database Layer Fixes

---

#### 📅 DAY 1 MORNING (3-4h): Fix Critical Bugs

**Task 1.1: Fix WorkspaceEntity (1h)**

**File:** `data/local/database/entity/WorkspaceEntity.java`

```java
@Entity(tableName = "workspaces")
public class WorkspaceEntity {
    
    @PrimaryKey
    @NonNull
    private String id;  // ✅ Changed from int to String
    
    private String name;
    private String description;
    private String userId;
    private Date createdAt;
    private Date updatedAt;
    
    // Update constructor & getters/setters accordingly
    public WorkspaceEntity(@NonNull String id, String name, ...) {
        this.id = id;
        // ...
    }
}
```

**File:** `data/local/database/dao/WorkspaceDao.java`

```java
@Dao
public interface WorkspaceDao {
    @Query("SELECT * FROM workspaces WHERE id = :workspaceId LIMIT 1")
    WorkspaceEntity getById(String workspaceId);  // ✅ Changed from int
    
    @Query("SELECT * FROM workspaces WHERE userId = :userId")
    List<WorkspaceEntity> getAllByUserId(String userId);
    
    // Update all other methods
}
```

**File:** `data/mapper/WorkspaceEntityMapper.java`

```java
public static WorkspaceEntity toEntity(Workspace workspace) {
    return new WorkspaceEntity(
        workspace.getId(),  // ✅ No longer need parseId()
        workspace.getName(),
        null,
        workspace.getOwnerId(),
        null,
        null
    );
}

public static Workspace toDomain(WorkspaceEntity entity) {
    return new Workspace(
        entity.getId(),  // ✅ Direct String
        entity.getName(),
        "TEAM",
        entity.getUserId()
    );
}

// ❌ REMOVE parseId() method completely
```

**Verification:**
```bash
# Build project
gradlew assembleDebug

# Check no errors
# Check Room generated WorkspaceDao_Impl.java uses String
```

---

**Task 1.2: Create BoardEntity (2h)**

**File:** `data/local/database/entity/BoardEntity.java`

```java
package com.example.tralalero.data.local.database.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import java.util.Date;

@Entity(
    tableName = "boards",
    indices = {
        @Index(value = "projectId"),
        @Index(value = "order")
    }
)
public class BoardEntity {
    
    @PrimaryKey
    @NonNull
    private String id;
    
    @NonNull
    private String projectId;
    
    @NonNull
    private String name;
    
    private int order;
    
    private Date createdAt;
    private Date updatedAt;
    
    // Constructor with required fields
    public BoardEntity(@NonNull String id, @NonNull String projectId, 
                       @NonNull String name, int order) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.order = order;
    }
    
    // Getters & Setters
    @NonNull public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    
    @NonNull public String getProjectId() { return projectId; }
    public void setProjectId(@NonNull String projectId) { this.projectId = projectId; }
    
    @NonNull public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }
    
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
```

**File:** `data/local/database/dao/BoardDao.java`

```java
package com.example.tralalero.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.tralalero.data.local.database.entity.BoardEntity;
import java.util.List;

@Dao
public interface BoardDao {
    
    // ==================== CRUD OPERATIONS ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BoardEntity board);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BoardEntity> boards);
    
    @Update
    void update(BoardEntity board);
    
    @Delete
    void delete(BoardEntity board);
    
    @Query("DELETE FROM boards")
    void deleteAll();
    
    // ==================== QUERY METHODS ====================
    
    @Query("SELECT * FROM boards WHERE id = :boardId LIMIT 1")
    BoardEntity getById(String boardId);
    
    @Query("SELECT * FROM boards WHERE projectId = :projectId ORDER BY `order` ASC")
    List<BoardEntity> getBoardsByProjectSync(String projectId);
    
    @Query("SELECT * FROM boards WHERE projectId = :projectId AND name = :name LIMIT 1")
    BoardEntity getBoardByName(String projectId, String name);
    
    @Query("DELETE FROM boards WHERE projectId = :projectId")
    void deleteByProjectId(String projectId);
    
    @Query("SELECT COUNT(*) FROM boards WHERE projectId = :projectId")
    int countByProjectId(String projectId);
    
    @Query("UPDATE boards SET `order` = :newOrder WHERE id = :boardId")
    void updateBoardOrder(String boardId, int newOrder);
}
```

**File:** `data/mapper/BoardEntityMapper.java`

```java
package com.example.tralalero.data.mapper;

import com.example.tralalero.data.local.database.entity.BoardEntity;
import com.example.tralalero.domain.model.Board;
import java.util.ArrayList;
import java.util.List;

public class BoardEntityMapper {
    
    public static BoardEntity toEntity(Board board) {
        if (board == null) {
            return null;
        }
        
        BoardEntity entity = new BoardEntity(
            board.getId(),
            board.getProjectId(),
            board.getName(),
            board.getOrder()
        );
        
        // Note: Board domain model doesn't have createdAt/updatedAt
        // So we leave them null in entity
        
        return entity;
    }
    
    public static Board toDomain(BoardEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return new Board(
            entity.getId(),
            entity.getProjectId(),
            entity.getName(),
            entity.getOrder()
        );
    }
    
    public static List<Board> toDomainList(List<BoardEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        
        List<Board> boards = new ArrayList<>();
        for (BoardEntity entity : entities) {
            Board board = toDomain(entity);
            if (board != null) {
                boards.add(board);
            }
        }
        return boards;
    }
    
    public static List<BoardEntity> toEntityList(List<Board> boards) {
        if (boards == null) {
            return new ArrayList<>();
        }
        
        List<BoardEntity> entities = new ArrayList<>();
        for (Board board : boards) {
            BoardEntity entity = toEntity(board);
            if (entity != null) {
                entities.add(entity);
            }
        }
        return entities;
    }
}
```

**File:** `data/local/database/AppDatabase.java`

```java
@Database(
    entities = {
        TaskEntity.class,
        ProjectEntity.class,
        WorkspaceEntity.class,
        BoardEntity.class  // ✅ ADD THIS
    },
    version = 3,  // ✅ INCREMENT VERSION
    exportSchema = false
)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    
    // ...existing code...
    
    public abstract BoardDao boardDao();  // ✅ ADD THIS
    
    // ...existing code...
}
```

**Verification:**
```bash
# Rebuild project
gradlew clean assembleDebug

# Check logs for Room generation
# Look for: BoardDao_Impl.java in build/generated/
```

---

#### 📅 DAY 1 AFTERNOON (3h): Update DependencyProvider

**Task 1.3: Add BoardDao to DependencyProvider**

**File:** `core/DependencyProvider.java`

```java
public class DependencyProvider {
    // ...existing code...
    
    private final BoardDao boardDao;  // ✅ ADD
    
    private DependencyProvider(Context context, TokenManager tokenManager) {
        // ...existing code...
        
        this.boardDao = database.boardDao();  // ✅ ADD
        Log.d(TAG, "✓ BoardDao initialized");
    }
    
    // ✅ ADD GETTER
    public BoardDao getBoardDao() {
        return boardDao;
    }
    
    // ...existing code...
}
```

**Task 1.4: Write Unit Tests**

**File:** `test/java/.../dao/BoardDaoTest.java`

```java
@RunWith(RobolectricTestRunner.class)
public class BoardDaoTest {
    private AppDatabase database;
    private BoardDao boardDao;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        boardDao = database.boardDao();
    }

    @After
    public void cleanup() {
        database.close();
    }

    @Test
    public void testInsertAndRetrieveBoard() {
        BoardEntity board = new BoardEntity("board-1", "proj-1", "TODO", 0);
        boardDao.insert(board);
        
        BoardEntity retrieved = boardDao.getById("board-1");
        
        assertNotNull(retrieved);
        assertEquals("TODO", retrieved.getName());
        assertEquals("proj-1", retrieved.getProjectId());
    }

    @Test
    public void testGetBoardsByProject() {
        boardDao.insertAll(Arrays.asList(
            new BoardEntity("b1", "proj-1", "TODO", 0),
            new BoardEntity("b2", "proj-1", "IN_PROGRESS", 1),
            new BoardEntity("b3", "proj-1", "DONE", 2)
        ));
        
        List<BoardEntity> boards = boardDao.getBoardsByProjectSync("proj-1");
        
        assertEquals(3, boards.size());
        assertEquals("TODO", boards.get(0).getName());
        assertEquals(0, boards.get(0).getOrder());
    }
}
```

**Run tests:**
```bash
gradlew test --tests BoardDaoTest
```

---

### 👤 PERSON 2: Repository & UI Integration

---

#### 📅 DAY 1 (6h): Create Cached Repositories

**Task 2.1: BoardRepositoryImplWithCache (3h)**

**File:** `data/repository/BoardRepositoryImplWithCache.java`

```java
package com.example.tralalero.data.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.tralalero.data.local.database.dao.BoardDao;
import com.example.tralalero.data.local.database.entity.BoardEntity;
import com.example.tralalero.data.mapper.BoardEntityMapper;
import com.example.tralalero.data.mapper.BoardMapper;
import com.example.tralalero.data.remote.api.BoardApiService;
import com.example.tralalero.data.remote.dto.board.BoardDTO;
import com.example.tralalero.domain.model.Board;
import com.example.tralalero.domain.repository.IBoardRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BoardRepositoryImplWithCache implements IBoardRepository {
    private static final String TAG = "BoardRepositoryCache";
    
    private final BoardApiService apiService;
    private final BoardDao boardDao;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    
    public BoardRepositoryImplWithCache(BoardApiService apiService, 
                                        BoardDao boardDao,
                                        ExecutorService executorService) {
        this.apiService = apiService;
        this.boardDao = boardDao;
        this.executorService = executorService;
        this.mainHandler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "BoardRepositoryImplWithCache initialized");
    }
    
    @Override
    public void getBoardsByProjectId(String projectId, RepositoryCallback<List<Board>> callback) {
        if (projectId == null || callback == null) {
            if (callback != null) {
                mainHandler.post(() -> callback.onError("Invalid projectId"));
            }
            return;
        }
        
        executorService.execute(() -> {
            try {
                // 1. Return from cache immediately (30ms)
                List<BoardEntity> cached = boardDao.getBoardsByProjectSync(projectId);
                if (cached != null && !cached.isEmpty()) {
                    List<Board> cachedBoards = BoardEntityMapper.toDomainList(cached);
                    mainHandler.post(() -> callback.onSuccess(cachedBoards));
                    Log.d(TAG, "✓ Returned " + cached.size() + " boards from cache");
                }
                
                // 2. Fetch from network in background (500-1500ms)
                fetchBoardsFromNetwork(projectId, callback, cached == null || cached.isEmpty());
            } catch (Exception e) {
                Log.e(TAG, "Error in getBoardsByProjectId", e);
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            }
        });
    }
    
    private void fetchBoardsFromNetwork(String projectId, RepositoryCallback<List<Board>> callback, boolean isFirstLoad) {
        apiService.getBoardsByProjectId(projectId).enqueue(new Callback<List<BoardDTO>>() {
            @Override
            public void onResponse(Call<List<BoardDTO>> call, Response<List<BoardDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        List<Board> boards = BoardMapper.toDomainList(response.body());
                        
                        // Cache in background
                        executorService.execute(() -> {
                            try {
                                List<BoardEntity> entities = BoardEntityMapper.toEntityList(boards);
                                boardDao.insertAll(entities);
                                Log.d(TAG, "✓ Cached " + boards.size() + " boards from network");
                            } catch (Exception e) {
                                Log.e(TAG, "Error caching boards", e);
                            }
                        });
                        
                        // Only callback if first load (no cache)
                        if (isFirstLoad && callback != null) {
                            mainHandler.post(() -> callback.onSuccess(boards));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing boards", e);
                    }
                } else if (isFirstLoad && callback != null) {
                    mainHandler.post(() -> callback.onError("Boards not found: " + response.code()));
                }
            }
            
            @Override
            public void onFailure(Call<List<BoardDTO>> call, Throwable t) {
                Log.e(TAG, "Network error fetching boards", t);
                if (isFirstLoad && callback != null) {
                    mainHandler.post(() -> callback.onError("Network error: " + t.getMessage()));
                }
            }
        });
    }
    
    @Override
    public void getBoardById(String boardId, RepositoryCallback<Board> callback) {
        // Similar pattern: cache-first + background refresh
        executorService.execute(() -> {
            try {
                BoardEntity cached = boardDao.getById(boardId);
                if (cached != null) {
                    Board board = BoardEntityMapper.toDomain(cached);
                    mainHandler.post(() -> callback.onSuccess(board));
                    Log.d(TAG, "✓ Returned board from cache: " + boardId);
                } else {
                    // Fetch from API if not in cache
                    fetchBoardFromNetwork(boardId, callback);
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            }
        });
    }
    
    private void fetchBoardFromNetwork(String boardId, RepositoryCallback<Board> callback) {
        // Implement network fetch for single board
        // Similar to fetchBoardsFromNetwork
    }
    
    // Implement other IBoardRepository methods...
    // createBoard(), updateBoard(), deleteBoard()
}
```

---

**Task 2.2: WorkspaceRepositoryImplWithCache (3h)**

**File:** `data/repository/WorkspaceRepositoryImplWithCache.java`

Follow same pattern as BoardRepositoryImplWithCache:
- Constructor: WorkspaceApiService, WorkspaceDao, ExecutorService
- Cache-first pattern
- Background network refresh
- Implement all IWorkspaceRepository methods

---

#### 📅 DAY 2 MORNING (4h): Integrate into DependencyProvider

**Task 2.3: Update DependencyProvider**

**File:** `core/DependencyProvider.java`

```java
public class DependencyProvider {
    // ...existing code...
    
    // Cached Repositories
    private TaskRepositoryImplWithCache taskRepositoryWithCache;
    private ProjectRepositoryImplWithCache projectRepositoryWithCache;
    private BoardRepositoryImplWithCache boardRepositoryWithCache;  // ✅ ADD
    private WorkspaceRepositoryImplWithCache workspaceRepositoryWithCache;  // ✅ ADD
    
    // ...existing code...
    
    // ✅ ADD GETTERS
    public BoardRepositoryImplWithCache getBoardRepositoryWithCache() {
        if (boardRepositoryWithCache == null) {
            BoardApiService apiService = /* get from retrofit */;
            boardRepositoryWithCache = new BoardRepositoryImplWithCache(
                apiService, boardDao, executorService
            );
        }
        return boardRepositoryWithCache;
    }
    
    public WorkspaceRepositoryImplWithCache getWorkspaceRepositoryWithCache() {
        if (workspaceRepositoryWithCache == null) {
            WorkspaceApiService apiService = /* get from retrofit */;
            workspaceRepositoryWithCache = new WorkspaceRepositoryImplWithCache(
                apiService, workspaceDao, executorService
            );
        }
        return workspaceRepositoryWithCache;
    }
    
    // Update clearAllCaches()
    @Override
    public void clearAllCaches() {
        executorService.execute(() -> {
            taskDao.deleteAll();
            projectDao.deleteAll();
            workspaceDao.deleteAll();
            boardDao.deleteAll();  // ✅ ADD
            Log.d(TAG, "✓ All caches cleared");
        });
    }
}
```

---

#### 📅 DAY 2 AFTERNOON (4h): UI Integration

**Task 2.4: Integrate InboxActivity (2h)**

**File:** `feature/inbox/InboxActivity.java`

```java
public class InboxActivity extends AppCompatActivity {
    // ...existing code...
    
    private void loadTasks() {
        // ✅ NEW: Use cached repository
        App.dependencyProvider.getTaskRepositoryWithCache()
            .getAllTasks(new TaskRepositoryImplWithCache.TaskCallback() {
                @Override
                public void onSuccess(List<Task> tasks) {
                    runOnUiThread(() -> {
                        hideLoading();
                        displayTasks(tasks);
                        Log.d(TAG, "✓ Loaded " + tasks.size() + " tasks from cache");
                    });
                }
                
                @Override
                public void onCacheEmpty() {
                    runOnUiThread(() -> {
                        showLoading();
                        Log.d(TAG, "Cache empty, fetching from API...");
                    });
                    // TaskRepositoryImplWithCache will automatically fetch from API
                }
                
                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        hideLoading();
                        showError("Error loading tasks: " + e.getMessage());
                    });
                }
            });
    }
    
    private void loadBoards(String projectId) {
        // ✅ NEW: Use cached board repository
        App.dependencyProvider.getBoardRepositoryWithCache()
            .getBoardsByProjectId(projectId, new IBoardRepository.RepositoryCallback<List<Board>>() {
                @Override
                public void onSuccess(List<Board> boards) {
                    displayBoardColumns(boards);
                    Log.d(TAG, "✓ Loaded " + boards.size() + " boards from cache");
                }
                
                @Override
                public void onError(String error) {
                    showError("Error loading boards: " + error);
                }
            });
    }
}
```

---

**Task 2.5: Integrate ProjectActivity (2h)**

**File:** `feature/project/ProjectActivity.java`

```java
public class ProjectActivity extends AppCompatActivity {
    // ...existing code...
    
    private void loadProject(String projectId) {
        // ✅ Use cached project repository
        App.dependencyProvider.getProjectRepositoryWithCache()
            .getProjectById(projectId, new IProjectRepository.RepositoryCallback<Project>() {
                @Override
                public void onSuccess(Project project) {
                    displayProject(project);
                    loadBoards(projectId);  // Then load boards
                }
                
                @Override
                public void onError(String error) {
                    showError(error);
                }
            });
    }
    
    private void loadBoards(String projectId) {
        // ✅ Use cached board repository
        App.dependencyProvider.getBoardRepositoryWithCache()
            .getBoardsByProjectId(projectId, new IBoardRepository.RepositoryCallback<List<Board>>() {
                @Override
                public void onSuccess(List<Board> boards) {
                    renderKanbanBoard(boards);
                    Log.d(TAG, "⚡ Kanban loaded instantly from cache");
                }
                
                @Override
                public void onError(String error) {
                    showError(error);
                }
            });
    }
}
```

---

#### 📅 DAY 3 (4h): Testing & Polish

**Task 2.6: Performance Testing (2h)**

Create test scenario:
```
1. Clean cache: App.dependencyProvider.clearAllCaches()
2. Measure API load time: Start timer → loadTasks() → Stop timer
3. Expected: ~1200ms (API call)

4. Close & reopen activity
5. Measure cache load time: Start timer → loadTasks() → Stop timer
6. Expected: ~30ms (cache load)

7. Calculate improvement: (1200 - 30) / 1200 = 97.5% faster
```

**Implementation:**
```java
private void measureLoadTime() {
    long startTime = System.currentTimeMillis();
    
    App.dependencyProvider.getTaskRepositoryWithCache()
        .getAllTasks(new TaskCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                
                Log.i(TAG, "⚡ PERFORMANCE: Load time = " + duration + "ms");
                
                if (duration < 100) {
                    Log.i(TAG, "✓ Loaded from CACHE (fast)");
                } else {
                    Log.i(TAG, "✓ Loaded from API (slow)");
                }
            }
        });
}
```

---

**Task 2.7: Offline Mode Testing (1h)**

Test scenario:
```
1. Open app with internet ON
2. Navigate: HomeActivity → ProjectActivity → InboxActivity
3. All data cached ✓

4. Disable internet (Airplane mode)
5. Close app completely
6. Reopen app
7. Navigate again: HomeActivity → ProjectActivity → InboxActivity
8. Expected: All screens load instantly from cache

9. Try to create new task → Should show error (no network)
10. Enable internet → Sync should work
```

---

**Task 2.8: Bug Fixes & Polish (1h)**

Review checklist:
- [ ] All database operations run on background thread (not main thread)
- [ ] All UI updates use mainHandler.post() or runOnUiThread()
- [ ] No memory leaks (close database properly)
- [ ] Error handling for all edge cases
- [ ] Proper logging for debugging
- [ ] UI shows loading states properly

---

## ✅ DEFINITION OF DONE (DoD)

### Criteria để coi như hoàn thành:

#### 🔴 MUST HAVE (Bắt buộc):
- [x] WorkspaceEntity uses String id (not int)
- [x] BoardEntity & BoardDao created & tested
- [x] AppDatabase version incremented to 3
- [x] BoardRepositoryImplWithCache implemented
- [x] At least InboxActivity uses cache (not direct API)
- [x] Project builds without errors
- [x] Cache-first pattern works (fast load)
- [x] Logout clears all caches including boards

#### 🟡 SHOULD HAVE (Nên có):
- [ ] ProjectActivity & HomeActivity use cache
- [ ] WorkspaceRepositoryImplWithCache implemented
- [ ] Offline mode tested (works without network)
- [ ] Performance measured (cache vs API)
- [ ] Loading indicators show cache vs API state

#### 🟢 NICE TO HAVE (Tốt nếu có):
- [ ] Unit tests for all DAOs
- [ ] Integration tests for cached repositories
- [ ] UI shows "last synced" timestamp
- [ ] Pull-to-refresh force syncs from API
- [ ] Cache expiration policy (e.g., 5 minutes)

---

## 📊 SUCCESS METRICS

Đo lường thành công:

| Metric | Target | How to Measure |
|--------|--------|----------------|
| Load time (cached) | < 50ms | Log timestamps |
| Load time (API) | 500-2000ms | Log timestamps |
| Improvement | > 90% | (API-Cache)/API |
| Offline mode | 100% read | Test without network |
| Cache hit rate | > 80% | Log cache hits vs misses |
| No crashes | 0 crashes | Test 50+ interactions |

---

## 🚨 RISK MITIGATION

### Risk #1: WorkspaceEntity migration fails
**Mitigation:** Use fallbackToDestructiveMigration (already in code)  
**Impact:** Users will lose cached data, but app won't crash  
**Action:** Test migration on clean install + upgrade scenario

### Risk #2: BoardEntity conflicts with existing data
**Mitigation:** Version 3 is new, no existing board data  
**Impact:** Low risk  
**Action:** Clean install for testing

### Risk #3: UI integration breaks existing functionality
**Mitigation:** Test all CRUD operations after integration  
**Impact:** Medium risk  
**Action:** Keep API fallback if cache fails

---

## 📞 COMMUNICATION PROTOCOL

### Daily Stand-up (15 minutes):
- **Person 1 report:** "BoardEntity status, WorkspaceEntity fix status"
- **Person 2 report:** "BoardRepositoryImplWithCache status, UI integration status"
- **Blockers:** Any issues preventing progress

### End of Day 1:
- Person 1: Show BoardEntity builds, WorkspaceEntity fixed
- Person 2: Show BoardRepositoryImplWithCache code review

### End of Day 2:
- Person 1: Demo unit tests passing
- Person 2: Demo InboxActivity loading from cache

### End of Day 3:
- Both: Demo offline mode working
- Both: Show performance metrics (cache vs API)
- Both: Show all tests passing

---

## 📝 DELIVERABLES

By end of Day 3, submit:

1. **Code changes** (PR/commit):
   - WorkspaceEntity.java (fixed)
   - BoardEntity.java (new)
   - BoardDao.java (new)
   - BoardEntityMapper.java (new)
   - BoardRepositoryImplWithCache.java (new)
   - WorkspaceRepositoryImplWithCache.java (new)
   - InboxActivity.java (modified - uses cache)
   - ProjectActivity.java (modified - uses cache)
   - DependencyProvider.java (updated)

2. **Test results**:
   - Unit test report (BoardDaoTest, etc.)
   - Performance metrics log
   - Offline mode test video/screenshots

3. **Documentation**:
   - Brief implementation notes
   - Known issues (if any)
   - Next steps recommendations

---

**Ready to start? Good luck! 🚀**

**Questions? Ask team leader immediately!**

