package com.example.tralalero.feature.home.ui.Home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tralalero.App.App;
import com.example.tralalero.R;
import com.example.tralalero.adapter.HomeAdapter;
import com.example.tralalero.data.repository.WorkspaceRepositoryImplWithCache;
import com.example.tralalero.domain.model.Workspace;
import com.example.tralalero.domain.repository.IWorkspaceRepository;
import com.example.tralalero.feature.home.ui.BaseActivity;
import com.example.tralalero.presentation.viewmodel.ViewModelFactoryProvider;
import com.example.tralalero.test.RepositoryTestActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.tralalero.presentation.viewmodel.WorkspaceViewModel;
import com.example.tralalero.presentation.viewmodel.AuthViewModel;

import java.util.List;

public class HomeActivity extends BaseActivity {
    private RecyclerView recyclerBoard;
    private HomeAdapter homeAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;  // ← ADDED
    private static final String TAG = "HomeActivity";
    private WorkspaceViewModel workspaceViewModel;
    private AuthViewModel authViewModel;
    private void setupViewModels() {
        workspaceViewModel = new ViewModelProvider(
                this,
                ViewModelFactoryProvider.provideWorkspaceViewModelFactory()
        ).get(WorkspaceViewModel.class);
        authViewModel = new ViewModelProvider(
                this,
                ViewModelFactoryProvider.provideAuthViewModelFactory()
        ).get(AuthViewModel.class);
    }
    private void observeWorkspaceViewModel() {
        workspaceViewModel.getWorkspaces().observe(this, workspaces -> {
            if (workspaces != null && !workspaces.isEmpty()) {
                Log.d(TAG, "Loaded " + workspaces.size() + " workspaces from ViewModel");
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

    private void loadWorkspacesWithCache() {
        Log.d(TAG, "Loading workspaces with cache...");
        final long startTime = System.currentTimeMillis();

        App.dependencyProvider.getWorkspaceRepositoryWithCache()
            .getWorkspaces(new WorkspaceRepositoryImplWithCache.WorkspaceCallback() {
                @Override
                public void onSuccess(List<Workspace> workspaces) {
                    long duration = System.currentTimeMillis() - startTime;

                    runOnUiThread(() -> {
                        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }

                        if (workspaces != null && !workspaces.isEmpty()) {
                            homeAdapter.setWorkspaceList(workspaces);
                            String message;
                            if (duration < 100) {
                                message = "⚡ Cache: " + duration + "ms (" + workspaces.size() + " workspaces)";
                                Log.i(TAG, "CACHE HIT: " + duration + "ms");
                            } else {
                                message = "🌐 API: " + duration + "ms (" + workspaces.size() + " workspaces)";
                                Log.i(TAG, "API CALL: " + duration + "ms");
                            }
                            Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "No workspaces found");
                        }
                    });
                }

                @Override
                public void onCacheEmpty() {
                    Log.d(TAG, "Cache empty, falling back to API...");
                    runOnUiThread(() -> {
                        workspaceViewModel.loadWorkspaces();
                    });
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Cache error: " + e.getMessage());
                    runOnUiThread(() -> {
                        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }

                        Toast.makeText(HomeActivity.this,
                            "Error loading from cache, trying API...", 
                            Toast.LENGTH_SHORT).show();
                        workspaceViewModel.loadWorkspaces();
                    });
                }
            });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent
            );
            swipeRefreshLayout.setOnRefreshListener(() -> {
                Log.d(TAG, "User triggered pull-to-refresh");
                forceRefreshWorkspaces();
            });
        }
    }

    private void forceRefreshWorkspaces() {
        Log.d(TAG, "Force refreshing workspaces from API...");
        App.dependencyProvider.clearWorkspaceCache();
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
        loadWorkspacesWithCache();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
                Log.d(TAG, "Refresh timeout");
            }
        }, 5000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        View titleView = findViewById(R.id.tvTitle);
        View bottomNav = findViewById(R.id.bottomNavigation);

        ViewCompat.setOnApplyWindowInsetsListener(titleView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return WindowInsetsCompat.CONSUMED;
        });

        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        setupViewModels();
        observeWorkspaceViewModel();
        setupRecyclerView();
        setupSwipeRefresh();
        loadWorkspacesWithCache();  // Person 1: Cache-first approach

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
        FloatingActionButton fabTest = findViewById(R.id.fabTestRepository);
        if (fabTest != null) {
            fabTest.setVisibility(View.GONE);

            fabTest.setVisibility(View.GONE);
        }
    }
}