package com.example.tralalero.data.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.tralalero.data.local.database.dao.CacheMetadataDao;
import com.example.tralalero.data.local.database.dao.WorkspaceDao;
import com.example.tralalero.data.local.database.entity.CacheMetadata;
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
 * Pattern: Cache-first with silent background refresh + TTL support
 *
 * @author Person 1
 * @date October 18, 2025
 * @updated October 19, 2025 - Added TTL support
 */
public class WorkspaceRepositoryImplWithCache {
    private static final String TAG = "WorkspaceRepoCache";
    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes
    private static final String CACHE_KEY = "workspaces";

    private final WorkspaceApiService apiService;
    private final WorkspaceDao workspaceDao;
    private final CacheMetadataDao cacheMetadataDao;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public WorkspaceRepositoryImplWithCache(
            WorkspaceApiService apiService,
            WorkspaceDao workspaceDao,
            CacheMetadataDao cacheMetadataDao,
            ExecutorService executorService) {
        this.apiService = apiService;
        this.workspaceDao = workspaceDao;
        this.cacheMetadataDao = cacheMetadataDao;
        this.executorService = executorService;
        this.mainHandler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "WorkspaceRepositoryImplWithCache initialized with TTL support");
    }

    // ==================== GET ALL WORKSPACES ====================

    /**
     * Get all workspaces with cache-first approach + TTL checking
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
                // 1. Check cache metadata for TTL
                CacheMetadata metadata = cacheMetadataDao.getMetadata(CACHE_KEY);
                boolean isCacheExpired = (metadata == null || metadata.isExpired(CACHE_TTL_MS));

                if (isCacheExpired) {
                    Log.d(TAG, "Cache expired or empty, forcing API refresh");
                    mainHandler.post(() -> callback.onCacheEmpty());
                    fetchWorkspacesFromNetwork(callback, true);
                    return;
                }

                // 2. Return from cache (still fresh)
                List<WorkspaceEntity> cached = workspaceDao.getAll();
                if (cached != null && !cached.isEmpty()) {
                    List<Workspace> cachedWorkspaces = WorkspaceEntityMapper.toDomainList(cached);
                    mainHandler.post(() -> callback.onSuccess(cachedWorkspaces));

                    long ageSeconds = metadata.getAgeInSeconds();
                    Log.d(TAG, "✓ Cache hit: " + cached.size() + " workspaces (age: " +
                          ageSeconds + "s, TTL: " + (CACHE_TTL_MS/1000) + "s)");
                } else {
                    // Metadata exists but no data - treat as empty
                    mainHandler.post(() -> callback.onCacheEmpty());
                    Log.d(TAG, "Cache metadata found but no data");
                }

                // 3. Silent background refresh
                fetchWorkspacesFromNetwork(callback, false);

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

                                // Update cache metadata with timestamp
                                CacheMetadata metadata = new CacheMetadata(
                                    CACHE_KEY,
                                    System.currentTimeMillis(),
                                    workspaces.size()
                                );
                                cacheMetadataDao.insert(metadata);

                                Log.d(TAG, "✓ Cached " + workspaces.size() + " workspaces with timestamp");
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
                cacheMetadataDao.deleteMetadata(CACHE_KEY); // ← FIXED: delete() → deleteMetadata()
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
