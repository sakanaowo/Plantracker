package com.example.tralalero.feature.home.ui.Home.project;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import static android.app.Activity.RESULT_OK;

import com.example.tralalero.R;
import com.example.tralalero.adapter.TaskAdapter;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.presentation.viewmodel.TaskViewModel;
import com.example.tralalero.presentation.viewmodel.TaskViewModelFactory;
import com.example.tralalero.data.repository.TaskRepositoryImpl;
import com.example.tralalero.domain.repository.ITaskRepository;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.App.App;
import com.example.tralalero.domain.usecase.task.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displaying tasks for a specific board (TO DO, IN PROGRESS, DONE)
 * Phase 5: Using TaskViewModel with domain models only
 *
 * @author Phase 5 - Consolidated
 * @date 15/10/2025
 */
public class ListProject extends Fragment {
    private static final String TAG = "ListProject";
    private static final String ARG_TYPE = "type";
    private static final String ARG_PROJECT_ID = "project_id";
    private static final String ARG_BOARD_ID = "board_id";
    
    // Request code for CardDetailActivity
    private static final int REQUEST_CODE_CREATE_TASK = 1001;
    private static final int REQUEST_CODE_EDIT_TASK = 1002;

    private String type;
    private String projectId;
    private String boardId;

    // ViewModel
    private TaskViewModel taskViewModel;

    // UI Components
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TaskAdapter taskAdapter;
    private FloatingActionButton fabAddTask;
    private ItemTouchHelper itemTouchHelper; // ✅ For drag & drop

    public static ListProject newInstance(String type, String projectId, String boardId) {
        ListProject fragment = new ListProject();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putString(ARG_PROJECT_ID, projectId);
        args.putString(ARG_BOARD_ID, boardId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_list_frm, container, false);

        // Get arguments
        if (getArguments() != null) {
            type = getArguments().getString(ARG_TYPE);
            projectId = getArguments().getString(ARG_PROJECT_ID);
            boardId = getArguments().getString(ARG_BOARD_ID);
        }
        
        Log.d(TAG, "onCreateView - Type: " + type + ", BoardId: " + boardId);

        // Setup
        initViews(view);
        setupViewModel();
        setupRecyclerView();
        observeViewModel();
        
        // Load tasks if boardId is available
        if (boardId != null && !boardId.isEmpty()) {
            loadTasksForBoard();
        } else {
            Log.w(TAG, "BoardId not available yet, waiting for boards to load...");
            // Show loading state while waiting for boardId
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            if (emptyView != null) {
                emptyView.setVisibility(View.GONE);
            }
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check if boardId has been updated since fragment creation
        if (boardId == null || boardId.isEmpty()) {
            // Try to get boardId from adapter
            if (getActivity() != null) {
                // Fragment will be notified when boards are loaded via observeViewModel
                Log.d(TAG, "onResume - Still waiting for boardId");
            }
        }
    }

    
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
        fabAddTask = view.findViewById(R.id.fabAddTask);

        // Setup FAB click listener
        if (fabAddTask != null) {
            fabAddTask.setOnClickListener(v -> showCreateTaskDialog());
        }
    }
    
    private void setupViewModel() {
        // Create repository
        TaskApiService apiService = ApiClient.get(App.authManager).create(TaskApiService.class);
        ITaskRepository repository = new TaskRepositoryImpl(apiService);
        
        // Create UseCases
        GetTaskByIdUseCase getTaskByIdUseCase = new GetTaskByIdUseCase(repository);
        GetTasksByBoardUseCase getTasksByBoardUseCase = new GetTasksByBoardUseCase(repository);
        CreateTaskUseCase createTaskUseCase = new CreateTaskUseCase(repository);
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
        
        // Create Factory
        TaskViewModelFactory factory = new TaskViewModelFactory(
            getTaskByIdUseCase,
            getTasksByBoardUseCase,
            createTaskUseCase,
            updateTaskUseCase,
            deleteTaskUseCase,
            assignTaskUseCase,
            unassignTaskUseCase,
            moveTaskToBoardUseCase,
            updateTaskPositionUseCase,
            addCommentUseCase,
            getTaskCommentsUseCase,
            addAttachmentUseCase,
            getTaskAttachmentsUseCase,
            addChecklistUseCase,
            getTaskChecklistsUseCase
        );
        
        // Create ViewModel - shared across Activity
        taskViewModel = new ViewModelProvider(requireActivity(), factory).get(TaskViewModel.class);
        Log.d(TAG, "TaskViewModel initialized");
    }
    
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Initialize adapter with click listener
        taskAdapter = new TaskAdapter(new ArrayList<>());
        taskAdapter.setOnTaskClickListener(task -> {
            showTaskDetailBottomSheet(task);
            Log.d(TAG, "Task clicked: " + task.getId());
        });
        
        // ✅ Setup drag & drop listener
        taskAdapter.setOnTaskDragListener((fromPosition, toPosition) -> {
            Log.d(TAG, "Task moved from " + fromPosition + " to " + toPosition);
            // TODO: Update task position in backend
            Task movedTask = taskAdapter.getTaskAt(toPosition);
            if (movedTask != null) {
                // Calculate new position value
                double newPosition = toPosition * 1000.0;
                taskViewModel.updateTaskPosition(movedTask.getId(), newPosition);
            }
        });

        recyclerView.setAdapter(taskAdapter);

        // ✅ Setup ItemTouchHelper for drag & drop
        setupDragAndDrop();
    }

    /**
     * ✅ Setup drag and drop functionality
     */
    private void setupDragAndDrop() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, // Drag directions
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT // Swipe directions
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                // Move item in adapter
                taskAdapter.moveItem(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Optional: Handle swipe to delete
                int position = viewHolder.getAdapterPosition();
                Task task = taskAdapter.getTaskAt(position);

                if (direction == ItemTouchHelper.LEFT || direction == ItemTouchHelper.RIGHT) {
                    // Show confirmation dialog before delete
                    showDeleteConfirmation(task);
                }
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true; // Enable long press to drag
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return false; // Disable swipe to delete for now (can enable later)
            }
        };

        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * Show confirmation dialog before deleting task
     */
    private void showDeleteConfirmation(Task task) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete \"" + task.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Log.d(TAG, "Deleting task: " + task.getId());
                    taskViewModel.deleteTask(task.getId());
                    Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Refresh list to restore the item
                    taskAdapter.notifyDataSetChanged();
                })
                .setOnCancelListener(dialog -> {
                    // Refresh list to restore the item
                    taskAdapter.notifyDataSetChanged();
                })
                .show();
    }

    private void observeViewModel() {
        // ✅ FIX: Observe tasks for THIS specific board only
        // Wait until boardId is available
        if (boardId != null && !boardId.isEmpty()) {
            taskViewModel.getTasksForBoard(boardId).observe(getViewLifecycleOwner(), tasks -> {
                if (tasks != null && !tasks.isEmpty()) {
                    // ✅ TaskAdapter already uses domain.model.Task - no conversion needed!
                    taskAdapter.updateTasks(tasks);
                    Log.d(TAG, "Loaded " + tasks.size() + " tasks for board: " + boardId);

                    // Show/hide views
                    recyclerView.setVisibility(View.VISIBLE);
                    if (emptyView != null) {
                        emptyView.setVisibility(View.GONE);
                    }
                } else {
                    // No tasks - show empty view
                    recyclerView.setVisibility(View.GONE);
                    if (emptyView != null) {
                        emptyView.setVisibility(View.VISIBLE);
                        emptyView.setText(getEmptyMessage());
                    }
                    Log.d(TAG, "No tasks found for board: " + boardId);
                }
            });
        } else {
            Log.w(TAG, "Cannot observe tasks yet - boardId is null");
        }

        // Observe loading state
        taskViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            Log.d(TAG, "Loading state for " + type + ": " + isLoading);
        });
        
        // Observe errors
        taskViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error loading tasks for " + type + ": " + error);
                taskViewModel.clearError();
            }
        });
    }
    
    // ===== HELPER METHODS =====

    private void loadTasksForBoard() {
        if (boardId == null || boardId.isEmpty()) {
            Log.e(TAG, "Cannot load tasks: boardId is null");
            return;
        }
        
        Log.d(TAG, "Loading tasks for board: " + boardId);
        taskViewModel.loadTasksByBoard(boardId);
    }

    private void showTaskDetailBottomSheet(Task task) {
        // Open CardDetailActivity for editing
        Intent intent = new Intent(getContext(), CardDetailActivity.class);
        intent.putExtra(CardDetailActivity.EXTRA_TASK_ID, task.getId());
        intent.putExtra(CardDetailActivity.EXTRA_BOARD_ID, boardId);
        intent.putExtra(CardDetailActivity.EXTRA_PROJECT_ID, projectId);
        intent.putExtra(CardDetailActivity.EXTRA_IS_EDIT_MODE, true);
        intent.putExtra(CardDetailActivity.EXTRA_BOARD_NAME, type); // Pass board name
        
        // Pass task details
        intent.putExtra(CardDetailActivity.EXTRA_TASK_TITLE, task.getTitle());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_DESCRIPTION, task.getDescription());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_PRIORITY, task.getPriority() != null ? task.getPriority().name() : "MEDIUM");
        intent.putExtra(CardDetailActivity.EXTRA_TASK_STATUS, task.getStatus() != null ? task.getStatus().name() : "TO_DO");
        intent.putExtra(CardDetailActivity.EXTRA_TASK_POSITION, task.getPosition());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_ASSIGNEE_ID, task.getAssigneeId());
        intent.putExtra(CardDetailActivity.EXTRA_TASK_CREATED_BY, task.getCreatedBy());
        
        startActivityForResult(intent, REQUEST_CODE_EDIT_TASK);
    }

    /**
     * Show CardDetailActivity to create new task
     * Modified to use Activity instead of BottomSheet
     */
    private void showCreateTaskDialog() {
        if (boardId == null || boardId.isEmpty()) {
            Toast.makeText(getContext(), "Board not ready yet, please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (projectId == null || projectId.isEmpty()) {
            Toast.makeText(getContext(), "Project ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Opening CardDetailActivity for creating task in board: " + boardId);

        // Open CardDetailActivity for creating new task
        Intent intent = new Intent(getContext(), CardDetailActivity.class);
        intent.putExtra(CardDetailActivity.EXTRA_BOARD_ID, boardId);
        intent.putExtra(CardDetailActivity.EXTRA_PROJECT_ID, projectId);
        intent.putExtra(CardDetailActivity.EXTRA_IS_EDIT_MODE, false);
        intent.putExtra(CardDetailActivity.EXTRA_BOARD_NAME, type); // Pass board name
        
        startActivityForResult(intent, REQUEST_CODE_CREATE_TASK);
    }

    private String getEmptyMessage() {
        return "No tasks in " + type;
    }

    /**
     * Handle result from CardDetailActivity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CREATE_TASK) {
                Log.d(TAG, "Task created, reloading tasks...");
                // Reload tasks after creation
                if (boardId != null && !boardId.isEmpty()) {
                    // Small delay to ensure backend has processed the request
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        loadTasksForBoard();
                    }, 500);
                }
            } else if (requestCode == REQUEST_CODE_EDIT_TASK) {
                Log.d(TAG, "Task updated/deleted, reloading tasks...");
                // Reload tasks after update or delete
                if (boardId != null && !boardId.isEmpty()) {
                    // Small delay to ensure backend has processed the request
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        loadTasksForBoard();
                    }, 500);
                }
            }
        }
    }

    /**
     * Update boardId and reload tasks
     * Called when boards are loaded after fragment creation
     */
    public void updateBoardIdAndReload(String newBoardId) {
        if (newBoardId != null && !newBoardId.isEmpty() && !newBoardId.equals(this.boardId)) {
            Log.d(TAG, "Updating boardId from " + this.boardId + " to " + newBoardId);
            this.boardId = newBoardId;

            // Update arguments for configuration changes
            if (getArguments() != null) {
                getArguments().putString(ARG_BOARD_ID, newBoardId);
            }

            // ✅ FIX: Re-observe with new boardId
            taskViewModel.getTasksForBoard(newBoardId).observe(getViewLifecycleOwner(), tasks -> {
                if (tasks != null && !tasks.isEmpty()) {
                    taskAdapter.updateTasks(tasks);
                    Log.d(TAG, "Loaded " + tasks.size() + " tasks for updated board: " + newBoardId);
                    recyclerView.setVisibility(View.VISIBLE);
                    if (emptyView != null) {
                        emptyView.setVisibility(View.GONE);
                    }
                } else {
                    recyclerView.setVisibility(View.GONE);
                    if (emptyView != null) {
                        emptyView.setVisibility(View.VISIBLE);
                        emptyView.setText(getEmptyMessage());
                    }
                    Log.d(TAG, "No tasks found for updated board: " + newBoardId);
                }
            });

            // Reload tasks with new boardId
            loadTasksForBoard();
        }
    }

    /**
     * Get current boardId
     */
    public String getBoardId() {
        return boardId;
    }
}

