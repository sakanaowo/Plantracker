package com.example.tralalero.feature.home.ui.Home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;

import com.example.tralalero.R;
import com.example.tralalero.adapter.HomeAdapter;

import com.example.tralalero.feature.home.ui.BaseActivity;
import com.example.tralalero.presentation.viewmodel.ViewModelFactoryProvider;

import com.example.tralalero.test.RepositoryTestActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.tralalero.presentation.viewmodel.WorkspaceViewModel;

public class HomeActivity extends BaseActivity {

    private RecyclerView recyclerBoard;
    private HomeAdapter homeAdapter;
    private static final String TAG = "HomeActivity";

    private WorkspaceViewModel workspaceViewModel;

    private void setupWorkspaceViewModel() {
        // Sử dụng ViewModelFactoryProvider thay vì tạo thủ công
        workspaceViewModel = new ViewModelProvider(
                this,
                ViewModelFactoryProvider.provideWorkspaceViewModelFactory()
        ).get(WorkspaceViewModel.class);
    }

    private void observeWorkspaceViewModel() {
        workspaceViewModel.getWorkspaces().observe(this, workspaces -> {
            if (workspaces != null && !workspaces.isEmpty()) {
                Log.d(TAG, "Loaded " + workspaces.size() + " workspaces from ViewModel");

                // LOẠI BỎ CONVERT - Dùng domain model trực tiếp
                homeAdapter.setWorkspaceList(workspaces);
            } else {
                Log.d(TAG, "No workspaces found");
            }
        });

        workspaceViewModel.isLoading().observe(this, isLoading -> {
            if (isLoading) {
                // TODO: show loading indicator
                Log.d(TAG, "Loading workspaces...");
            } else {
                // TODO: hide loading indicator
                Log.d(TAG, "Finished loading workspaces.");
            }
        });

        workspaceViewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Error loading workspaces: " + error, Toast.LENGTH_SHORT).show();
                workspaceViewModel.clearError();
            }
        });
    }


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

        // Setup ViewModel TRƯỚC khi setup UI
        setupWorkspaceViewModel();
        observeWorkspaceViewModel();

        // Initialize RecyclerView
        setupRecyclerView();

        // Load workspaces từ ViewModel
        workspaceViewModel.loadWorkspaces();

        // Setup Test Repository Button (Development only)
        setupTestRepositoryButton();

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
                // TODO: lưu vào database
                Toast.makeText(this, "Đã thêm: " + text, Toast.LENGTH_SHORT).show();

                inboxForm.setVisibility(View.GONE);
                cardNew.setText(""); // clear sau khi lưu
            } else {
                Toast.makeText(this, "Vui lòng nhập dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });

        setupBottomNavigation(0);
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


    private void setupTestRepositoryButton() {
        // Tạo FAB button để test repository (chỉ dùng khi development)
        FloatingActionButton fabTest = findViewById(R.id.fabTestRepository);
        if (fabTest != null) {
            fabTest.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, RepositoryTestActivity.class);
                startActivity(intent);
            });
        }
    }
}