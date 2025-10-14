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
import android.widget.Toast;

import com.example.tralalero.R;
import com.example.tralalero.model.Task;
import com.example.tralalero.presentation.viewmodel.BoardViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displaying tasks for a specific board (TO DO, IN PROGRESS, DONE)
 * Phase 5: Updated to use injected BoardViewModel
 * 
 * @author Người 2 - Phase 5
 * @date 14/10/2025
 */
public class ListProject extends Fragment {
    private static final String TAG = "ListProject";
    private static final String ARG_TYPE = "type";
    private static final String ARG_PROJECT_ID = "project_id";

    /**
     * Legacy constructor without projectId
     * @deprecated Use newInstance with projectId and BoardViewModel
     */
    @Deprecated
    public static ListProject newInstance(String type) {
        ListProject fragment = new ListProject();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Legacy constructor with projectId only
     * @deprecated Use newInstance with BoardViewModel
     */
    @Deprecated
    public static ListProject newInstance(String type, String projectId) {
        ListProject fragment = new ListProject();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putString(ARG_PROJECT_ID, projectId);
        fragment.setArguments(args);
        return fragment;
    }
    
    /**
     * NEW: Constructor with BoardViewModel injection
     * Phase 5: Proper way to create fragment with ViewModel
     * 
     * @param type Board type/name (TO DO, IN PROGRESS, DONE)
     * @param projectId Project ID
     * @param boardViewModel Shared BoardViewModel instance from activity
     */
    public static ListProject newInstance(String type, String projectId, BoardViewModel boardViewModel) {
        ListProject fragment = new ListProject();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putString(ARG_PROJECT_ID, projectId);
        fragment.setArguments(args);
        // Store boardViewModel in fragment
        fragment.boardViewModel = boardViewModel;
        return fragment;
    }

    private String type;
    private String projectId;
    private String boardId; // Will be calculated from projectId and type
    private BoardViewModel boardViewModel; // Injected from activity
    private ListProjectViewModel legacyViewModel; // Keep for backward compatibility
    private TaskAdapter taskAdapter;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_list_frm, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get arguments
        if (getArguments() != null) {
            type = getArguments().getString(ARG_TYPE);
            projectId = getArguments().getString(ARG_PROJECT_ID);
        }
        
        Log.d(TAG, "onCreateView - Type: " + type + ", ProjectId: " + projectId);

        // Phase 5: Check if we have injected BoardViewModel
        if (boardViewModel != null) {
            Log.d(TAG, "Using injected BoardViewModel (Phase 5)");
            setupWithBoardViewModel();
        } else {
            Log.d(TAG, "Using legacy ListProjectViewModel (backward compatibility)");
            setupWithLegacyViewModel();
        }

        // Initialize adapter with click listener
        taskAdapter = new TaskAdapter(task -> {
            // Handle task click
            Toast.makeText(getContext(), "Clicked: " + task.getTitle(), Toast.LENGTH_SHORT).show();
            // TODO: Navigate to task detail screen
        });

        recyclerView.setAdapter(taskAdapter);

        return view;
    }
    
    /**
     * Phase 5: Setup with injected BoardViewModel
     */
    private void setupWithBoardViewModel() {
        // Calculate board ID based on projectId and type
        boardId = calculateBoardId(projectId, type);
        
        Log.d(TAG, "Calculated boardId: " + boardId);
        
        // Observe BoardViewModel
        observeBoardViewModel();
        
        // Load tasks for this board
        loadTasksWithBoardViewModel();
    }
    
    /**
     * Legacy: Setup with ListProjectViewModel
     */
    private void setupWithLegacyViewModel() {
        // Initialize ViewModel
        legacyViewModel = new ViewModelProvider(this).get(ListProjectViewModel.class);
        
        // Observe ViewModel data
        observeLegacyViewModel();

        // Load tasks based on status
        if (projectId != null && !projectId.isEmpty()) {
            loadTasksForStatus();
        } else {
            Log.w(TAG, "No project ID provided");
        }
    }
    
    /**
     * Phase 5: Observe BoardViewModel LiveData
     */
    private void observeBoardViewModel() {
        // Observe board tasks
        boardViewModel.getBoardTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null && !tasks.isEmpty()) {
                Log.d(TAG, "Tasks loaded from BoardViewModel: " + tasks.size() + " tasks for " + type);
                
                // Convert domain.model.Task to old model.Task
                // TODO: Phase 6 - Update adapter to use domain models directly
                List<Task> oldTasks = convertDomainTasksToOldModel(tasks);
                taskAdapter.setTasks(oldTasks);
            } else {
                Log.d(TAG, "No tasks found for " + type);
                taskAdapter.setTasks(new ArrayList<>());
            }
        });
        
        // Observe loading state
        boardViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // TODO: Add ProgressBar to layout if needed in future
            Log.d(TAG, "Loading state for " + type + ": " + isLoading);
        });
        
        // Observe errors
        boardViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error loading tasks for " + type + ": " + error);
                boardViewModel.clearError();
            }
        });
    }
    
    /**
     * Phase 5: Load tasks using BoardViewModel
     */
    private void loadTasksWithBoardViewModel() {
        if (boardId == null || boardId.isEmpty()) {
            Log.e(TAG, "Cannot load tasks: boardId is null");
            return;
        }
        
        Log.d(TAG, "Loading tasks for board: " + boardId);
        boardViewModel.loadBoardTasks(boardId);
    }
    
    /**
     * Calculate board ID from projectId and type
     * This is a temporary solution - ideally board IDs should come from API
     * TODO: Phase 6 - Get actual board IDs from project details
     */
    private String calculateBoardId(String projectId, String boardType) {
        if (projectId == null || boardType == null) {
            return null;
        }
        
        // Map board type to suffix
        String suffix;
        switch (boardType) {
            case "TO DO":
                suffix = "_todo";
                break;
            case "IN PROGRESS":
                suffix = "_inprogress";
                break;
            case "DONE":
                suffix = "_done";
                break;
            default:
                suffix = "_todo";
                break;
        }
        
        return projectId + suffix;
    }
    
    /**
     * Convert domain.model.Task to old model.Task
     * TODO: Phase 6 - Remove when adapter uses domain models
     */
    private List<Task> convertDomainTasksToOldModel(List<com.example.tralalero.domain.model.Task> domainTasks) {
        List<Task> oldTasks = new ArrayList<>();
        for (com.example.tralalero.domain.model.Task domainTask : domainTasks) {
            Task oldTask = new Task();
            
            // Basic fields
            oldTask.setId(domainTask.getId());
            oldTask.setTitle(domainTask.getTitle());
            oldTask.setDescription(domainTask.getDescription());
            
            // Convert enums to String
            if (domainTask.getStatus() != null) {
                oldTask.setStatus(domainTask.getStatus().name()); // TO_DO, IN_PROGRESS, IN_REVIEW, DONE
            }
            if (domainTask.getPriority() != null) {
                oldTask.setPriority(domainTask.getPriority().name()); // LOW, MEDIUM, HIGH
            }
            if (domainTask.getType() != null) {
                oldTask.setType(domainTask.getType().name()); // TASK, BUG, STORY, etc.
            }
            
            // IDs and relationships
            oldTask.setProjectId(domainTask.getProjectId());
            oldTask.setBoardId(domainTask.getBoardId());
            oldTask.setAssigneeId(domainTask.getAssigneeId());
            oldTask.setCreatedBy(domainTask.getCreatedBy());
            oldTask.setSprintId(domainTask.getSprintId());
            oldTask.setEpicId(domainTask.getEpicId());
            oldTask.setParentTaskId(domainTask.getParentTaskId());
            
            // Other fields
            oldTask.setIssueKey(domainTask.getIssueKey());
            oldTask.setPosition(domainTask.getPosition());
            oldTask.setStoryPoints(domainTask.getStoryPoints());
            oldTask.setOriginalEstimateSec(domainTask.getOriginalEstimateSec());
            oldTask.setRemainingEstimateSec(domainTask.getRemainingEstimateSec());
            
            // Dates
            oldTask.setStartAt(domainTask.getStartAt());
            oldTask.setDueAt(domainTask.getDueAt());
            oldTask.setCreatedAt(domainTask.getCreatedAt());
            oldTask.setUpdatedAt(domainTask.getUpdatedAt());
            
            oldTasks.add(oldTask);
        }
        return oldTasks;
    }

    /**
     * Legacy: Observe ListProjectViewModel
     */
    private void observeLegacyViewModel() {
        // TODO: Fix in Phase 6 - Complete migration to domain models
        // TEMPORARILY DISABLED - Uncomment after complete migration
        /*
        // Observe tasks
        legacyViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                taskAdapter.setTasks(tasks);
                Log.d(TAG, "Tasks updated: " + tasks.size() + " tasks for " + type);
            }
        });

        // Observe loading state
        legacyViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // TODO: Add ProgressBar to layout if needed in future
            Log.d(TAG, "Loading state: " + isLoading);
        });

        // Observe errors
        legacyViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error: " + error);
            }
        });
        */

        Log.w(TAG, "Legacy observeViewModel temporarily disabled - waiting for Phase 6 migration");
    }

    /**
     * Legacy: Load tasks using ListProjectViewModel
     */
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
        legacyViewModel.loadTasks(projectId, status);
    }

    /**
     * Method to refresh tasks (can be called from parent activity)
     * Phase 5: Updated to use BoardViewModel if available
     */
    public void refreshTasks() {
        if (boardViewModel != null && boardId != null) {
            // Phase 5: Use BoardViewModel
            loadTasksWithBoardViewModel();
        } else if (projectId != null && !projectId.isEmpty()) {
            // Legacy: Use ListProjectViewModel
            loadTasksForStatus();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload tasks when returning to this fragment
        refreshTasks();
    }
}