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
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.data.mapper.TaskMapper;
import com.example.tralalero.network.ApiClient;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectAllWorkFragment extends Fragment implements TaskAdapter.OnTaskClickListener {
    
    private static final String TAG = "ProjectAllWork";
    
    private String projectId;
    private TaskApiService taskApiService;
    
    // Views
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private SwipeRefreshLayout swipeRefreshLayout;

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
        Log.d(TAG, "onResume - Refreshing assigned tasks");
        loadAssignedTasks();
    }
    
    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewTasks);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::loadAssignedTasks);
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
        
        Log.d(TAG, "Loading assigned tasks for projectId: " + projectId);
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
        
        taskApiService.getMyAssignedTasksInProject(projectId).enqueue(new Callback<List<TaskDTO>>() {
            @Override
            public void onResponse(Call<List<TaskDTO>> call, Response<List<TaskDTO>> response) {
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<TaskDTO> taskDTOs = response.body();
                    Log.d(TAG, "Loaded " + taskDTOs.size() + " assigned tasks");
                    
                    // Convert DTOs to domain models
                    List<Task> tasks = new ArrayList<>();
                    for (TaskDTO dto : taskDTOs) {
                        tasks.add(TaskMapper.toDomain(dto));
                    }
                    
                    taskAdapter.setTasks(tasks);
                    
                    // Show empty state if no tasks
                    if (tasks.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        tvEmptyState.setText("No tasks assigned to you in this project");
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
        intent.putExtra("task_id", task.getId());
        intent.putExtra("project_id", projectId);
        startActivity(intent);
    }
    
    @Override
    public void onTaskCheckboxClick(Task task, String currentBoardType) {
        // Not needed for All Work view - tasks are read-only here
        Log.d(TAG, "Checkbox clicked for task: " + task.getId());
    }
}
