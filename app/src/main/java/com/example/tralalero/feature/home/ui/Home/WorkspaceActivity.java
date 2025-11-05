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

        if (workspaceId == null || workspaceId.isEmpty()) {
            Toast.makeText(this, "Error: No workspace ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "=== WorkspaceActivity Started ===");
        Log.d(TAG, "Workspace ID: " + workspaceId);
        Log.d(TAG, "Workspace Name: " + workspaceName);
        setupViewModels();
        setupUI();
        setupRecyclerView();
        observeViewModels();
        loadProjects();
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
        workspaceViewModel.isLoading().observe(this, isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            Log.d(TAG, "Loading state: " + isLoading);
        });
        workspaceViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error from ViewModel: " + error);
                workspaceViewModel.clearError();
            }
        });
        projectViewModel.isProjectCreated().observe(this, created -> {
            if (created != null && created) {
                Log.d(TAG, "Project created successfully");
                Toast.makeText(this, "Project created successfully!", Toast.LENGTH_SHORT).show();
                loadProjects();
            }
        });
        projectViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Project Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Project error: " + error);
                projectViewModel.clearError();
            }
        });
        projectViewModel.isProjectDeleted().observe(this, deleted -> {
            if (deleted != null && deleted) {
                Toast.makeText(this, "Project deleted successfully", Toast.LENGTH_SHORT).show();
                loadProjects(); // Reload list
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

    private void loadProjects() {
        Log.d(TAG, "Loading projects for workspace: " + workspaceId);

        if (workspaceId == null || workspaceId.isEmpty()) {
            Toast.makeText(this, "Workspace ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        workspaceViewModel.loadWorkspaceProjects(workspaceId);
    }

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
        String projectKey = projectName.length() >= 3
            ? projectName.substring(0, 3).toUpperCase()
            : projectName.toUpperCase();
        Project newProject = new Project(
            "",              // id will be generated by backend
            workspaceId,     // workspaceId
            projectName,     // name
            projectDescription, // description
            projectKey,      // key
            "KANBAN"         // boardType
        );

        projectViewModel.createProject(workspaceId, newProject);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            loadProjects();
        }
    }
}
