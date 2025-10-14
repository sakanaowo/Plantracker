package com.example.tralalero.feature.home.ui.Home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tralalero.R;
import com.example.tralalero.feature.home.ui.Home.project.ListProjectAdapter;

import com.example.tralalero.feature.home.ui.InboxActivity;
import com.example.tralalero.presentation.viewmodel.BoardViewModel;
import com.example.tralalero.presentation.viewmodel.ProjectViewModel;
import com.example.tralalero.presentation.viewmodel.ViewModelFactoryProvider;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * ProjectActivity - Main activity for project management
 * 
 * Features:
 * - Display 3 tabs: TO DO, IN PROGRESS, DONE
 * - Each tab shows tasks from corresponding board
 * - Uses ViewPager2 with ListProjectAdapter
 * - Integrates ProjectViewModel to load boards
 * 
 * Phase 5 Integration:
 * - Load boards from API
 * - Map boards to tabs
 * - Pass boardIds to fragments
 * 
 * @author Người 2 + Người 3
 * @date 14/10/2025
 */
public class ProjectActivity extends AppCompatActivity {
    private static final String TAG = "ProjectActivity";

    private ImageButton backButton;

    // ViewModels
    private ProjectViewModel projectViewModel;
    private BoardViewModel boardViewModel;
    
    // UI Components
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private ListProjectAdapter adapter;
    
    // Data
    private String projectId;
    private String projectName;
    private String workspaceId;

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
        
        // Initialize views
        initViews();
        
        // Setup ViewPager with Adapter
        setupViewPager();
        
        // Setup back button
        setupBackButton();
        
        // Setup TabLayout
        setupTabs();
    }
    
    // ===== SETUP METHODS =====
    
    private void getIntentData() {

        projectId = getIntent().getStringExtra("project_id");
        projectName = getIntent().getStringExtra("project_name");
        workspaceId = getIntent().getStringExtra("workspace_id");
        
        // Debug logging
        Log.d(TAG, "=== ProjectActivity Started ===");
        Log.d(TAG, "Project ID: " + projectId);
        Log.d(TAG, "Project Name: " + projectName);
        Log.d(TAG, "Workspace ID: " + workspaceId);
        
        if (projectId == null || projectId.isEmpty()) {
            Log.e(TAG, "ERROR: No project_id provided! Check Intent extras.");

            Toast.makeText(this, "Error: No project ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout1);
        viewPager2 = findViewById(R.id.PrjViewPager2);
        backButton = findViewById(R.id.btnClosePjrDetail);
    }
    
    private void setupViewPager() {
        // Create adapter with projectId
        adapter = new ListProjectAdapter(this, projectId);
        viewPager2.setAdapter(adapter);
        
        Log.d(TAG, "ViewPager2 setup complete");
    }
    
    private void setupBackButton() {
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProjectActivity.this, WorkspaceActivity.class);
            intent.putExtra("WORKSPACE_ID", workspaceId); // ✅ FIX: Use correct key
            startActivity(intent);
            finish(); // Close current activity
        });
    }
    
    private void setupTabs() {

        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("TO DO");
                    break;
                case 1:
                    tab.setText("IN PROGRESS");
                    break;
                case 2:
                    tab.setText("DONE");
                    break;
            }
        }).attach();
        
        Log.d(TAG, "UI setup completed with " + adapter.getItemCount() + " tabs");
    }
    
    /**
     * Observe ViewModel LiveData
     * Phase 5: Replace direct API calls with ViewModel observations
     */
    private void observeViewModels() {
        // Observe project details
        projectViewModel.getSelectedProject().observe(this, project -> {
            if (project != null) {
                Log.d(TAG, "Project loaded: " + project.getName());
                // Update UI with project details if needed
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(project.getName());
                }
            }
        });
        
        // Observe loading state
        projectViewModel.isLoading().observe(this, isLoading -> {
            // TODO: Add ProgressBar to layout if needed in future
            Log.d(TAG, "Loading state: " + isLoading);
        });
        
        // Observe errors from ProjectViewModel
        projectViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Project error: " + error);
                projectViewModel.clearError();
            }
        });
        
        // Observe errors from BoardViewModel
        boardViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Board error: " + error);
                boardViewModel.clearError();
            }
        });
        
        // Observe board operations
        boardViewModel.getSelectedBoard().observe(this, board -> {
            if (board != null) {
                Log.d(TAG, "Board operation completed: " + board.getName());
                // Refresh the adapter if needed
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }
    
    /**
     * Load project details using ViewModel
     */
    private void loadProjectDetails() {
        Log.d(TAG, "Loading project details for: " + projectId);
        projectViewModel.loadProjectById(projectId);
    }
}
