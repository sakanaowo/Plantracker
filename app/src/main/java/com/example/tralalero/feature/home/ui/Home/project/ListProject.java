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
import com.example.tralalero.model.Task;

public class ListProject extends Fragment {
    private static final String TAG = "ListProject";
    private static final String ARG_TYPE = "type";
    private static final String ARG_PROJECT_ID = "project_id";

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

    private String type;
    private String projectId;
    private ListProjectViewModel viewModel;
    private TaskAdapter taskAdapter;
    private ProgressBar progressBar;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_list_frm, container, false);

        // Initialize views
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get arguments
        if (getArguments() != null) {
            type = getArguments().getString(ARG_TYPE);
            projectId = getArguments().getString(ARG_PROJECT_ID);
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ListProjectViewModel.class);

        // Initialize adapter with click listener
        taskAdapter = new TaskAdapter(task -> {
            // Handle task click
            Toast.makeText(getContext(), "Clicked: " + task.getTitle(), Toast.LENGTH_SHORT).show();
            // TODO: Navigate to task detail screen
        });

        recyclerView.setAdapter(taskAdapter);

        // Observe ViewModel data
        observeViewModel();

        // Load tasks based on status
        if (projectId != null && !projectId.isEmpty()) {
            loadTasksForStatus();
        } else {
            Log.w(TAG, "No project ID provided, using sample data");
            // Fallback to sample data if no project ID
            // This can be removed when integration is complete
        }

        return view;
    }

    private void observeViewModel() {
        // Observe tasks
        viewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                taskAdapter.setTasks(tasks);
                Log.d(TAG, "Tasks updated: " + tasks.size() + " tasks for " + type);
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // TODO: Show/hide progress bar
            // if (progressBar != null) {
            //     progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            // }
        });

        // Observe errors
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error: " + error);
            }
        });
    }

    private void loadTasksForStatus() {
        // Map fragment type to API status
        String status;
        switch (type) {
            case "TO DO":
                status = "TO_DO";
                break;
            case "IN PROGRESS":
                status = "IN_PROGRESS";
                break;
            case "DONE":
                status = "DONE";
                break;
            default:
                status = "TO_DO";
                break;
        }

        Log.d(TAG, "Loading tasks for project: " + projectId + ", status: " + status);
        viewModel.loadTasks(projectId, status);
    }

    /**
     * Method to refresh tasks (can be called from parent activity)
     */
    public void refreshTasks() {
        if (projectId != null && !projectId.isEmpty()) {
            loadTasksForStatus();
        }
    }
}