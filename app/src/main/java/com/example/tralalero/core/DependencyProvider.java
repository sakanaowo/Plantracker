package com.example.tralalero.core;


import android.content.Context;
import android.util.Log;

import com.example.tralalero.App.App;
import com.example.tralalero.auth.remote.AuthManager;
import com.example.tralalero.auth.storage.TokenManager;
import com.example.tralalero.data.local.database.AppDatabase;
import com.example.tralalero.data.local.database.dao.BoardDao;
import com.example.tralalero.data.local.database.dao.ProjectDao;
import com.example.tralalero.data.local.database.dao.TaskDao;
import com.example.tralalero.data.local.database.dao.WorkspaceDao;
import com.example.tralalero.data.remote.api.WorkspaceApiService;
import com.example.tralalero.data.repository.TaskRepositoryImplWithCache;
import com.example.tralalero.data.repository.WorkspaceRepositoryImplWithCache;
import com.example.tralalero.network.ApiClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DependencyProvider - Singleton quản lý tất cả dependencies của app
 * 
 * Cung cấp:
 * - Room Database instance
 * - DAOs (TaskDao, ProjectDao, WorkspaceDao)
 * - Cached Repositories
 * - Cache management (clear cache, reset)
 * 
 * Usage:
 *   DependencyProvider provider = DependencyProvider.getInstance(context, authManager);
 *   AppDatabase db = provider.getDatabase();
 *   TaskDao taskDao = provider.getTaskDao();
 */
public class DependencyProvider {
    private static final String TAG = "DependencyProvider";
    private static DependencyProvider instance;
    
    // Database
    private final AppDatabase database;
    
    // DAOs
    private final TaskDao taskDao;
    private final ProjectDao projectDao;
    private final WorkspaceDao workspaceDao;
    private final BoardDao boardDao;

    // Cached Repositories
    private TaskRepositoryImplWithCache taskRepositoryWithCache;
    private WorkspaceRepositoryImplWithCache workspaceRepositoryWithCache;
    
    // Executor for background operations
    private final ExecutorService executorService;
    
    // Token Manager
    private final TokenManager tokenManager;

    /**
     * Private constructor - Singleton pattern
     */
    private DependencyProvider(Context context, TokenManager tokenManager) {
        this.tokenManager = tokenManager;

        // Initialize Database
        this.database = AppDatabase.getInstance(context);
        Log.d(TAG, "✓ AppDatabase initialized");
        
        // Initialize DAOs
        this.taskDao = database.taskDao();
        this.projectDao = database.projectDao();
        this.workspaceDao = database.workspaceDao();
        this.boardDao = database.boardDao();
        Log.d(TAG, "✓ All DAOs initialized");

        // Initialize ExecutorService
        this.executorService = Executors.newFixedThreadPool(3);
        Log.d(TAG, "✓ ExecutorService initialized with 3 threads");
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized DependencyProvider getInstance(Context context, TokenManager tokenManager) {
        if (instance == null) {
            instance = new DependencyProvider(context.getApplicationContext(), tokenManager);
            Log.d(TAG, "DependencyProvider instance created");
        }
        return instance;
    }
    
    /**
     * Reset singleton - useful for logout or testing
     */
    public static synchronized void reset() {
        if (instance != null) {
            instance.clearAllCaches();
            instance = null;
            Log.d(TAG, "✓ DependencyProvider reset");
        }
    }
    
    // ==================== Database & DAOs ====================
    
    public AppDatabase getDatabase() {
        return database;
    }
    
    public TaskDao getTaskDao() {
        return taskDao;
    }
    
    public ProjectDao getProjectDao() {
        return projectDao;
    }
    
    public WorkspaceDao getWorkspaceDao() {
        return workspaceDao;
    }
    
    public ExecutorService getExecutorService() {
        return executorService;
    }
    
    // ==================== Cached Repositories ====================
    
    /**
     * Get TaskRepository with caching
     * Lazy initialization - only created when needed
     */
    public synchronized TaskRepositoryImplWithCache getTaskRepositoryWithCache() {
        if (taskRepositoryWithCache == null) {
            taskRepositoryWithCache = new TaskRepositoryImplWithCache(
                taskDao,
                executorService,
                tokenManager
            );
            Log.d(TAG, "✓ TaskRepositoryImplWithCache created");
        }
        return taskRepositoryWithCache;
    }
    
    /**
     * Get WorkspaceRepository with caching
     * Lazy initialization - only created when needed
     * 
     * @author Person 1
     */
    public synchronized WorkspaceRepositoryImplWithCache getWorkspaceRepositoryWithCache() {
        if (workspaceRepositoryWithCache == null) {
            // Get WorkspaceApiService from ApiClient with App.authManager
            WorkspaceApiService apiService = ApiClient.get(App.authManager)
                .create(WorkspaceApiService.class);

            workspaceRepositoryWithCache = new WorkspaceRepositoryImplWithCache(
                apiService,          // ✅ WorkspaceApiService
                workspaceDao,        // ✅ WorkspaceDao
                executorService      // ✅ ExecutorService
            );
            Log.d(TAG, "✓ WorkspaceRepositoryImplWithCache created");
        }
        return workspaceRepositoryWithCache;
    }
    
    // TODO: Person 2 - Add more cached repositories
    // public synchronized ProjectRepositoryImplWithCache getProjectRepositoryWithCache() { ... }
    // public synchronized BoardRepositoryImplWithCache getBoardRepositoryWithCache() { ... }
    // public synchronized WorkspaceRepositoryImplWithCache getWorkspaceRepositoryWithCache() { ... }
    
    // ==================== Cache Management ====================
    
    /**
     * Clear all caches - called on logout or app termination
     */
    public void clearAllCaches() {
        Log.d(TAG, "Clearing all caches...");
        
        executorService.execute(() -> {
            try {
                // Clear Task cache
                if (taskDao != null) {
                    taskDao.deleteAll();
                    Log.d(TAG, "✓ Task cache cleared");
                }
                
                // Clear Project cache
                if (projectDao != null) {
                    projectDao.deleteAll();
                    Log.d(TAG, "✓ Project cache cleared");
                }
                
                // Clear Workspace cache
                if (workspaceDao != null) {
                    workspaceDao.deleteAll();
                    Log.d(TAG, "✓ Workspace cache cleared");
                }
                
                // Clear Board cache
                if (boardDao != null) {
                    boardDao.deleteAll();
                    Log.d(TAG, "✓ Board cache cleared");
                }

                Log.d(TAG, "✓ All caches cleared successfully");
                
            } catch (Exception e) {
                Log.e(TAG, "Error clearing caches", e);
            }
        });
    }
    
    /**
     * Clear specific cache by type
     */
    public void clearTaskCache() {
        executorService.execute(() -> {
            taskDao.deleteAll();
            Log.d(TAG, "✓ Task cache cleared");
        });
    }
    
    public void clearProjectCache() {
        executorService.execute(() -> {
            projectDao.deleteAll();
            Log.d(TAG, "✓ Project cache cleared");
        });
    }
    
    public void clearWorkspaceCache() {
        executorService.execute(() -> {
            workspaceDao.deleteAll();
            Log.d(TAG, "✓ Workspace cache cleared");
        });
    }

    public void clearBoardCache() {
        executorService.execute(() -> {
            boardDao.deleteAll();
            Log.d(TAG, "✓ Board cache cleared");
        });
    }
}
