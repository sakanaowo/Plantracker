package com.example.tralalero.feature.home.ui;

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
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.App.App;
import com.example.tralalero.MainActivity;
import com.example.tralalero.R;
import com.example.tralalero.adapter.WorkspaceAdapter;
import com.example.tralalero.network.api.WorkspaceApiService;
import com.example.tralalero.model.Workspace.Workspace;
import com.example.tralalero.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkspaceActivity extends ActivityActivity{

    private WorkspaceAdapter workspaceAdapter;
    private RecyclerView recyclerView;

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

        setupRecyclerView();
        loadWorkspaces();


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
        workspaceAdapter = new WorkspaceAdapter(this);

        // Set up the RecyclerView with GridLayoutManager
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(workspaceAdapter);

        // Set click listener for workspace items
        workspaceAdapter.setOnWorkspaceClickListener(workspace -> {
            // Handle workspace click - you can navigate to workspace details
            Toast.makeText(this, "Clicked on workspace: " + workspace.getName(), Toast.LENGTH_SHORT).show();
            Log.d("HomeActivity", "Workspace clicked: " + workspace.getName() + " (ID: " + workspace.getId() + ")");
        });
    }

    private void loadWorkspaces() {
        WorkspaceApiService apiService = ApiClient.get(App.authManager).create(WorkspaceApiService.class);
        Call<List<Workspace>> call = apiService.getWorkspaces();

        call.enqueue(new Callback<List<Workspace>>() {
            @Override
            public void onResponse(Call<List<Workspace>> call, Response<List<Workspace>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Workspace> workspaces = response.body();
                    workspaceAdapter.setWorkspaceList(workspaces);
                    Log.d("WorkspaceActivity", "Loaded " + workspaces.size() + " workspaces");
                } else {
                    Log.e("WorkspaceActivity", "Failed to load workspaces: " + response.code());
                    Toast.makeText(WorkspaceActivity.this, "Failed to load workspaces", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Workspace>> call, Throwable t) {
                Log.e("HomeActivity", "Error loading workspaces", t);
                Toast.makeText(WorkspaceActivity.this, "Error loading workspaces: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            String workspaceName = data.getStringExtra("workspace_name");
            String visibility = data.getStringExtra("visibility");
            String background = data.getStringExtra("background");

            // Tạo object mới
            Workspace newWorkspace = new Workspace("",workspaceName,"","");

            // Thêm vào list của adapter
            workspaceAdapter.addWorkspace(newWorkspace);


            // Scroll xuống cuối

        }
    }


}

