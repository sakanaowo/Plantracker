package com.example.tralalero.feature.home.ui.Home;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.App.App;
import com.example.tralalero.MainActivity;
import com.example.tralalero.R;
import com.example.tralalero.adapter.WorkspaceAdapter;
import com.example.tralalero.feature.home.ui.AccountActivity;
import com.example.tralalero.feature.home.ui.ActivityActivity;
import com.example.tralalero.feature.home.ui.BottomNavigationFragment;
import com.example.tralalero.feature.home.ui.InboxActivity;
import com.example.tralalero.feature.home.ui.NewBoard;
import com.example.tralalero.domain.model.Project;
import com.example.tralalero.data.remote.dto.project.ProjectDTO;
import com.example.tralalero.data.remote.api.WorkspaceApiService;
import com.example.tralalero.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkspaceActivity extends HomeActivity {

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

        setupRecyclerView();
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

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerActivity);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        workspaceAdapter = new WorkspaceAdapter(this);
        workspaceAdapter.setOnProjectClickListener(project -> {
            // Khi click vào workspace, lấy ID và chuyển sang WorkspaceActivity
            String projectId = project.getId();
            String projectName = project.getName();

            Log.d(TAG, "Clicked workspace: " + projectName + " (ID: " + projectId + ")");

            Intent intent = new Intent(WorkspaceActivity.this, ProjectActivity.class);
            intent.putExtra("project_id", projectId);  // Sửa thành chữ thường để match với ProjectActivity
            intent.putExtra("project_name", projectName);  // Sửa thành chữ thường
            intent.putExtra("workspace_id", workspaceId);  // Thêm workspace_id
            startActivity(intent);
        });

        recyclerView.setAdapter(workspaceAdapter);
    }

    private void loadProjects() {
        WorkspaceApiService apiService = ApiClient.get(App.authManager).create(WorkspaceApiService.class);
        Call<List<ProjectDTO>> call = apiService.getProjects(workspaceId);

        call.enqueue(new Callback<List<ProjectDTO>>() {
            @Override
            public void onResponse(Call<List<ProjectDTO>> call, Response<List<ProjectDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ProjectDTO> projectDTOs = response.body();

                    // TODO: Fix in Phase 4 - Convert DTO to old model temporarily
                    // This is a temporary solution until we migrate to new architecture
                    List<Project> projects = convertDTOsToOldModel(projectDTOs);

                    workspaceAdapter.setProjectList(projects);
                    Log.d("WorkspaceActivity", "Loaded " + projects.size() + " projects");
                    for (Project project : projects) {
                        Log.d(TAG, "Project: " + project.getName() + " (ID: " + project.getId() + ")");
                    }
                } else {
                    Log.e("WorkspaceActivity", "Failed to load projects: " + response.code());
                    Toast.makeText(WorkspaceActivity.this, "Failed to load projects", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ProjectDTO>> call, Throwable t) {
                Log.e(TAG, "Error loading projects", t);
                Toast.makeText(WorkspaceActivity.this, "Error loading projects: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Temporary method to convert ProjectDTO to old Project model
     * TODO: Remove this in Phase 4 when migrating to new architecture
     */
    private List<Project> convertDTOsToOldModel(List<ProjectDTO> dtos) {
        List<Project> projects = new ArrayList<>();
        for (ProjectDTO dto : dtos) {
            Project project = new Project(
                dto.getId(),
                dto.getName(),
                dto.getDescription(),
                dto.getKey(),
                null,
                null
            );
            projects.add(project);
        }
        return projects;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            String projectName = data.getStringExtra("workspace_name");
            String visibility = data.getStringExtra("visibility");
            String background = data.getStringExtra("background");

            // Tạo object mới
            Project newProject = new Project("",projectName,"","",null,null);

            // Thêm vào list của adapter
            workspaceAdapter.addProject(newProject);


            // Scroll xuống cuối

        }
    }


}
