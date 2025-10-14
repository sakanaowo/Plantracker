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

    // ===== INSTANCE VARIABLES =====
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

    // ===== FACTORY METHOD =====
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
        
        // Load tasks
        if (boardId != null && !boardId.isEmpty()) {
            loadTasksForBoard();
        } else {
            Log.w(TAG, "No board ID provided");
        }

        return view;
    }
    
    // ===== SETUP METHODS =====
    
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
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
        
        recyclerView.setAdapter(taskAdapter);
    }

    private void observeViewModel() {
        // Observe tasks list
        taskViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null && !tasks.isEmpty()) {
                // ✅ TaskAdapter already uses domain.model.Task - no conversion needed!
                taskAdapter.updateTasks(tasks);
                Log.d(TAG, "Loaded " + tasks.size() + " tasks for " + type);

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
                Log.d(TAG, "No tasks found for " + type);
            }
        });
        
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
        // TODO: Implement TaskDetailBottomSheet
        Toast.makeText(getContext(), "Task: " + task.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private String getEmptyMessage() {
        return "No tasks in " + type;
    }
}