package com.example.tralalero.feature.home.ui.Home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.App.App;
import com.example.tralalero.R;
import com.example.tralalero.adapter.HomeAdapter;
import com.example.tralalero.feature.home.ui.AccountActivity;
import com.example.tralalero.feature.home.ui.ActivityActivity;
import com.example.tralalero.feature.home.ui.InboxActivity;
import com.example.tralalero.model.Workspace;
import com.example.tralalero.network.ApiClient;
import com.example.tralalero.network.api.HomeApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerBoard;
    private HomeAdapter homeAdapter;
    private static final String TAG = "HomeActivity";

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

        // Initialize RecyclerView
        setupRecyclerView();

        // Load workspaces từ API
        loadWorkspacesFromApi();

        EditText cardNew = findViewById(R.id.cardNew);
        LinearLayout inboxForm = findViewById(R.id.inboxForm);

        cardNew.setOnClickListener(v -> inboxForm.setVisibility(View.VISIBLE));

        Button btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> {
                    inboxForm.setVisibility(View.GONE);
                    cardNew.setText("");
                }
        );

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
        recyclerBoard = findViewById(R.id.recyclerBoard);
        recyclerBoard.setLayoutManager(new LinearLayoutManager(this));

        homeAdapter = new HomeAdapter(this);
        homeAdapter.setOnWorkspaceClickListener(workspace -> {
            // Khi click vào workspace, lấy ID và chuyển sang WorkspaceActivity
            String workspaceId = workspace.getId();
            String workspaceName = workspace.getName();

            Log.d(TAG, "Clicked workspace: " + workspaceName + " (ID: " + workspaceId + ")");

            Intent intent = new Intent(HomeActivity.this, WorkspaceActivity.class);
            intent.putExtra("WORKSPACE_ID", workspaceId);
            intent.putExtra("WORKSPACE_NAME", workspaceName);
            startActivity(intent);
        });

        recyclerBoard.setAdapter(homeAdapter);
    }

    private void loadWorkspacesFromApi() {
        // Tạo API service
        HomeApiService apiService = ApiClient.get(App.authManager).create(HomeApiService.class);

        // Gọi API
        Call<List<Workspace>> call = apiService.getWorkspaces();

        call.enqueue(new Callback<List<Workspace>>() {
            @Override
            public void onResponse(Call<List<Workspace>> call, Response<List<Workspace>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Workspace> workspaces = response.body();

                    Log.d(TAG, "Loaded " + workspaces.size() + " workspaces from API");

                    // Cập nhật adapter với dữ liệu từ API
                    homeAdapter.setWorkspaceList(workspaces);

                    // Log để debug
                    for (Workspace workspace : workspaces) {
                        Log.d(TAG, "Workspace: " + workspace.getName() + " (ID: " + workspace.getId() + ")");
                    }
                } else {
                    Log.e(TAG, "Failed to load workspaces: " + response.code());
                    Toast.makeText(HomeActivity.this,
                            "Failed to load workspaces",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Workspace>> call, Throwable t) {
                Log.e(TAG, "Error loading workspaces", t);
                Toast.makeText(HomeActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}