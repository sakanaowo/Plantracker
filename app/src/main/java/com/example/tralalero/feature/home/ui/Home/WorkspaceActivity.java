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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.App.App;
import com.example.tralalero.MainActivity;
import com.example.tralalero.R;
import com.example.tralalero.adapter.HomeAdapter;
import com.example.tralalero.adapter.WorkspaceAdapter;
import com.example.tralalero.feature.home.ui.AccountActivity;
import com.example.tralalero.feature.home.ui.ActivityActivity;
import com.example.tralalero.feature.home.ui.InboxActivity;
import com.example.tralalero.feature.home.ui.NewBoard;
import com.example.tralalero.model.Project;
import com.example.tralalero.model.Workspace;
import com.example.tralalero.network.api.WorkspaceApiService;
import com.example.tralalero.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkspaceActivity extends ActivityActivity {

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


        ImageButton btnBoard = findViewById(R.id.btn1);
        btnBoard.setOnClickListener(v -> {
            Intent intent = new Intent(WorkspaceActivity.this, MainActivity.class);
            startActivity(intent);
        });

        ImageButton btnInbox = findViewById(R.id.btn2);
        btnInbox.setOnClickListener(v -> {
            Intent intent = new Intent(WorkspaceActivity.this, InboxActivity.class);
            startActivity(intent);
        });

        ImageButton btnActivity = findViewById(R.id.btn3);
        btnActivity.setOnClickListener(v -> {
            Intent intent = new Intent(WorkspaceActivity.this, ActivityActivity.class);
            startActivity(intent);
        });

        ImageButton btnAccount = findViewById(R.id.btn4);

        btnAccount.setOnClickListener(v -> {
            Intent intent = new Intent(WorkspaceActivity.this, AccountActivity.class);
            startActivity(intent);
        });

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
            intent.putExtra("PROJECT_ID", projectId);
            intent.putExtra("PROJECT_NAME", projectName);
            startActivity(intent);
        });

        recyclerView.setAdapter(workspaceAdapter);
    }

    private void loadProjects() {
        WorkspaceApiService apiService = ApiClient.get(App.authManager).create(WorkspaceApiService.class);
        Call<List<Project>> call = apiService.getProjects(workspaceId);

            call.enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Project> projects = response.body();
                    workspaceAdapter.setProjectList(projects);
                    Log.d("WorkspaceActivity", "Loaded " + projects.size() + " projects");
                    for (Project project : projects) {
                        Log.d(TAG, "Project: " + project.getName() + " (ID: " + project.getId() + ")");
                    }
                } else {
                    Log.e("WorkspaceActivity", "Failed to load projects: " + response.code());
                    Toast.makeText(WorkspaceActivity.this, "Failed to load workspaces", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
                public void onFailure(Call<List<Project>> call, Throwable t) {
                Log.e(TAG, "Error loading workspaces", t);
                Toast.makeText(WorkspaceActivity.this, "Error loading workspaces: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
