package com.example.tralalero.feature.home.ui.Home.project;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.presentation.viewmodel.TaskViewModel;
import com.example.tralalero.presentation.viewmodel.TaskViewModelFactory;
import com.example.tralalero.data.repository.TaskRepositoryImpl;
import com.example.tralalero.domain.repository.ITaskRepository;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.App.App;
import com.example.tralalero.domain.usecase.task.*;

import java.util.ArrayList;
import java.util.List;

public class ListProject extends Fragment {
    private static final String TAG = "ListProject";
    private static final String ARG_TYPE = "type";
    private static final String ARG_PROJECT_ID = "project_id";
    private static final String ARG_BOARD_ID = "board_id";

    // ===== FACTORY METHODS =====
    
    public static ListProject newInstance(String type) {
        ListProject fragment = new ListProject();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    public static ListProject newInstance(String type, String projectId) {
        ListProject fragment = new ListProject();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putString(ARG_PROJECT_ID, projectId);
        fragment.setArguments(args);
        return fragment;
    }
    
    public static ListProject newInstance(String type, String projectId, String boardId) {
        ListProject fragment = new ListProject();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putString(ARG_PROJECT_ID, projectId);
        args.putString(ARG_BOARD_ID, boardId);
        fragment.setArguments(args);
        return fragment;
    }

    // ===== INSTANCE VARIABLES =====
    
    private String type;
    private String projectId;
    private String boardId;
    
    // ViewModels
    private TaskViewModel taskViewModel;  // Phase 5 - New ViewModel
    private ListProjectViewModel legacyViewModel;  // Old ViewModel (deprecated)
    
    // UI Components
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private ProgressBar progressBar;
    private TextView emptyView;

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

        // Initialize views
        initViews(view);
        
        // Setup ViewModels
        setupViewModels();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Observe ViewModel data
        observeViewModel();
        
        // Load tasks
        loadTasks();

        return view;
    }
    
    // ===== SETUP METHODS =====
    
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
    }
    
    private void setupViewModels() {
        // Setup TaskViewModel (Phase 5 - New Architecture)
        setupTaskViewModel();
        
        // Keep legacy ViewModel for backward compatibility
        legacyViewModel = new ViewModelProvider(this).get(ListProjectViewModel.class);
    }
    
    private void setupTaskViewModel() {
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
        taskAdapter = new TaskAdapter(task -> {
            // Show TaskDetailBottomSheet
            showTaskDetailBottomSheet(task);
            Log.d(TAG, "Task clicked: " + task.getId());
        });
        
        recyclerView.setAdapter(taskAdapter);
    }

    private void observeViewModel() {
        // Observe tasks list
        taskViewModel.getTasks().observe(getViewLifecycleOwner(), domainTasks -> {
            if (domainTasks != null && !domainTasks.isEmpty()) {
                // TaskAdapter already uses domain.model.Task - no conversion needed!
                taskAdapter.setTasks(domainTasks);
                Log.d(TAG, "Loaded " + domainTasks.size() + " tasks for " + type);
                
                // Show/hide views
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            } else {
                // No tasks - show empty view
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText(getEmptyMessage());
                Log.d(TAG, "No tasks found for " + type);
            }
        });
        
        // Observe loading state
        taskViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                // Show loading
                progressBar.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.GONE);
                Log.d(TAG, "Loading tasks...");
            } else {
                // Hide loading
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Loading complete");
            }
        });
        
        // Observe errors
        taskViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error: " + error);
                
                // Show empty view with error
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText("Failed to load tasks\n" + error);
            }
        });
    }
    
    // ===== TASK ACTIONS =====
    
    /**
     * Show BottomSheet with task details and actions
     */
    private void showTaskDetailBottomSheet(Task task) {
        TaskDetailBottomSheet bottomSheet = TaskDetailBottomSheet.newInstance(task);
        
        bottomSheet.setOnActionClickListener(new TaskDetailBottomSheet.OnActionClickListener() {
            @Override
            public void onAssignTask(Task task) {
                handleAssignTask(task);
            }
            
            @Override
            public void onMoveTask(Task task) {
                handleMoveTask(task);
            }
            
            @Override
            public void onAddComment(Task task) {
                handleAddComment(task);
            }
            
            @Override
            public void onDeleteTask(Task task) {
                handleDeleteTask(task);
            }
        });
        
        bottomSheet.show(getParentFragmentManager(), "TaskDetailBottomSheet");
    }
    
    /**
     * Handle assign task action
     */
    private void handleAssignTask(Task task) {
        // Show dialog to enter user ID to assign
        android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setHint("Enter user ID");
        
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Assign Task")
            .setMessage("Assign \"" + task.getTitle() + "\" to user:")
            .setView(input)
            .setPositiveButton("Assign", (dialog, which) -> {
                String userId = input.getText().toString().trim();
                if (!userId.isEmpty()) {
                    taskViewModel.assignTask(task.getId(), userId);
                    // Reload tasks after a short delay to allow backend to update
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        loadTasks();
                        Toast.makeText(getContext(), "Task assigned successfully", Toast.LENGTH_SHORT).show();
                    }, 500);
                } else {
                    Toast.makeText(getContext(), "User ID cannot be empty", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    /**
     * Handle move task action
     */
    private void handleMoveTask(Task task) {
        // Get all boards to select from
        // For now, show a simple dialog with board ID input
        // TODO: Fetch actual boards from BoardViewModel and show a list
        
        android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setHint("Enter target board ID");
        
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Move Task")
            .setMessage("Move \"" + task.getTitle() + "\" to board:")
            .setView(input)
            .setPositiveButton("Move", (dialog, which) -> {
                String targetBoardId = input.getText().toString().trim();
                if (!targetBoardId.isEmpty()) {
                    // Use position 0 as default
                    taskViewModel.moveTaskToBoard(task.getId(), targetBoardId, 0);
                    // Reload tasks after a short delay
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        loadTasks();
                        Toast.makeText(getContext(), "Task moved successfully", Toast.LENGTH_SHORT).show();
                    }, 500);
                } else {
                    Toast.makeText(getContext(), "Board ID cannot be empty", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    /**
     * Handle add comment action
     */
    private void handleAddComment(Task task) {
        // Show dialog to enter comment text
        android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setHint("Enter your comment");
        input.setMinLines(3);
        input.setMaxLines(5);
        
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Add Comment")
            .setMessage("Add comment to \"" + task.getTitle() + "\":")
            .setView(input)
            .setPositiveButton("Add", (dialog, which) -> {
                String commentText = input.getText().toString().trim();
                if (!commentText.isEmpty()) {
                    // Create a TaskComment object
                    com.example.tralalero.domain.model.TaskComment comment = 
                        new com.example.tralalero.domain.model.TaskComment(
                            null, // id - will be generated
                            task.getId(), // taskId
                            null, // userId - should get from current user
                            commentText,
                            new java.util.Date() // createdAt
                        );
                    
                    taskViewModel.addComment(task.getId(), comment);
                    Toast.makeText(getContext(), "Comment added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    /**
     * Handle delete task action
     */
    private void handleDeleteTask(Task task) {
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete \"" + task.getTitle() + "\"?")
            .setPositiveButton("Delete", (dialog, which) -> {
                // Delete task
                taskViewModel.deleteTask(task.getId());
                // Reload tasks after a short delay
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    loadTasks();
                    Toast.makeText(getContext(), "Task deleted successfully", Toast.LENGTH_SHORT).show();
                }, 500);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    // ===== HELPER METHODS =====
    
    private String getEmptyMessage() {
        if (type != null) {
            return "No tasks in " + type;
        }
        return "No tasks";
    }

    private void loadTasks() {
        if (boardId != null && !boardId.isEmpty()) {
            // Phase 5 - Load by boardId (preferred)
            Log.d(TAG, "Loading tasks for boardId: " + boardId);
            taskViewModel.loadTasksByBoard(boardId);
            
        } else if (projectId != null && !projectId.isEmpty() && type != null) {
            // Legacy - Load by projectId + status
            String status = mapTypeToStatus(type);
            Log.d(TAG, "Loading tasks (legacy) for projectId: " + projectId + ", status: " + status);
            
            // Use legacy ViewModel
            legacyViewModel.loadTasks(projectId, status);
            
            Toast.makeText(getContext(), 
                "Legacy mode - Please update to use boardId", 
                Toast.LENGTH_SHORT).show();
            
        } else {
            Log.w(TAG, "No boardId or projectId provided");
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("No board selected");
        }
    }
    
    private String mapTypeToStatus(String type) {
        if (type == null) return "TO_DO";
        
        switch (type) {
            case "TO DO":
                return "TO_DO";
            case "IN PROGRESS":
                return "IN_PROGRESS";
            case "DONE":
                return "DONE";
            default:
                return "TO_DO";
        }
    }

    // ===== PUBLIC METHODS =====
    
    /**
     * Method to refresh tasks (can be called from parent activity)
     */
    public void refreshTasks() {
        Log.d(TAG, "Refreshing tasks...");
        loadTasks();
    }
    
    /**
     * Set boardId and reload tasks
     */
    public void setBoardId(String boardId) {
        this.boardId = boardId;
        if (taskViewModel != null) {
            loadTasks();
        }
    }
}