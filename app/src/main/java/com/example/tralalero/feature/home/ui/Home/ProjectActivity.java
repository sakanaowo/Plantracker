package com.example.tralalero.feature.home.ui.Home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.tralalero.R;
import com.example.tralalero.App.App;
import com.example.tralalero.adapter.BoardAdapter;
import com.example.tralalero.domain.model.Board;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.feature.home.ui.Home.project.CardDetailActivity;
import com.example.tralalero.feature.home.ui.Home.project.CreateTaskBottomSheet;
import com.example.tralalero.feature.home.ui.Home.project.ProjectCalendarFragment;
import com.example.tralalero.feature.home.ui.Home.project.ProjectSummaryFragment;
import com.example.tralalero.feature.home.ui.Home.project.ProjectEventsFragment;
import com.example.tralalero.feature.home.ui.Home.project.ProjectAllWorkFragment;
import com.example.tralalero.presentation.viewmodel.BoardViewModel;
import com.example.tralalero.presentation.viewmodel.ProjectViewModel;
import com.example.tralalero.presentation.viewmodel.TaskViewModel;
import com.example.tralalero.presentation.viewmodel.TaskViewModelFactory;
import com.example.tralalero.presentation.viewmodel.ViewModelFactoryProvider;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.remote.api.CommentApiService;
import com.example.tralalero.data.remote.api.AttachmentApiService;
import com.example.tralalero.data.remote.dto.task.TaskDTO;
import com.example.tralalero.data.repository.TaskRepositoryImpl;
import com.example.tralalero.domain.repository.ITaskRepository;
import com.example.tralalero.domain.usecase.task.*;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.utils.SwipeGestureListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.material.tabs.TabLayout;

public class ProjectActivity extends AppCompatActivity implements 
    BoardAdapter.OnBoardActionListener, 
    BoardAdapter.OnTaskPositionChangeListener, 
    BoardAdapter.OnCrossBoardDragListener, // ✅ NEW: Cross-board drag support
    BoardAdapter.OnTaskStatusChangeListener {
    private static final String TAG = "ProjectActivity";
    private static final int REQUEST_CODE_CREATE_TASK = 2001;
    private static final int REQUEST_CODE_EDIT_TASK = 2002;

    private ProjectViewModel projectViewModel;
    private BoardViewModel boardViewModel;
    private TaskViewModel taskViewModel;

    private RecyclerView boardsRecyclerView;
    private BoardAdapter boardAdapter;
    private ProgressBar progressBar;
    private TextView tvProjectName;
    private TextView tvWorkspaceName;
    private ImageButton backButton;
    private TabLayout tabLayout;
    private GestureDetector gestureDetector;

    private String projectId;
    private String projectName;
    private String workspaceId;
    private String workspaceName;
    // ✅ REMOVED: State moved to ProjectViewModel
    // ❌ OLD: private List<Board> boards = new ArrayList<>();
    // ❌ OLD: private final Map<String, List<Task>> tasksPerBoard = new HashMap<>();
    private ActivityResultLauncher<Intent> inboxActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.project_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getIntentData();
        setupActivityResultLauncher();  // 📥 Setup result launcher
        setupViewModels();
        initViews();
        setupRecyclerView();
        setupTabs();
        setupSwipeGesture();  // ✅ Add swipe gesture support
        observeViewModels();
        
        // ✅ MVVM: Load once via ProjectViewModel - auto-loads boards and tasks
        projectViewModel.selectProject(projectId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ✅ Reload boards and tasks when returning from CardDetailActivity
        Log.d(TAG, "📱 onResume - Reloading boards and tasks for project: " + projectId);
        if (projectId != null && !projectId.isEmpty()) {
            // ✅ Force reload both boards and tasks to get latest updates
            projectViewModel.loadBoardsForProject(projectId);
            Log.d(TAG, "  ✅ Triggered full reload - boards and tasks will update via observers");
        }
    }

    private void getIntentData() {
        projectId = getIntent().getStringExtra("project_id");
        // Also support PROJECT_ID (uppercase) from notification navigation
        if (projectId == null) {
            projectId = getIntent().getStringExtra("PROJECT_ID");
        }
        
        projectName = getIntent().getStringExtra("project_name");
        workspaceId = getIntent().getStringExtra("workspace_id");
        workspaceName = getIntent().getStringExtra("workspace_name");

        Log.d(TAG, "=== ProjectActivity Started ===");
        Log.d(TAG, "Project ID: " + projectId);

        if (projectId == null || projectId.isEmpty()) {
            Log.e(TAG, "ERROR: No project_id provided!");
            Toast.makeText(this, "Error: No project ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Check if we need to open calendar tab (from notification)
        boolean openCalendarTab = getIntent().getBooleanExtra("OPEN_CALENDAR_TAB", false);
        String eventId = getIntent().getStringExtra("EVENT_ID");
        
        if (openCalendarTab) {
            Log.d(TAG, "📅 Notification intent: Opening calendar tab for event: " + eventId);
            // Will switch to calendar tab after setup completes
            getIntent().putExtra("_SWITCH_TO_CALENDAR", true);
        }
    }

    private void setupActivityResultLauncher() {
        inboxActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String taskId = data.getStringExtra("task_id");
                    String boardId = data.getStringExtra("board_id");

                    Log.d(TAG, "📥 Received result: Task created - taskId: " + taskId + ", boardId: " + boardId);
                    
                    // ✅ MVVM: Observer auto-updates UI
                    // No manual reload needed
                    Toast.makeText(this, "✅ Task added to board!", Toast.LENGTH_SHORT).show();
                }
            }
        );
        Log.d(TAG, "📥 ActivityResultLauncher registered");
    }

    private void setupViewModels() {
        projectViewModel = new ViewModelProvider(this,
                ViewModelFactoryProvider.provideProjectViewModelFactory()
        ).get(ProjectViewModel.class);

        boardViewModel = new ViewModelProvider(this,
                ViewModelFactoryProvider.provideBoardViewModelFactory()
        ).get(BoardViewModel.class);

        TaskApiService apiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        CommentApiService commentApiService = ApiClient.get(App.authManager).create(CommentApiService.class);
        AttachmentApiService attachmentApiService = ApiClient.get(App.authManager).create(AttachmentApiService.class);
        ITaskRepository repository = new TaskRepositoryImpl(apiService, commentApiService, attachmentApiService);

        GetTaskByIdUseCase getTaskByIdUseCase = new GetTaskByIdUseCase(repository);
        GetTasksByBoardUseCase getTasksByBoardUseCase = new GetTasksByBoardUseCase(repository);
        CreateTaskUseCase createTaskUseCase = new CreateTaskUseCase(repository);
        CreateQuickTaskUseCase createQuickTaskUseCase = new CreateQuickTaskUseCase(repository);
        UpdateTaskUseCase updateTaskUseCase = new UpdateTaskUseCase(repository);
        DeleteTaskUseCase deleteTaskUseCase = new DeleteTaskUseCase(repository);
        AssignTaskUseCase assignTaskUseCase = new AssignTaskUseCase(repository);
        UnassignTaskUseCase unassignTaskUseCase = new UnassignTaskUseCase(repository);
        MoveTaskToBoardUseCase moveTaskToBoardUseCase = new MoveTaskToBoardUseCase(repository);
        UpdateTaskPositionUseCase updateTaskPositionUseCase = new UpdateTaskPositionUseCase(repository);
        AddCommentUseCase addCommentUseCase = new AddCommentUseCase(repository);
        GetTaskCommentsUseCase getTaskCommentsUseCase = new GetTaskCommentsUseCase(repository);
        AddAttachmentUseCase addAttachmentUseCase = new AddAttachmentUseCase(repository);
        GetTaskAttachmentsUseCase getTaskAttachmentsUseCase = new GetTaskAttachmentsUseCase(repository);
        AddChecklistUseCase addChecklistUseCase = new AddChecklistUseCase(repository);
        GetTaskChecklistsUseCase getTaskChecklistsUseCase = new GetTaskChecklistsUseCase(repository);
        UpdateCommentUseCase updateCommentUseCase = new UpdateCommentUseCase(repository);
        DeleteCommentUseCase deleteCommentUseCase = new DeleteCommentUseCase(repository);
        DeleteAttachmentUseCase deleteAttachmentUseCase = new DeleteAttachmentUseCase(repository);
        GetAttachmentViewUrlUseCase getAttachmentViewUrlUseCase = new GetAttachmentViewUrlUseCase(repository);

        TaskViewModelFactory taskFactory = new TaskViewModelFactory(
                getTaskByIdUseCase, getTasksByBoardUseCase, createTaskUseCase,
                createQuickTaskUseCase, updateTaskUseCase, deleteTaskUseCase, assignTaskUseCase,
                unassignTaskUseCase, moveTaskToBoardUseCase, updateTaskPositionUseCase,
                addCommentUseCase, getTaskCommentsUseCase, addAttachmentUseCase,
                getTaskAttachmentsUseCase, addChecklistUseCase, getTaskChecklistsUseCase,
                updateCommentUseCase, deleteCommentUseCase, deleteAttachmentUseCase, getAttachmentViewUrlUseCase,
                repository
        );

        taskViewModel = new ViewModelProvider(this, taskFactory).get(TaskViewModel.class);
        Log.d(TAG, "ViewModels setup complete");
    }

    private void initViews() {
//        TODO: fix this red line
        boardsRecyclerView = findViewById(R.id.boardsRecyclerView);
        backButton = findViewById(R.id.btnClosePjrDetail);
        progressBar = findViewById(R.id.progressBar);
        tvProjectName = findViewById(R.id.tvProjectName);
        tvWorkspaceName = findViewById(R.id.tvWorkspaceName);
        tabLayout = findViewById(R.id.tabLayout);
        ImageView ivProjectMenu = findViewById(R.id.ivProjectMenu);

        if (projectName != null && !projectName.isEmpty()) {
            tvProjectName.setText(projectName);
        }
        
      

        backButton.setOnClickListener(v -> {
            // Just finish() to return to previous WorkspaceActivity (don't create new one)
            finish();
        });
        
        // Click vào icon 3 chấm để mở Project Menu
        ivProjectMenu.setOnClickListener(v -> {
            showProjectMenu();
        });
    }
    
    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0: // Summary tab
                        showSummaryFragment();
                        break;
                    case 1: // All Work tab
                        showAllWorkFragment();
                        break;
                    case 2: // Board tab
                        showBoardView();
                        break;
                    case 3: // Calendar tab
                        showCalendarFragment();
                        break;
                    case 4: // Event tab
                        showEventsFragment();
                        break;
                }
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        
        // Check if we should open specific tab (from activity feed or notification)
        int tabIndex = getIntent().getIntExtra("TAB_INDEX", -1);
        boolean shouldOpenCalendar = getIntent().getBooleanExtra("_SWITCH_TO_CALENDAR", false);
        
        if (tabIndex >= 0) {
            // Open specific tab from intent
            TabLayout.Tab tab = tabLayout.getTabAt(tabIndex);
            if (tab != null) {
                tab.select();
                Log.d(TAG, "📍 Auto-selected tab index: " + tabIndex);
            }
        } else if (shouldOpenCalendar) {
            // Open Calendar tab (index 3) from notification
            TabLayout.Tab calendarTab = tabLayout.getTabAt(3);
            if (calendarTab != null) {
                calendarTab.select();
                Log.d(TAG, "📅 Auto-selected Calendar tab from notification");
            }
        } else {
            // Default: Select Board tab (index 2)
            TabLayout.Tab boardTab = tabLayout.getTabAt(2);
            if (boardTab != null) {
                boardTab.select();
            }
        }
    }
    
    private void setupSwipeGesture() {
        // ✅ Setup gesture detector for swipe navigation between tabs
        gestureDetector = new GestureDetector(this, new SwipeGestureListener() {
            @Override
            public void onSwipeLeft() {
                // Swipe left = next tab
                int currentTab = tabLayout.getSelectedTabPosition();
                if (currentTab < 4) { // Max tab index is 4 (0-4 = 5 tabs)
                    TabLayout.Tab nextTab = tabLayout.getTabAt(currentTab + 1);
                    if (nextTab != null) {
                        nextTab.select();
                    }
                }
            }

            @Override
            public void onSwipeRight() {
                // Swipe right = previous tab
                int currentTab = tabLayout.getSelectedTabPosition();
                if (currentTab > 0) {
                    TabLayout.Tab prevTab = tabLayout.getTabAt(currentTab - 1);
                    if (prevTab != null) {
                        prevTab.select();
                    }
                }
            }
        });
        
        // Apply gesture to root layout
        View rootView = findViewById(android.R.id.content);
        rootView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false; // Don't consume - let children handle clicks
        });
    }
    
    private void showSummaryFragment() {
        boardsRecyclerView.setVisibility(View.GONE);
        FrameLayout fragmentContainer = findViewById(R.id.fragmentContainer);
        fragmentContainer.setVisibility(View.VISIBLE);
        
        androidx.fragment.app.Fragment summaryFragment = getSupportFragmentManager()
            .findFragmentByTag("summary_fragment");
        
        // Hide other fragments
        hideOtherFragments("summary_fragment");
        
        if (summaryFragment == null) {
            summaryFragment = ProjectSummaryFragment.newInstance(projectId);
            getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer, summaryFragment, "summary_fragment")
                .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                .show(summaryFragment)
                .commit();
        }
    }
    
    private void showAllWorkFragment() {
        boardsRecyclerView.setVisibility(View.GONE);
        FrameLayout fragmentContainer = findViewById(R.id.fragmentContainer);
        fragmentContainer.setVisibility(View.VISIBLE);
        
        androidx.fragment.app.Fragment allWorkFragment = getSupportFragmentManager()
            .findFragmentByTag("all_work_fragment");
        
        // Hide other fragments
        hideOtherFragments("all_work_fragment");
        
        if (allWorkFragment == null) {
            allWorkFragment = ProjectAllWorkFragment.newInstance(projectId);
            getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer, allWorkFragment, "all_work_fragment")
                .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                .show(allWorkFragment)
                .commit();
        }
    }
    
    private void showBoardView() {
        boardsRecyclerView.setVisibility(View.VISIBLE);
        FrameLayout fragmentContainer = findViewById(R.id.fragmentContainer);
        fragmentContainer.setVisibility(View.GONE);
        
        // Hide all fragments
        hideOtherFragments(null);
    }
    
    private void showCalendarFragment() {
        boardsRecyclerView.setVisibility(View.GONE);
        FrameLayout fragmentContainer = findViewById(R.id.fragmentContainer);
        fragmentContainer.setVisibility(View.VISIBLE);
        
        androidx.fragment.app.Fragment calendarFragment = getSupportFragmentManager()
            .findFragmentByTag("calendar_fragment");
        
        // Hide other fragments
        hideOtherFragments("calendar_fragment");
        
        if (calendarFragment == null) {
            calendarFragment = ProjectCalendarFragment.newInstance(projectId);
            getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer, calendarFragment, "calendar_fragment")
                .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                .show(calendarFragment)
                .commit();
        }
    }
    
    private void showEventsFragment() {
        boardsRecyclerView.setVisibility(View.GONE);
        FrameLayout fragmentContainer = findViewById(R.id.fragmentContainer);
        fragmentContainer.setVisibility(View.VISIBLE);
        
        androidx.fragment.app.Fragment eventsFragment = getSupportFragmentManager()
            .findFragmentByTag("events_fragment");
        
        // Hide other fragments
        hideOtherFragments("events_fragment");
        
        if (eventsFragment == null) {
            eventsFragment = ProjectEventsFragment.newInstance(projectId);
            getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer, eventsFragment, "events_fragment")
                .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                .show(eventsFragment)
                .commit();
        }
    }
    
    private void hideOtherFragments(String exceptTag) {
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        
        String[] fragmentTags = {"summary_fragment", "all_work_fragment", "calendar_fragment", "events_fragment"};
        for (String tag : fragmentTags) {
            if (!tag.equals(exceptTag)) {
                androidx.fragment.app.Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
                if (fragment != null) {
                    transaction.hide(fragment);
                }
            }
        }
        
        transaction.commit();
    }
    
    private void showProjectMenu() {
        com.example.tralalero.feature.home.ui.Home.project.ProjectMenuBottomSheet bottomSheet = 
            com.example.tralalero.feature.home.ui.Home.project.ProjectMenuBottomSheet.newInstance(projectId, projectName);
        bottomSheet.show(getSupportFragmentManager(), "ProjectMenuBottomSheet");
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false
        );
        boardsRecyclerView.setLayoutManager(layoutManager);

        boardAdapter = new BoardAdapter(this); // ✅ Pass 'this' as listener

        boardsRecyclerView.setAdapter(boardAdapter);
    }
    public void onAddCardClick(Board board) {
        showCreateTaskDialog(board);
    }

    public void onBoardMenuClick(Board board) {
        Toast.makeText(this, "Board menu: " + board.getName(), Toast.LENGTH_SHORT).show();
    }

    public void onTaskClick(Task task, Board board) {
        showTaskDetailBottomSheet(task, board);
    }

    public List<Task> getTasksForBoard(String boardId) {
        // ✅ MVVM: Get from ViewModel, not Activity state
        Map<String, List<Task>> tasksMap = projectViewModel.getTasksPerBoard().getValue();
        return tasksMap != null && tasksMap.containsKey(boardId) 
            ? tasksMap.get(boardId) 
            : new ArrayList<>();
    }
    public void onTaskPositionChanged(Task task, double newPosition, Board board) {
        Log.d(TAG, "🔄 Task '" + task.getTitle() + "' updating position to " + newPosition);
        
        // ✅ MVVM: Update position, then refresh board
        taskViewModel.updateTaskPosition(task.getId(), newPosition);
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            projectViewModel.refreshBoardTasks(board.getId());
        }, 300);
    }

    private void observeViewModels() {
        // ✅ Loading indicator
        projectViewModel.isLoading().observe(this, isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // ✅ Error handling
        projectViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                projectViewModel.clearError();
            }
        });

        // ✅ MVVM: Observe boards from ProjectViewModel
        projectViewModel.getBoards().observe(this, boards -> {
            if (boards != null) {
                Log.d(TAG, "📋 Loaded " + boards.size() + " boards from ProjectViewModel");
                boardAdapter.setBoards(boards);
            }
        });

        // ✅ MVVM: Observe tasksPerBoard from ProjectViewModel
        projectViewModel.getTasksPerBoard().observe(this, tasksMap -> {
            if (tasksMap != null) {
                Log.d(TAG, "📋 Loaded tasks for " + tasksMap.size() + " boards from ProjectViewModel");
                
                // ✅ Update each board's tasks in adapter
                // Note: updateTasksForBoard() already calls notifyDataSetChanged() in TaskAdapter
                for (Map.Entry<String, List<Task>> entry : tasksMap.entrySet()) {
                    boardAdapter.updateTasksForBoard(entry.getKey(), entry.getValue());
                }
                
                // ✅ No need to call boardAdapter.notifyDataSetChanged() here
                // as updateTasksForBoard() already updates each TaskAdapter
            }
        });
        
        // ✅ Observe task moved event - just show success message (no refresh needed)
        taskViewModel.getTaskMovedEvent().observe(this, event -> {
            if (event != null) {
                Log.d(TAG, "✅ Task moved confirmed by backend: taskId=" + event.taskId);
                Toast.makeText(this, "✅ Task moved successfully", Toast.LENGTH_SHORT).show();
            }
        });
        
        // ✅ Observe task move failed event - rollback optimistic update
        taskViewModel.getTaskMoveFailedEvent().observe(this, event -> {
            if (event != null) {
                Log.e(TAG, "❌ Task move failed, rolling back: " + event.error);
                projectViewModel.rollbackTaskMove(event.originalTask, event.targetBoardId);
                Toast.makeText(this, "❌ Failed to move task: " + event.error, Toast.LENGTH_LONG).show();
            }
        });
        
        // ✅ Observe task errors
        taskViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "❌ Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateTaskDialog(Board board) {
        CreateTaskBottomSheet bottomSheet = CreateTaskBottomSheet.newInstance();
        bottomSheet.setOnTaskCreatedListener(title -> {
            // Create task with just title
            // Note: For personal projects, user can manually assign themselves later via AssignMemberBottomSheet
            // For team projects, assignees are managed collaboratively
            Task newTask = new Task(
                null,              // id - will be generated by backend
                projectId,         // projectId
                board.getId(),     // boardId
                title,             // title from dialog
                "",                // empty description
                null,              // issueKey
                null,              // type
                Task.TaskStatus.TO_DO,    // default status
                Task.TaskPriority.MEDIUM, // default priority
                0.0,               // position (double)
                null,              // assigneeId - legacy field
                App.tokenManager.getInternalUserId(), // createdBy
                null,              // sprintId
                null,              // epicId
                null,              // parentTaskId
                null,              // startAt
                null,              // dueAt
                null,              // storyPoints
                null,              // originalEstimateSec
                null,              // remainingEstimateSec
                null,              // createdAt
                null,              // updatedAt
                false,             // calendarSyncEnabled
                null,              // calendarReminderMinutes
                null,              // calendarEventId
                null,              // calendarSyncedAt
                null,              // labels
                null               // assignees
            );

            // Create task via API
            taskViewModel.createTask(newTask);
            
            // ✅ MVVM: Refresh board to get new task from server
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                projectViewModel.refreshBoardTasks(board.getId());
                Toast.makeText(this, "✅ Task created", Toast.LENGTH_SHORT).show();
            }, 300);
        });
        
        bottomSheet.show(getSupportFragmentManager(), "CreateTaskBottomSheet");
    }

    private void showTaskDetailBottomSheet(Task task, Board board) {
        Intent intent = new Intent(this, CardDetailActivity.class);
        intent.putExtra(CardDetailActivity.EXTRA_TASK_ID, task.getId());
        intent.putExtra(CardDetailActivity.EXTRA_BOARD_ID, board.getId());
        intent.putExtra(CardDetailActivity.EXTRA_PROJECT_ID, projectId);
        intent.putExtra(CardDetailActivity.EXTRA_IS_EDIT_MODE, true);
        intent.putExtra(CardDetailActivity.EXTRA_BOARD_NAME, board.getName()); // Pass board name
        intent.putExtra(CardDetailActivity.EXTRA_TASK_TITLE, task.getTitle());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_DESCRIPTION, task.getDescription());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_PRIORITY, task.getPriority() != null ? task.getPriority().name() : "MEDIUM");
        intent.putExtra(CardDetailActivity.EXTRA_TASK_STATUS, task.getStatus() != null ? task.getStatus().name() : "TO_DO");
        intent.putExtra(CardDetailActivity.EXTRA_TASK_POSITION, task.getPosition());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_ASSIGNEE_ID, task.getAssigneeId());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_CREATED_BY, task.getCreatedBy());
        intent.putExtra("board_id_for_reload", board.getId());

        startActivityForResult(intent, REQUEST_CODE_EDIT_TASK);
    }
    
    /**
     * Show task detail from TaskDTO (used by Calendar fragment)
     */
    public void showTaskDetailFromDTO(TaskDTO taskDTO) {
        if (taskDTO == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(this, CardDetailActivity.class);
        intent.putExtra(CardDetailActivity.EXTRA_TASK_ID, taskDTO.getId());
        intent.putExtra(CardDetailActivity.EXTRA_BOARD_ID, taskDTO.getBoardId());
        intent.putExtra(CardDetailActivity.EXTRA_PROJECT_ID, projectId);
        intent.putExtra(CardDetailActivity.EXTRA_IS_EDIT_MODE, true);
        intent.putExtra(CardDetailActivity.EXTRA_BOARD_NAME, 
                taskDTO.getBoards() != null ? taskDTO.getBoards().getName() : "Unknown Board");
        intent.putExtra(CardDetailActivity.EXTRA_TASK_TITLE, taskDTO.getTitle());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_DESCRIPTION, taskDTO.getDescription());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_PRIORITY, taskDTO.getPriority() != null ? taskDTO.getPriority() : "MEDIUM");
        intent.putExtra(CardDetailActivity.EXTRA_TASK_STATUS, taskDTO.getStatus() != null ? taskDTO.getStatus() : "TO_DO");
        intent.putExtra(CardDetailActivity.EXTRA_TASK_POSITION, taskDTO.getPosition() != null ? taskDTO.getPosition() : 0.0);
        intent.putExtra(CardDetailActivity.EXTRA_TASK_ASSIGNEE_ID, taskDTO.getAssigneeId());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_CREATED_BY, taskDTO.getCreatedBy());
        intent.putExtra("board_id_for_reload", taskDTO.getBoardId());

        startActivityForResult(intent, REQUEST_CODE_EDIT_TASK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CREATE_TASK || requestCode == REQUEST_CODE_EDIT_TASK) {
                // ✅ MVVM: Observer auto-updates UI when task changes
                // No manual reload needed
                Log.d(TAG, "✅ Task updated - observer will auto-refresh UI");
            }
        }
    }

    // ✅ REMOVED: onMoveTaskToBoard (arrow buttons removed, replaced with drag & drop)
    
    /**
     * ✅ NEW: Handle cross-board drag & drop
     */
    @Override
    public void onTaskDroppedOnBoard(Task task, String sourceBoardId, String targetBoardId, int position) {
        Log.d(TAG, "onTaskDroppedOnBoard: taskId=" + task.getId() + 
            ", from=" + sourceBoardId + ", to=" + targetBoardId + ", position=" + position);
        
        if (sourceBoardId.equals(targetBoardId)) {
            Log.d(TAG, "Task dropped on same board - no action needed");
            return;
        }
        
        // Calculate new position based on drop location
        List<Task> targetBoardTasks = getTasksForBoard(targetBoardId);
        double newPosition = calculateDropPosition(targetBoardTasks, position);
        
        Log.d(TAG, "Moving task from board " + sourceBoardId + " to " + targetBoardId + " at position " + newPosition);
        
        // ✅ OPTIMISTIC UPDATE: Update UI immediately
        projectViewModel.moveTaskOptimistically(task, targetBoardId, newPosition);
        
        // ✅ Then call API in background
        taskViewModel.moveTaskToBoard(task, targetBoardId, newPosition);
        
        Toast.makeText(this, "✅ Moving task...", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Calculate position for dropped task
     */
    private double calculateDropPosition(List<Task> tasks, int dropIndex) {
        if (tasks == null || tasks.isEmpty()) {
            return 1000.0;
        }
        
        // Drop at beginning
        if (dropIndex == 0) {
            Task firstTask = tasks.get(0);
            return firstTask.getPosition() / 2.0;
        }
        
        // Drop at end
        if (dropIndex >= tasks.size()) {
            Task lastTask = tasks.get(tasks.size() - 1);
            return lastTask.getPosition() + 1024.0;
        }
        
        // Drop in middle
        Task prevTask = tasks.get(dropIndex - 1);
        Task nextTask = tasks.get(dropIndex);
        return (prevTask.getPosition() + nextTask.getPosition()) / 2.0;
    }
    
    @Override
    public void onTaskStatusChanged(Task task, boolean isDone) {
        Log.d(TAG, "onTaskStatusChanged: task=" + task.getTitle() + ", checkbox action triggered");
        
        // Simply toggle task status between TO_DO and DONE (same as All Work)
        Task.TaskStatus newStatus = task.getStatus() == Task.TaskStatus.DONE 
            ? Task.TaskStatus.TO_DO 
            : Task.TaskStatus.DONE;
        
        Log.d(TAG, "Updating task status from " + task.getStatus() + " to " + newStatus);
        
        // Update task status via API
        java.util.Map<String, Object> updatePayload = new java.util.HashMap<>();
        updatePayload.put("status", newStatus.name());
        
        TaskApiService taskApiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        taskApiService.updateTask(task.getId(), updatePayload).enqueue(new retrofit2.Callback<TaskDTO>() {
            @Override
            public void onResponse(retrofit2.Call<TaskDTO> call, retrofit2.Response<TaskDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Task status updated successfully");
                    
                    // Reload boards to refresh UI
                    if (projectViewModel != null) {
                        projectViewModel.loadBoardsForProject(projectId);
                    }
                    
                    String message = newStatus == Task.TaskStatus.DONE 
                        ? "✅ Task completed!" 
                        : "Task reopened";
                    Toast.makeText(ProjectActivity.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Failed to update task status: " + response.code());
                    Toast.makeText(ProjectActivity.this, "Failed to update task", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<TaskDTO> call, Throwable t) {
                Log.e(TAG, "Error updating task status", t);
                Toast.makeText(ProjectActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
