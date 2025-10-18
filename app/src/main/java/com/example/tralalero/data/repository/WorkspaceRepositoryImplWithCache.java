package com.example.tralalero.data.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.tralalero.data.local.database.dao.WorkspaceDao;
import com.example.tralalero.data.local.database.entity.WorkspaceEntity;
import com.example.tralalero.data.mapper.WorkspaceEntityMapper;
import com.example.tralalero.data.remote.api.WorkspaceApiService;
import com.example.tralalero.data.remote.dto.workspace.WorkspaceDTO;
import com.example.tralalero.data.remote.mapper.WorkspaceMapper;
import com.example.tralalero.domain.model.Workspace;

import java.util.List;
import java.util.concurrent.ExecutorService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Workspace Repository with Room Database caching
 * Pattern: Cache-first with silent background refresh
 *
 * NOTE: This does NOT implement IWorkspaceRepository because we need
 * a different callback pattern with onCacheEmpty() support
 *
 * @author Person 1
 * @date October 18, 2025
 */
public class WorkspaceRepositoryImplWithCache {
    private static final String TAG = "WorkspaceRepoCache";

    private final WorkspaceApiService apiService;
    private final WorkspaceDao workspaceDao;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public WorkspaceRepositoryImplWithCache(
            WorkspaceApiService apiService,
            WorkspaceDao workspaceDao,
            ExecutorService executorService) {
        this.apiService = apiService;
        this.workspaceDao = workspaceDao;
        this.executorService = executorService;
        this.mainHandler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "WorkspaceRepositoryImplWithCache initialized");
    }

    // ==================== GET ALL WORKSPACES ====================

    /**
     * Get all workspaces with cache-first approach
     *
     * @param callback WorkspaceCallback with onSuccess, onCacheEmpty, onError
     */
    public void getWorkspaces(WorkspaceCallback callback) {
        if (callback == null) {
            Log.e(TAG, "Callback is null");
            return;
        }

        executorService.execute(() -> {
            try {
                // 1. Return from cache immediately (30ms)
                List<WorkspaceEntity> cached = workspaceDao.getAll();
                if (cached != null && !cached.isEmpty()) {
                    List<Workspace> cachedWorkspaces = WorkspaceEntityMapper.toDomainList(cached);
                    mainHandler.post(() -> callback.onSuccess(cachedWorkspaces));
                    Log.d(TAG, "✓ Returned " + cached.size() + " workspaces from cache");
                } else {
                    // Cache is empty - notify caller
                    mainHandler.post(() -> callback.onCacheEmpty());
                    Log.d(TAG, "Cache empty - will fetch from API");
                }

                // 2. Fetch from network in background (500-1000ms)
                fetchWorkspacesFromNetwork(callback, cached == null || cached.isEmpty());
            } catch (Exception e) {
                Log.e(TAG, "Error in getWorkspaces", e);
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    private void fetchWorkspacesFromNetwork(
            WorkspaceCallback callback,
            boolean isFirstLoad) {

        apiService.getWorkspaces().enqueue(new Callback<List<WorkspaceDTO>>() {
            @Override
            public void onResponse(Call<List<WorkspaceDTO>> call, Response<List<WorkspaceDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        List<Workspace> workspaces = WorkspaceMapper.toDomainList(response.body());

                        // Cache in background
                        executorService.execute(() -> {
                            try {
                                List<WorkspaceEntity> entities =
                                    WorkspaceEntityMapper.toEntityList(workspaces);
                                workspaceDao.insertAll(entities);
                                Log.d(TAG, "✓ Cached " + workspaces.size() + " workspaces from network");
                            } catch (Exception e) {
                                Log.e(TAG, "Error caching workspaces", e);
                            }
                        });

                        // Only callback if first load (no cache)
                        if (isFirstLoad && callback != null) {
                            mainHandler.post(() -> callback.onSuccess(workspaces));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing workspaces", e);
                        if (isFirstLoad && callback != null) {
                            mainHandler.post(() -> callback.onError(e));
                        }
                    }
                } else if (isFirstLoad && callback != null) {
                    Exception error = new Exception("Failed to load workspaces: " + response.code());
                    mainHandler.post(() -> callback.onError(error));
                }
            }

            @Override
            public void onFailure(Call<List<WorkspaceDTO>> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                if (isFirstLoad && callback != null) {
                    mainHandler.post(() -> callback.onError(new Exception(t)));
                }
            }
        });
    }

    /**
     * Save workspaces to cache manually (if needed)
     */
    public void saveWorkspacesToCache(List<Workspace> workspaces) {
        executorService.execute(() -> {
            try {
                List<WorkspaceEntity> entities = WorkspaceEntityMapper.toEntityList(workspaces);
                workspaceDao.insertAll(entities);
                Log.d(TAG, "✓ Saved " + workspaces.size() + " workspaces to cache");
            } catch (Exception e) {
                Log.e(TAG, "Error saving workspaces to cache", e);
            }
        });
    }

    /**
     * Clear workspace cache
     */
    public void clearCache() {
        executorService.execute(() -> {
            try {
                workspaceDao.deleteAll();
                Log.d(TAG, "✓ Workspace cache cleared");
            } catch (Exception e) {
                Log.e(TAG, "Error clearing workspace cache", e);
            }
        });
    }

    // ==================== CALLBACK INTERFACE ====================

    /**
     * Callback interface for workspace operations
     * Similar to TaskRepositoryImplWithCache.TaskCallback
     */
    public interface WorkspaceCallback {
        /**
         * Called when workspaces are loaded successfully (from cache or network)
         */
        void onSuccess(List<Workspace> workspaces);

        /**
         * Called when cache is empty (first load)
         * UI should show loading indicator
         */
        void onCacheEmpty();

        /**
         * Called when an error occurs
         */
        void onError(Exception e);
    }
}

