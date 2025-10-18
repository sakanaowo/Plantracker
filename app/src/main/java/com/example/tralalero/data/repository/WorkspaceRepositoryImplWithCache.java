package com.example.tralalero.data.repository;

import android.util.Log;

import com.example.tralalero.auth.storage.TokenManager;
import com.example.tralalero.data.local.database.dao.WorkspaceDao;
import com.example.tralalero.data.local.database.entity.WorkspaceEntity;
import com.example.tralalero.data.mapper.WorkspaceEntityMapper;
import com.example.tralalero.domain.model.Workspace;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Workspace Repository with Room Database caching
 * Pattern: Cache-first, read-only from database
 * Same pattern as TaskRepositoryImplWithCache
 * 
 * @author Person 1
 * @date October 18, 2025
 */
public class WorkspaceRepositoryImplWithCache {
    
    private static final String TAG = "WorkspaceRepoCache";
    
    private final WorkspaceDao workspaceDao;
    private final ExecutorService executorService;
    private final TokenManager tokenManager;
    
    public WorkspaceRepositoryImplWithCache(
            WorkspaceDao workspaceDao, 
            ExecutorService executorService, 
            TokenManager tokenManager) {
        this.workspaceDao = workspaceDao;
        this.executorService = executorService;
        this.tokenManager = tokenManager;
        Log.d(TAG, "WorkspaceRepositoryImplWithCache initialized");
    }
    
    // ==================== GET ALL WORKSPACES ====================
    
    public void getWorkspaces(WorkspaceCallback callback) {
        executorService.execute(() -> {
            try {
                String userId = tokenManager.getUserId();
                
                List<WorkspaceEntity> entities = workspaceDao.getAllByUserId(userId);
                
                if (entities != null && !entities.isEmpty()) {
                    Log.d(TAG, "✓ Loaded " + entities.size() + " workspaces from cache");
                    List<Workspace> workspaces = WorkspaceEntityMapper.toDomainList(entities);
                    callback.onSuccess(workspaces);
                } else {
                    Log.d(TAG, "Cache empty, need to fetch from API");
                    callback.onCacheEmpty();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading workspaces from cache", e);
                callback.onError(e);
            }
        });
    }
    
    // ==================== GET WORKSPACE BY ID ====================
    
    public void getWorkspaceById(String workspaceId, WorkspaceSingleCallback callback) {
        executorService.execute(() -> {
            try {
                //String userId = tokenManager.getUserId();
                
                WorkspaceEntity entity = workspaceDao.getById(workspaceId);//, userId);
                
                if (entity != null) {
                    Log.d(TAG, "✓ Loaded workspace " + workspaceId + " from cache");
                    Workspace workspace = WorkspaceEntityMapper.toDomain(entity);
                    callback.onSuccess(workspace);
                } else {
                    Log.d(TAG, "Workspace " + workspaceId + " not in cache");
                    callback.onCacheEmpty();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading workspace from cache", e);
                callback.onError(e);
            }
        });
    }
    
    // ==================== CALLBACK INTERFACES ====================
    
    public interface WorkspaceCallback {
        void onSuccess(List<Workspace> workspaces);
        void onCacheEmpty();
        void onError(Exception e);
    }
    
    public interface WorkspaceSingleCallback {
        void onSuccess(Workspace workspace);
        void onCacheEmpty();
        void onError(Exception e);
    }
}
