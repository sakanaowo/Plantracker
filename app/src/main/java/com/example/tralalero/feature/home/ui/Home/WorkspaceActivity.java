package com.example.tralalero.feature.home.ui.Home;

import static android.content.ContentValues.TAG;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tralalero.R;
import com.example.tralalero.adapter.WorkspaceAdapter;
import com.example.tralalero.feature.home.ui.BottomNavigationFragment;
import com.example.tralalero.feature.home.ui.NewBoard;
import com.example.tralalero.domain.model.Project;

import com.example.tralalero.presentation.viewmodel.ProjectViewModel;
import com.example.tralalero.presentation.viewmodel.ViewModelFactoryProvider;
import com.example.tralalero.presentation.viewmodel.WorkspaceViewModel;

import java.util.ArrayList;
import java.util.List;

public class WorkspaceActivity extends HomeActivity {

    private static final String TAG = "WorkspaceActivity";

    private WorkspaceViewModel workspaceViewModel;
    private ProjectViewModel projectViewModel;

    private WorkspaceAdapter workspaceAdapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private String workspaceId;
    private String workspaceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.workspace_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        workspaceId = getIntent().getStringExtra("WORKSPACE_ID");
        workspaceName = getIntent().getStringExtra("WORKSPACE_NAME");
        String projectId = getIntent().getStringExtra("PROJECT_ID");
        String projectName = getIntent().getStringExtra("PROJECT_NAME");
        boolean shouldCreateProject = getIntent().getBooleanExtra("CREATE_PROJECT", false);
        String newProjectName = getIntent().getStringExtra("PROJECT_NAME");
        String newProjectDescription = getIntent().getStringExtra("PROJECT_DESCRIPTION");

        if (workspaceId == null || workspaceId.isEmpty()) {
            Toast.makeText(this, "Error: No workspace ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "=== WorkspaceActivity Started ===");
        Log.d(TAG, "Workspace ID: " + workspaceId);
        Log.d(TAG, "Workspace Name: " + workspaceName);
        Log.d(TAG, "Project ID: " + projectId);
        Log.d(TAG, "Should Create Project: " + shouldCreateProject);
        
        // Check if we should open a specific project directly
        if (projectId != null && !projectId.isEmpty() && !shouldCreateProject) {
            Log.d(TAG, "Opening project directly: " + projectName);
            openProjectActivity(projectId, projectName != null ? projectName : "Project");
            return; // Don't show workspace list
        }
        
        setupViewModels();
        setupUI();
        setupRecyclerView();
        observeViewModels(); // ✅ Observe once
        
        // ✅ Trigger initial load once - selectWorkspace auto-loads projects
        workspaceViewModel.selectWorkspace(workspaceId);
        
        // Check if we should create a new project automatically
        if (shouldCreateProject && newProjectName != null && !newProjectName.isEmpty()) {
            Log.d(TAG, "Auto-creating project: " + newProjectName);
            createProject(newProjectName, newProjectDescription != null ? newProjectDescription : "");
        }
        
        LinearLayout btnCreateBoard = findViewById(R.id.btn_create_board);
        btnCreateBoard.setOnClickListener(v -> showCreateProjectDialog());
        setupBackButton();
        setupBottomNavigation(0);
    }

    private void setupUI() {
        progressBar = findViewById(R.id.progressBar);
        if (progressBar == null) {
            Log.w(TAG, "ProgressBar not found in layout, creating programmatically");
        }
        
        // Set workspace name as title
        TextView tvTitle = findViewById(R.id.tvTitle);
        if (tvTitle != null && workspaceName != null && !workspaceName.isEmpty()) {
            tvTitle.setText(workspaceName);
            Log.d(TAG, "Title set to: " + workspaceName);
        } else {
            Log.w(TAG, "Could not set title - tvTitle or workspaceName is null");
        }
    }

    private void setupViewModels() {
        workspaceViewModel = new ViewModelProvider(this,
                ViewModelFactoryProvider.provideWorkspaceViewModelFactory()
        ).get(WorkspaceViewModel.class);
        projectViewModel = new ViewModelProvider(this,
                ViewModelFactoryProvider.provideProjectViewModelFactory()
        ).get(ProjectViewModel.class);

        Log.d(TAG, "ViewModels setup complete");
    }

    private void observeViewModels() {
        // ✅ Projects observer - auto-updates on any change
        workspaceViewModel.getProjects().observe(this, projects -> {
            if (projects != null) {
                Log.d(TAG, "Projects loaded from ViewModel: " + projects.size());
                workspaceAdapter.setProjectList(projects);

                if (projects.isEmpty()) {
                    Toast.makeText(this, "No projects yet. Create one!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d(TAG, "No projects loaded or list is null");
                workspaceAdapter.setProjectList(new ArrayList<>());
            }
        });
        
        // ✅ Loading observer
        workspaceViewModel.isLoading().observe(this, isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            Log.d(TAG, "Loading state: " + isLoading);
        });
        
        // ✅ Error observer
        workspaceViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error from ViewModel: " + error);
                workspaceViewModel.clearError();
            }
        });
        
        // ✅ Project ViewModel observers (kept for compatibility)
        projectViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Project Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Project error: " + error);
                projectViewModel.clearError();
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerActivity);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        workspaceAdapter = new WorkspaceAdapter(this);
        workspaceAdapter.setOnProjectClickListener(project -> {
            navigateToProject(project);
        });

        recyclerView.setAdapter(workspaceAdapter);
        Log.d(TAG, "RecyclerView setup complete");
    }

    // ❌ DELETE - No longer needed, ViewModel auto-updates
    // private void loadProjects() { ... }

    private void showCreateProjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Project");
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_project, null);
        EditText etProjectName = dialogView.findViewById(R.id.etProjectName);
        EditText etProjectDescription = dialogView.findViewById(R.id.etProjectDescription);

        builder.setView(dialogView);

        builder.setPositiveButton("Create", null); // Set null first
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String projectName = etProjectName.getText().toString().trim();
                String projectDescription = etProjectDescription.getText().toString().trim();
                if (projectName.isEmpty()) {
                    etProjectName.setError("Project name cannot be empty");
                    Toast.makeText(this, "Project name cannot be empty", Toast.LENGTH_SHORT).show();
                    return; // Don't close dialog
                }
                createProject(projectName, projectDescription);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void createProject(String projectName, String projectDescription) {
        Log.d(TAG, "Creating project: " + projectName);
        
        // ✅ Use WorkspaceViewModel's optimistic update method
        workspaceViewModel.createProject(workspaceId, projectName, projectDescription);
        
        // Observer auto-updates UI - NO MANUAL RELOAD
        Toast.makeText(this, "Creating project...", Toast.LENGTH_SHORT).show();
    }

    private void setupBackButton() {
        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                finish();
            });
        }
    }

    private void navigateToProject(Project project) {
        String projectId = project.getId();
        String projectName = project.getName();

        Log.d(TAG, "Navigating to project: " + projectName + " (ID: " + projectId + ")");

        Intent intent = new Intent(WorkspaceActivity.this, ProjectActivity.class);
        intent.putExtra("project_id", projectId);
        intent.putExtra("project_name", projectName);
        intent.putExtra("workspace_id", workspaceId);
        startActivity(intent);
    }
    
    /**
     * Open ProjectActivity directly (when coming from HomeActivity with specific project)
     */
    private void openProjectActivity(String projectId, String projectName) {
        Log.d(TAG, "Opening ProjectActivity directly: " + projectName);
        
        Intent intent = new Intent(this, ProjectActivity.class);
        intent.putExtra("project_id", projectId);
        intent.putExtra("project_name", projectName);
        intent.putExtra("workspace_id", workspaceId);
        startActivity(intent);
        finish(); // Close WorkspaceActivity since we're going directly to project
    }

    // ❌ DELETE onActivityResult - LiveData auto-updates, no manual reload needed
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // DO NOTHING - LiveData observer auto-updates UI
    }
}
