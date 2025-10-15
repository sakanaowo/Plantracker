package com.example.tralalero.feature.home.ui.Home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.Board;
import com.example.tralalero.feature.home.ui.Home.project.ListProject;
import com.example.tralalero.feature.home.ui.Home.project.ListProjectAdapter;

import com.example.tralalero.presentation.viewmodel.BoardViewModel;
import com.example.tralalero.presentation.viewmodel.ProjectViewModel;
import com.example.tralalero.presentation.viewmodel.ViewModelFactoryProvider;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

/**
 * ProjectActivity - Main activity for project management
 * 
 * Phase 6 - Task 2.2 Implementation:
 * - Load boards from API via BoardViewModel
 * - Create 3 default boards if none exist (TO DO, IN PROGRESS, DONE)
 * - Setup ViewPager2 with dynamic board IDs
 * - Tab navigation with board names
 *
 * Features:
 * - Display 3 tabs: TO DO, IN PROGRESS, DONE
 * - Each tab shows tasks from corresponding board
 * - Auto-create boards on first load
 * - Pass boardIds to fragments
 * 
 * @author Người 2 - Phase 6
 * @date 16/10/2025
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
    private ProgressBar progressBar;
    private TextView tvProjectName;
    private TextView tvWorkspaceName;

    // Data
    private String projectId;
    private String projectName;
    private String workspaceId;
    private String workspaceName;

    // Board IDs for 3 tabs
    private List<String> boardIds = new ArrayList<>();
    private int boardsCreatedCount = 0;

    // Flag to prevent infinite loop of board creation
    private boolean isDefaultBoardsCreated = false;

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

        // Get intent data
        getIntentData();
        
        // Setup ViewModels FIRST
        setupViewModels();

        // Initialize views
        initViews();
        
        // Observe ViewModels
        observeViewModels();

        // Setup ViewPager with Adapter (initially empty)
        setupViewPager();
        
        // Setup back button
        setupBackButton();
        
        // Load boards for this project
        loadBoards();
    }
    
    // ===== SETUP METHODS =====
    
    private void getIntentData() {
        projectId = getIntent().getStringExtra("project_id");
        projectName = getIntent().getStringExtra("project_name");
        workspaceId = getIntent().getStringExtra("workspace_id");
        workspaceName = getIntent().getStringExtra("workspace_name");

        // Debug logging
        Log.d(TAG, "=== ProjectActivity Started ===");
        Log.d(TAG, "Project ID: " + projectId);
        Log.d(TAG, "Project Name: " + projectName);
        Log.d(TAG, "Workspace ID: " + workspaceId);
        Log.d(TAG, "Workspace Name: " + workspaceName);

        if (projectId == null || projectId.isEmpty()) {
            Log.e(TAG, "ERROR: No project_id provided! Check Intent extras.");
            Toast.makeText(this, "Error: No project ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    /**
     * Setup ViewModels with dependency injection
     * Phase 6: Using ViewModelFactoryProvider
     */
    private void setupViewModels() {
        projectViewModel = new ViewModelProvider(this,
                ViewModelFactoryProvider.provideProjectViewModelFactory()
        ).get(ProjectViewModel.class);

        boardViewModel = new ViewModelProvider(this,
                ViewModelFactoryProvider.provideBoardViewModelFactory()
        ).get(BoardViewModel.class);

        Log.d(TAG, "ViewModels setup complete");
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout1);
        viewPager2 = findViewById(R.id.PrjViewPager2);
        backButton = findViewById(R.id.btnClosePjrDetail);
        progressBar = findViewById(R.id.progressBar);
        tvProjectName = findViewById(R.id.tvProjectName);
        tvWorkspaceName = findViewById(R.id.tvWorkspaceName);

        if (progressBar == null) {
            Log.w(TAG, "ProgressBar not found in layout");
        }

        // Set project name and workspace name from intent
        if (projectName != null && !projectName.isEmpty()) {
            tvProjectName.setText(projectName);
        } else {
            tvProjectName.setText("Project");
        }

        if (workspaceName != null && !workspaceName.isEmpty()) {
            tvWorkspaceName.setText(workspaceName);
        } else {
            tvWorkspaceName.setText("Workspace");
        }
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
            intent.putExtra("WORKSPACE_ID", workspaceId);
            startActivity(intent);
            finish();
        });
    }
    
    /**
     * Observe ViewModels LiveData
     * Phase 6: Complete implementation with board creation logic
     */
    private void observeViewModels() {
        // Observe project details
        projectViewModel.getSelectedProject().observe(this, project -> {
            if (project != null) {
                Log.d(TAG, "Project loaded: " + project.getName());
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(project.getName());
                }
            }
        });
        
        // Observe loading state
        projectViewModel.isLoading().observe(this, isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            Log.d(TAG, "Project loading state: " + isLoading);
        });

        boardViewModel.isLoading().observe(this, isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            Log.d(TAG, "Board loading state: " + isLoading);
        });
        
        // Observe errors
        projectViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Project error: " + error);
                projectViewModel.clearError();
            }
        });
        
        boardViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Board Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Board error: " + error);
                boardViewModel.clearError();
            }
        });
        
        // Observe boards loaded from API
        boardViewModel.getProjectBoards().observe(this, boards -> {
            if (boards != null && !boards.isEmpty()) {
                Log.d(TAG, "✅ Boards loaded successfully: " + boards.size() + " boards");
                setupTabsWithBoardIds(boards);
            } else {
                Log.w(TAG, "⚠️ No boards found for project");
                Toast.makeText(this, "No boards found", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Load boards for this project
     * Phase 6: Task 2.2 - Core Implementation
     * FIXED: Load boards from backend instead of creating new ones
     */
    private void loadBoards() {
        Log.d(TAG, "Loading boards for project: " + projectId);

        if (projectId == null || projectId.isEmpty()) {
            Log.e(TAG, "❌ Cannot load boards: projectId is null or empty");
            Toast.makeText(this, "Error: Invalid project ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load boards from backend (backend already created default boards)
        boardViewModel.loadBoardsByProject(projectId);
    }

    /**
     * ❌ REMOVED: createDefaultBoards() method
     * Backend already creates default boards automatically
     */

    /**
     * Setup tabs with default board names
     * Phase 6: Temporary solution for demo
     */
    private void setupTabsWithDefaultBoards() {
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

        Log.d(TAG, "Tabs setup complete with " + adapter.getItemCount() + " tabs");
    }

    /**
     * Setup tabs with actual board IDs from API
     * Phase 6: This will be used when backend endpoint is ready
     */
    private void setupTabsWithBoardIds(List<Board> boards) {
        if (boards == null || boards.isEmpty()) {
            Log.e(TAG, "No boards to setup tabs");
            return;
        }

        // Extract board IDs
        boardIds.clear();
        for (Board board : boards) {
            boardIds.add(board.getId());
        }

        // Update adapter with board IDs
        adapter.setBoardIds(boardIds);

        // Setup TabLayout with board names
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            if (position < boards.size()) {
                tab.setText(boards.get(position).getName());
            }
        }).attach();

        Log.d(TAG, "Tabs setup with " + boards.size() + " boards");

        // Notify fragments to reload tasks with new boardIds
        notifyFragmentsToReload();
    }

    /**
     * Notify all fragments to reload tasks with updated boardIds
     */
    private void notifyFragmentsToReload() {
        // ViewPager2 may have already created fragments
        // We need to update their boardIds and trigger reload
        viewPager2.post(() -> {
            for (int position = 0; position < adapter.getItemCount(); position++) {
                String boardId = adapter.getBoardIdForPosition(position);
                if (boardId != null && !boardId.isEmpty()) {
                    // Try to get fragment from ViewPager2
                    Fragment fragment = getSupportFragmentManager()
                            .findFragmentByTag("f" + position);

                    if (fragment instanceof ListProject) {
                        ListProject listProjectFragment = (ListProject) fragment;
                        listProjectFragment.updateBoardIdAndReload(boardId);
                        Log.d(TAG, "Updated fragment at position " + position + " with boardId: " + boardId);
                    }
                }
            }
        });
    }
}
