package com.example.tralalero.feature.home.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tralalero.App.App;
import com.example.tralalero.R;
import com.example.tralalero.adapter.HomeAdapter;
import com.example.tralalero.data.repository.WorkspaceRepositoryImplWithCache;
import com.example.tralalero.domain.model.Workspace;
import com.example.tralalero.feature.home.ui.Home.WorkspaceActivity;
import com.example.tralalero.presentation.viewmodel.ViewModelFactoryProvider;
import com.example.tralalero.presentation.viewmodel.WorkspaceViewModel;

import java.util.List;

/**
 * Activity to display and manage all workspaces
 */
public class WorkspaceListActivity extends AppCompatActivity {
    private static final String TAG = "WorkspaceListActivity";
    
    private RecyclerView recyclerWorkspaces;
    private HomeAdapter workspaceAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private WorkspaceViewModel workspaceViewModel;
    
    // Broadcast receiver for workspace updates
    private BroadcastReceiver workspaceUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received WORKSPACE_UPDATED broadcast");
            forceRefreshWorkspaces();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace_list);
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Workspaces");
        }
        
        setupViewModel();
        setupRecyclerView();
        setupSwipeRefresh();
        loadWorkspacesWithCache();
        
        // Register broadcast receiver for workspace updates
        androidx.localbroadcastmanager.content.LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(workspaceUpdateReceiver, new IntentFilter("com.example.tralalero.WORKSPACE_UPDATED"));
    }
    
    private void setupViewModel() {
        workspaceViewModel = new ViewModelProvider(
                this,
                ViewModelFactoryProvider.provideWorkspaceViewModelFactory()
        ).get(WorkspaceViewModel.class);
        
        observeWorkspaceViewModel();
    }
    
    private void observeWorkspaceViewModel() {
        workspaceViewModel.getWorkspaces().observe(this, workspaces -> {
            if (workspaces != null && !workspaces.isEmpty()) {
                Log.d(TAG, "Loaded " + workspaces.size() + " workspaces from ViewModel");
                workspaceAdapter.setWorkspaceList(workspaces);
            } else {
                Log.d(TAG, "No workspaces found");
            }
        });
        
        workspaceViewModel.isLoading().observe(this, isLoading -> {
            if (isLoading) {
                Log.d(TAG, "Loading workspaces...");
            } else {
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
    
    private void setupRecyclerView() {
        recyclerWorkspaces = findViewById(R.id.recyclerWorkspaces);
        recyclerWorkspaces.setLayoutManager(new LinearLayoutManager(this));
        workspaceAdapter = new HomeAdapter(this);
        workspaceAdapter.setOnWorkspaceClickListener(workspace -> {
            String workspaceId = workspace.getId();
            String workspaceName = workspace.getName();
            Log.d(TAG, "Clicked workspace: " + workspaceName + " (ID: " + workspaceId + ")");
            Intent intent = new Intent(WorkspaceListActivity.this, WorkspaceActivity.class);
            intent.putExtra("WORKSPACE_ID", workspaceId);
            intent.putExtra("WORKSPACE_NAME", workspaceName);
            startActivity(intent);
        });
        recyclerWorkspaces.setAdapter(workspaceAdapter);
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
                            workspaceAdapter.setWorkspaceList(workspaces);
                            String message;
                            if (duration < 100) {
                                message = "⚡ Cache: " + duration + "ms (" + workspaces.size() + " workspaces)";
                                Log.i(TAG, "CACHE HIT: " + duration + "ms");
                            } else {
                                message = "🌐 API: " + duration + "ms (" + workspaces.size() + " workspaces)";
                                Log.i(TAG, "API CALL: " + duration + "ms");
                            }
                            Toast.makeText(WorkspaceListActivity.this, message, Toast.LENGTH_SHORT).show();
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

                        Toast.makeText(WorkspaceListActivity.this,
                            "Error loading from cache, trying API...", 
                            Toast.LENGTH_SHORT).show();
                        workspaceViewModel.loadWorkspaces();
                    });
                }
            });
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister broadcast receiver
        androidx.localbroadcastmanager.content.LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(workspaceUpdateReceiver);
    }
}
