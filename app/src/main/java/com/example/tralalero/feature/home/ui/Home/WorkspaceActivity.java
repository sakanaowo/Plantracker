package com.example.tralalero.feature.home.ui.Home;

import static android.content.ContentValues.TAG;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
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
import com.example.tralalero.model.Project;
import com.example.tralalero.presentation.viewmodel.ProjectViewModel;
import com.example.tralalero.presentation.viewmodel.ViewModelFactoryProvider;
import com.example.tralalero.presentation.viewmodel.WorkspaceViewModel;

import java.util.ArrayList;
import java.util.List;

public class WorkspaceActivity extends HomeActivity {

    private WorkspaceViewModel workspaceViewModel;
    private ProjectViewModel projectViewModel;
    
    private WorkspaceAdapter workspaceAdapter;
    private RecyclerView recyclerView;
    private String workspaceId;

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

        // Setup ViewModels FIRST
        setupViewModels();
        
        // Then setup UI
        setupRecyclerView();
        
        // Observe ViewModels
        observeViewModels();
        
        // Finally load data
        loadProjects();


        LinearLayout btnCreateBoard = findViewById(R.id.btn_create_board);
        btnCreateBoard.setOnClickListener(v -> {
            Intent intent = new Intent(WorkspaceActivity.this, NewBoard.class);
            startActivityForResult(intent, 100); // 100 là requestCode
        });


        BottomNavigationFragment bottomNav = (BottomNavigationFragment)
                getSupportFragmentManager().findFragmentById(R.id.bottomNavigation);

        setupBottomNavigation(0);

    }
    
    /**
     * Setup ViewModels with dependency injection
     * Phase 5: Using ViewModelFactoryProvider for cleaner code
     */
    private void setupViewModels() {
        // Setup WorkspaceViewModel
        workspaceViewModel = new ViewModelProvider(this,
                ViewModelFactoryProvider.provideWorkspaceViewModelFactory()
        ).get(WorkspaceViewModel.class);
        
        // Setup ProjectViewModel for project operations
        projectViewModel = new ViewModelProvider(this,
                ViewModelFactoryProvider.provideProjectViewModelFactory()
        ).get(ProjectViewModel.class);
    }
    
    /**
     * Observe ViewModel LiveData and update UI accordingly
     * Phase 5: Replace direct API calls with ViewModel observations
     */
    private void observeViewModels() {
        // Observe projects from workspace
        workspaceViewModel.getProjects().observe(this, projects -> {
            if (projects != null && !projects.isEmpty()) {
                Log.d(TAG, "Projects loaded from ViewModel: " + projects.size());
                
                // Convert domain Project to old model Project temporarily
                // TODO: Phase 6 - Migrate adapter to use domain models directly
                List<Project> oldProjects = convertDomainProjectsToOldModel(projects);
                workspaceAdapter.setProjectList(oldProjects);
            } else {
                Log.d(TAG, "No projects loaded or list is empty");
                workspaceAdapter.setProjectList(new ArrayList<>());
            }
        });
        
        // Observe loading state
        workspaceViewModel.isLoading().observe(this, isLoading -> {
            // TODO: Add ProgressBar to layout if needed in future
            Log.d(TAG, "Loading state: " + isLoading);
        });
        
        // Observe errors
        workspaceViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error from ViewModel: " + error);
                workspaceViewModel.clearError();
            }
        });
        
        // Observe project operations from ProjectViewModel
        projectViewModel.getSelectedProject().observe(this, project -> {
            if (project != null) {
                Log.d(TAG, "Project operation completed: " + project.getName());
                Toast.makeText(this, "Project saved successfully", Toast.LENGTH_SHORT).show();
                // Reload projects list
                loadProjects();
            }
        });
        
        projectViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Project error: " + error);
                projectViewModel.clearError();
            }
        });
        
        // Observe project deleted
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
            // Khi click vào project, navigate to ProjectActivity
            String projectId = project.getId();
            String projectName = project.getName();

            Log.d(TAG, "Clicked project: " + projectName + " (ID: " + projectId + ")");

            Intent intent = new Intent(WorkspaceActivity.this, ProjectActivity.class);
            intent.putExtra("project_id", projectId);
            intent.putExtra("project_name", projectName);
            intent.putExtra("workspace_id", workspaceId);
            startActivity(intent);
        });

        recyclerView.setAdapter(workspaceAdapter);
    }

    /**
     * Load projects using ViewModel instead of direct API call
     * Phase 5: Refactored from direct Retrofit call to ViewModel
     */
    private void loadProjects() {
        Log.d(TAG, "Loading projects for workspace: " + workspaceId);
        
        if (workspaceId == null || workspaceId.isEmpty()) {
            Toast.makeText(this, "Workspace ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // BEFORE (Phase 4): Direct API call
        // WorkspaceApiService apiService = ApiClient.get(App.authManager).create(WorkspaceApiService.class);
        // Call<List<ProjectDTO>> call = apiService.getProjects(workspaceId);
        
        // AFTER (Phase 5): Use ViewModel
        workspaceViewModel.loadWorkspaceProjects(workspaceId);
    }
    
    /**
     * Convert domain model Projects to old model Projects
     * TODO: Phase 6 - Remove this when adapter is migrated to use domain models
     */
    private List<Project> convertDomainProjectsToOldModel(List<com.example.tralalero.domain.model.Project> domainProjects) {
        List<Project> oldProjects = new ArrayList<>();
        for (com.example.tralalero.domain.model.Project domainProject : domainProjects) {
            Project oldProject = new Project(
                domainProject.getId(),
                domainProject.getName(),
                domainProject.getDescription(),
                domainProject.getKey()
            );
            oldProjects.add(oldProject);
        }
        return oldProjects;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            String projectName = data.getStringExtra("workspace_name");
            String visibility = data.getStringExtra("visibility");
            String background = data.getStringExtra("background");

            // Tạo object mới
            Project newProject = new Project("",projectName,"","");

            // Thêm vào list của adapter
            workspaceAdapter.addProject(newProject);


            // Scroll xuống cuối

        }
    }


}
