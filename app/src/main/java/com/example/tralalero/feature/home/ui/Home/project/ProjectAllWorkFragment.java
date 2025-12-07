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

import java.util.Calendar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
            sortAndDisplayTasks(allTasks);
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
        
        // Apply smart sorting algorithm
        sortAndDisplayTasks(filteredTasks);
        
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
    
    /**
     * Smart sorting algorithm for All Work tab:
     * 
     * Priority order:
     * 1. Overdue tasks (due date < now) - sorted by due date ASC (oldest first)
     * 2. Due today tasks - sorted by priority DESC then time ASC
     * 3. Due this week tasks - sorted by priority DESC then due date ASC
     * 4. Tasks with priority but no due date - sorted by priority DESC
     * 5. Other tasks with due date - sorted by due date ASC
     * 6. Tasks without priority and due date - sorted by creation date DESC
     * 
     * Within each category, secondary sort by priority (HIGH > MEDIUM > LOW)
     */
    private void sortAndDisplayTasks(List<Task> tasks) {
        List<Task> sortedTasks = new ArrayList<>(tasks);
        
        long now = System.currentTimeMillis();
        long todayEnd = getTodayEndTime();
        long weekEnd = getWeekEndTime();
        
        Collections.sort(sortedTasks, (t1, t2) -> {
            // Get task properties
            Long due1 = t1.getDueAt() != null ? t1.getDueAt().getTime() : null;
            Long due2 = t2.getDueAt() != null ? t2.getDueAt().getTime() : null;
            Task.TaskPriority priority1 = t1.getPriority();
            Task.TaskPriority priority2 = t2.getPriority();
            
            // Determine categories
            int cat1 = getTaskCategory(t1, now, todayEnd, weekEnd);
            int cat2 = getTaskCategory(t2, now, todayEnd, weekEnd);
            
            // Sort by category first
            if (cat1 != cat2) {
                return Integer.compare(cat1, cat2);
            }
            
            // Within same category, apply secondary sorting
            switch (cat1) {
                case 1: // Overdue - oldest first
                    return Long.compare(due1, due2);
                    
                case 2: // Due today - priority DESC then time ASC
                case 3: // Due this week - priority DESC then due date ASC
                    int priorityCompare = comparePriority(priority2, priority1); // DESC
                    if (priorityCompare != 0) return priorityCompare;
                    return Long.compare(due1, due2);
                    
                case 4: // Has priority only - priority DESC
                    return comparePriority(priority2, priority1);
                    
                case 5: // Has due date only - due date ASC
                    return Long.compare(due1, due2);
                    
                case 6: // No priority, no due date - creation date DESC (newest first)
                default:
                    Long created1 = t1.getCreatedAt() != null ? t1.getCreatedAt().getTime() : 0L;
                    Long created2 = t2.getCreatedAt() != null ? t2.getCreatedAt().getTime() : 0L;
                    return Long.compare(created2, created1);
            }
        });
        
        taskAdapter.setTasks(sortedTasks);
    }
    
    /**
     * Categorize tasks for sorting priority
     * @return category number (lower = higher priority)
     */
    private int getTaskCategory(Task task, long now, long todayEnd, long weekEnd) {
        Long dueTime = task.getDueAt() != null ? task.getDueAt().getTime() : null;
        boolean hasPriority = task.getPriority() != null;
        boolean isDone = task.getStatus() == Task.TaskStatus.DONE;
        
        // Completed tasks go to bottom
        if (isDone) return 7;
        
        if (dueTime != null) {
            if (dueTime < now) return 1; // Overdue - highest priority
            if (dueTime <= todayEnd) return 2; // Due today
            if (dueTime <= weekEnd) return 3; // Due this week
            return 5; // Future due date
        }
        
        if (hasPriority) return 4; // Has priority but no due date
        
        return 6; // No priority, no due date
    }
    
    /**
     * Compare priorities: CRITICAL > HIGH > MEDIUM > LOW > null
     */
    private int comparePriority(Task.TaskPriority p1, Task.TaskPriority p2) {
        int val1 = getPriorityValue(p1);
        int val2 = getPriorityValue(p2);
        return Integer.compare(val1, val2);
    }
    
    private int getPriorityValue(Task.TaskPriority priority) {
        if (priority == null) return 0;
        switch (priority) {
            case HIGH: return 3;
            case MEDIUM: return 2;
            case LOW: return 1;
            default: return 0;
        }
    }
    
    private long getTodayEndTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTimeInMillis();
    }
    
    private long getWeekEndTime() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 7);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTimeInMillis();
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
