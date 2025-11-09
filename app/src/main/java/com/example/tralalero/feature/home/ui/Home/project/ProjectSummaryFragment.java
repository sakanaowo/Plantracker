package com.example.tralalero.feature.home.ui.Home.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tralalero.R;
import com.google.android.material.snackbar.Snackbar;

public class ProjectSummaryFragment extends Fragment {
    
    private String projectId;
    private ProjectSummaryViewModel viewModel;
    
    // Stats Views
    private TextView tvDoneCount;
    private TextView tvUpdatedCount;
    private TextView tvCreatedCount;
    private TextView tvDueCount;
    
    // Status Overview Views
    private TextView tvTotalWorkItems;
    private TextView tvToDoCount;
    private TextView tvInProgressCount;
    private TextView tvDoneStatusCount;
    
    // Loading & Refresh
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    public static ProjectSummaryFragment newInstance(String projectId) {
        ProjectSummaryFragment fragment = new ProjectSummaryFragment();
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
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_project_summary, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupViewModel();
        loadSummaryData();
    }
    
    private void initializeViews(View view) {
        // Stats card views
        tvDoneCount = view.findViewById(R.id.tvDoneCount);
        tvUpdatedCount = view.findViewById(R.id.tvUpdatedCount);
        tvCreatedCount = view.findViewById(R.id.tvCreatedCount);
        tvDueCount = view.findViewById(R.id.tvDueCount);
        
        // Status overview views
        tvTotalWorkItems = view.findViewById(R.id.tvTotalWorkItems);
        tvToDoCount = view.findViewById(R.id.tvToDoCount);
        tvInProgressCount = view.findViewById(R.id.tvInProgressCount);
        tvDoneStatusCount = view.findViewById(R.id.tvDoneStatusCount);
        
        // Loading indicator
        progressBar = view.findViewById(R.id.progressBar);
        
        // Swipe refresh
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadSummaryData();
        });
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProjectSummaryViewModel.class);
        
        // Observe summary data
        viewModel.getSummary().observe(getViewLifecycleOwner(), summary -> {
            if (summary != null) {
                updateStatsCards(
                    summary.getDone(),
                    summary.getUpdated(),
                    summary.getCreated(),
                    summary.getDue()
                );
                
                // Update status overview if available
                if (summary.getStatusOverview() != null) {
                    updateStatusOverview(
                        summary.getStatusOverview().getTotal(),
                        summary.getStatusOverview().getToDo(),
                        summary.getStatusOverview().getInProgress(),
                        summary.getStatusOverview().getDone()
                    );
                }
            }
        });
        
        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(isLoading);
            }
        });
        
        // Observe errors
        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty() && getView() != null) {
                Snackbar.make(getView(), 
                    "❌ " + errorMsg, 
                    Snackbar.LENGTH_LONG)
                    .setAction("Retry", v -> loadSummaryData())
                    .show();
            }
        });
    }
    
    private void loadSummaryData() {
        if (projectId != null && !projectId.isEmpty()) {
            viewModel.loadSummary(projectId);
        } else {
            Toast.makeText(getContext(), "No project selected", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateStatsCards(int doneCount, int updatedCount, int createdCount, int dueCount) {
        tvDoneCount.setText(doneCount + " done");
        tvUpdatedCount.setText(updatedCount + " updated");
        tvCreatedCount.setText(createdCount + " created");
        tvDueCount.setText(dueCount + " due");
    }
    
    private void updateStatusOverview(int total, int todoCount, int inProgressCount, int doneCount) {
        tvTotalWorkItems.setText(String.valueOf(total));
        tvToDoCount.setText(String.valueOf(todoCount));
        tvInProgressCount.setText(String.valueOf(inProgressCount));
        tvDoneStatusCount.setText(String.valueOf(doneCount));
    }
}
