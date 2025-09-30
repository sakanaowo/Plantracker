package com.example.tralalero.feature.home.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.App.App;
import com.example.tralalero.MainActivity;
import com.example.tralalero.feature.home.adapter.WorkspaceAdapter;
import com.example.tralalero.feature.home.api.WorkspaceApiService;
import com.example.tralalero.feature.home.model.Workspace;
import com.example.tralalero.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private WorkspaceAdapter workspaceAdapter;
    private RecyclerView recyclerView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String name = getIntent().getStringExtra("user_name");
        String email = getIntent().getStringExtra("user_email");

//        TextView tvTitle = findViewById(R.id.tv_home_title);
//        if (tvTitle != null) {
//            String who = (name != null && !name.isEmpty()) ? name : (email != null ? email : "");
//            String text = who.isEmpty() ? "Welcome to Plantracker" : "Welcome, " + who;
//            tvTitle.setText(text);
//        }

        setupRecyclerView();
        loadWorkspaces();

        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            String text = cardNew.getText().toString().trim();

            if (!text.isEmpty()) {
                // 👉 gọi hàm lưu vào database
//                TODO: lưu vào database
//                saveToDatabase(text);

                Toast.makeText(this, "Đã thêm: " + text, Toast.LENGTH_SHORT).show();

                inboxForm.setVisibility(View.GONE);
                cardNew.setText(""); // clear sau khi lưu
            } else {
                Toast.makeText(this, "Vui lòng nhập dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayout btnCreateBoard = findViewById(R.id.btn_create_board);
        btnCreateBoard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NewBoard.class);
            startActivity(intent);
        });


        ImageButton btnBoard = findViewById(R.id.btn1);
        btnBoard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
        });

        ImageButton btnInbox = findViewById(R.id.btn2);
        btnInbox.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, InboxActivity.class);
            startActivity(intent);
        });

        ImageButton btnActivity = findViewById(R.id.btn3);
        btnActivity.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ActivityActivity.class);
            startActivity(intent);
        });

        ImageButton btnAccount = findViewById(R.id.btn4);

        btnAccount.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AccountActivity.class);
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
                    Log.d("HomeActivity", "Loaded " + workspaces.size() + " workspaces");
                } else {
                    Log.e("HomeActivity", "Failed to load workspaces: " + response.code());
                    Toast.makeText(HomeActivity.this, "Failed to load workspaces", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Workspace>> call, Throwable t) {
                Log.e("HomeActivity", "Error loading workspaces", t);
                Toast.makeText(HomeActivity.this, "Error loading workspaces: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
//    private void handleAccount() {
//        Intent intent = new Intent(this, AccountActivity.class);
//        startActivity(intent);
//        finish();
//    }

}