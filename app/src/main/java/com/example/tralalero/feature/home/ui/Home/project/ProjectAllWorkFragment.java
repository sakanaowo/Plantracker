package com.example.tralalero.feature.home.ui.Home.project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tralalero.R;
import com.example.tralalero.App.App;
import com.example.tralalero.data.remote.api.TaskApiService;
import com.example.tralalero.data.remote.dto.task.TaskDTO;
import com.example.tralalero.data.dto.TaskStatisticsDTO;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.data.mapper.TaskMapper;
import com.example.tralalero.network.ApiClient;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectAllWorkFragment extends Fragment implements TaskAdapter.OnTaskClickListener {
    
    private static final String TAG = "ProjectAllWork";
    
    private String projectId;
    private TaskApiService taskApiService;
    private List<Task> allTasks = new ArrayList<>();
    private String currentFilter = "all"; // all, my, overdue
    
    // Views
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    // Filter chips
    private Chip chipAll;
    private Chip chipMyTasks;
    private Chip chipOverdue;

    public static ProjectAllWorkFragment newInstance(String projectId) {
        ProjectAllWorkFragment fragment = new ProjectAllWorkFragment();
        Bundle args = new Bundle();
        args.putString("project_id", projectId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString("project_id");
        }
        // ✅ Use authenticated ApiClient with FirebaseInterceptor
        taskApiService = ApiClient.get(App.authManager).create(TaskApiService.class);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_project_all_work, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        Log.d(TAG, "onViewCreated - projectId: " + projectId);
        
        initializeViews(view);
        setupRecyclerView();
        loadAssignedTasks();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - Refreshing tasks");
        loadAssignedTasks();
    }
    
    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewTasks);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::loadAssignedTasks);
        
        // Filter chips
        chipAll = view.findViewById(R.id.chipAll);
        chipMyTasks = view.findViewById(R.id.chipMyTasks);
        chipOverdue = view.findViewById(R.id.chipOverdue);
        
        setupFilterChips();
    }
    
    private void setupFilterChips() {
        chipAll.setOnClickListener(v -> {
            currentFilter = "all";
            applyFilter();
        });
        
        chipMyTasks.setOnClickListener(v -> {
            currentFilter = "my";
            applyFilter();
        });
        
        chipOverdue.setOnClickListener(v -> {
            currentFilter = "overdue";
            applyFilter();
        });
    }
    
    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(taskAdapter);
    }
    
    private void loadAssignedTasks() {
        if (projectId == null || projectId.isEmpty()) {
            Log.e(TAG, "No project ID available");
            Toast.makeText(getContext(), "No project selected", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "Loading ALL tasks for projectId: " + projectId);
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
        
        taskApiService.getAllTasksInProject(projectId).enqueue(new Callback<List<TaskDTO>>() {
            @Override
            public void onResponse(Call<List<TaskDTO>> call, Response<List<TaskDTO>> response) {
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<TaskDTO> taskDTOs = response.body();
                    Log.d(TAG, "Loaded " + taskDTOs.size() + " tasks");
                    
                    // Convert DTOs to domain models
                    List<Task> tasks = new ArrayList<>();
                    for (TaskDTO dto : taskDTOs) {
                        tasks.add(TaskMapper.toDomain(dto));
                    }
                    
                    allTasks = tasks; // Store for filtering
                    applyFilter(); // Apply current filter
                    
                    // Show empty state if no tasks
                    if (tasks.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        tvEmptyState.setText("No tasks in this project yet\n\nCreate tasks in the Board view to get started");
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                    }
                } else {
                    Log.e(TAG, "Failed to load tasks: " + response.code());
                    showError("Failed to load tasks");
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("Failed to load tasks");
                }
            }
            
            @Override
            public void onFailure(Call<List<TaskDTO>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                
                Log.e(TAG, "Error loading tasks", t);
                showError("Error: " + t.getMessage());
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("Error loading tasks");
            }
        });
    }
    
    private void applyFilter() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            taskAdapter.setTasks(allTasks);
            return;
        }
        String currentUserId = firebaseUser.getUid();
        
        List<Task> filteredTasks = allTasks;
        
        switch (currentFilter) {
            case "my":
                // Filter tasks assigned to current user
                filteredTasks = allTasks.stream()
                    .filter(task -> task.getAssignees() != null && 
                        task.getAssignees().stream()
                            .anyMatch(assignee -> currentUserId.equals(assignee.getId())))
                    .collect(Collectors.toList());
                break;
                
            case "overdue":
                // Filter overdue tasks (has due date in past and not DONE)
                long now = System.currentTimeMillis();
                filteredTasks = allTasks.stream()
                    .filter(task -> task.getDueAt() != null && 
                        task.getDueAt().getTime() < now &&
                        task.getStatus() != Task.TaskStatus.DONE)
                    .collect(Collectors.toList());
                break;
                
            case "all":
            default:
                // Show all tasks
                filteredTasks = allTasks;
                break;
        }
        
        taskAdapter.setTasks(filteredTasks);
        
        // Update empty state
        if (filteredTasks.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            if ("my".equals(currentFilter)) {
                tvEmptyState.setText("No tasks assigned to you");
            } else if ("overdue".equals(currentFilter)) {
                tvEmptyState.setText("No overdue tasks");
            } else {
                tvEmptyState.setText("No tasks in this project yet");
            }
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }
    }
    
    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                    .setAction("Retry", v -> loadAssignedTasks())
                    .show();
        }
    }
    
    @Override
    public void onTaskClick(Task task) {
        Log.d(TAG, "Task clicked: " + task.getId());
        
        Intent intent = new Intent(getActivity(), CardDetailActivity.class);
        // Use constants from CardDetailActivity to match Board navigation exactly
        intent.putExtra(CardDetailActivity.EXTRA_TASK_ID, task.getId());
        intent.putExtra(CardDetailActivity.EXTRA_PROJECT_ID, projectId);
        intent.putExtra(CardDetailActivity.EXTRA_IS_EDIT_MODE, true);
        
        // Include board info if available from task object
        if (task.getBoardId() != null && !task.getBoardId().isEmpty()) {
            intent.putExtra(CardDetailActivity.EXTRA_BOARD_ID, task.getBoardId());
        }
        if (task.getBoardName() != null && !task.getBoardName().isEmpty()) {
            intent.putExtra(CardDetailActivity.EXTRA_BOARD_NAME, task.getBoardName());
        }
        
        Log.d(TAG, "Navigating to CardDetail - taskId: " + task.getId() + 
            ", boardId: " + task.getBoardId() + 
            ", boardName: " + task.getBoardName());
        
        startActivity(intent);
    }
    
    @Override
    public void onTaskCheckboxClick(Task task, String currentBoardType) {
        Log.d(TAG, "Checkbox clicked for task: " + task.getId());
        
        // Toggle task status: TO_DO ↔ DONE
        Task.TaskStatus newStatus = task.getStatus() == Task.TaskStatus.DONE 
            ? Task.TaskStatus.TO_DO 
            : Task.TaskStatus.DONE;
        
        Log.d(TAG, "Updating task status from " + task.getStatus() + " to " + newStatus);
        
        updateTaskStatus(task, newStatus);
    }
    
    private void updateTaskStatus(Task task, Task.TaskStatus newStatus) {
        // Create update payload with ONLY the field we want to change
        // Using Map to avoid sending null values for other fields
        java.util.Map<String, Object> updatePayload = new java.util.HashMap<>();
        updatePayload.put("status", newStatus.name());
        
        taskApiService.updateTask(task.getId(), updatePayload).enqueue(new Callback<TaskDTO>() {
            @Override
            public void onResponse(Call<TaskDTO> call, Response<TaskDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Task status updated successfully");
                    
                    // Update local task list
                    Task updatedTask = TaskMapper.toDomain(response.body());
                    taskAdapter.updateTask(updatedTask);
                    
                    String message = newStatus == Task.TaskStatus.DONE 
                        ? "✅ Task completed!" 
                        : "Task reopened";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Failed to update task status: " + response.code());
                    showError("Failed to update task");
                }
            }
            
            @Override
            public void onFailure(Call<TaskDTO> call, Throwable t) {
                Log.e(TAG, "Error updating task status", t);
                showError("Error: " + t.getMessage());
            }
        });
    }
}
