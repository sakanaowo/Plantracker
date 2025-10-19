package com.example.tralalero.data.repository;

import android.util.Log;

import com.example.tralalero.auth.storage.TokenManager;
import com.example.tralalero.data.local.database.dao.CacheMetadataDao;
import com.example.tralalero.data.local.database.dao.TaskDao;
import com.example.tralalero.data.local.database.entity.CacheMetadata;
import com.example.tralalero.data.mapper.TaskEntityMapper;
import com.example.tralalero.domain.model.Task;
import com.example.tralalero.data.local.database.entity.TaskEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Task Repository with Room Database caching + TTL support
 *
 * @author Person 2
 * @updated October 19, 2025 - Added TTL support
 */
public class TaskRepositoryImplWithCache {
    
    private static final String TAG = "TaskRepoCache";
    private static final long CACHE_TTL_MS = 3 * 60 * 1000; // 3 minutes (tasks change more frequently)

    private final TaskDao taskDao;
    private final ExecutorService executorService;
    private final TokenManager tokenManager;
    private final CacheMetadataDao cacheMetadataDao;

    public TaskRepositoryImplWithCache(
            TaskDao taskDao,
            ExecutorService executorService,
            TokenManager tokenManager,
            CacheMetadataDao cacheMetadataDao) {
        this.taskDao = taskDao;
        this.executorService = executorService;
        this.tokenManager = tokenManager;
        this.cacheMetadataDao = cacheMetadataDao;
        Log.d(TAG, "TaskRepositoryImplWithCache initialized with TTL support");
    }
    
    public void getAllTasks(TaskCallback callback) {
        executorService.execute(() -> {
            try {
                String userId = tokenManager.getUserId();
                String cacheKey = "tasks_" + userId;

                // Check cache metadata for TTL
                CacheMetadata metadata = cacheMetadataDao.getMetadata(cacheKey);
                boolean isCacheExpired = (metadata == null || metadata.isExpired(CACHE_TTL_MS));

                if (isCacheExpired) {
                    Log.d(TAG, "Task cache expired, forcing refresh");
                    callback.onCacheEmpty();
                    return;
                }

                // Return cache if fresh
                List<TaskEntity> entities = taskDao.getAllByUserId(userId);
                
                if (entities != null && !entities.isEmpty()) {
                    long ageSeconds = metadata.getAgeInSeconds();
                    Log.d(TAG, "✓ Cache hit: " + entities.size() + " tasks (age: " +
                          ageSeconds + "s, TTL: " + (CACHE_TTL_MS/1000) + "s)");
                    List<Task> tasks = TaskEntityMapper.toDomainList(entities);
                    callback.onSuccess(tasks);
                } else {
                    Log.d(TAG, "Cache metadata found but no tasks");
                    callback.onCacheEmpty();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading tasks from cache", e);
                callback.onError(e);
            }
        });
    }
    
    public void saveTasksToCache(List<Task> tasks) {
        executorService.execute(() -> {
            try {
                String userId = tokenManager.getUserId();
                String cacheKey = "tasks_" + userId;

                List<TaskEntity> entities = TaskEntityMapper.toEntityList(tasks);
                taskDao.insertAll(entities);

                // Update metadata with timestamp
                CacheMetadata metadata = new CacheMetadata(
                    cacheKey,
                    System.currentTimeMillis(),
                    tasks.size()
                );
                cacheMetadataDao.insert(metadata);

                Log.d(TAG, "✓ Saved " + tasks.size() + " tasks with timestamp");
            } catch (Exception e) {
                Log.e(TAG, "Error saving tasks to cache", e);
            }
        });
    }
    
    public void getTaskById(String taskId, SingleTaskCallback callback) {
        executorService.execute(() -> {
            try {
                TaskEntity entity = taskDao.getById(taskId);
                
                if (entity != null) {
                    Log.d(TAG, "✓ Loaded task " + taskId + " from cache");
                    Task task = TaskEntityMapper.toDomain(entity);
                    callback.onSuccess(task);
                } else {
                    Log.d(TAG, "Task " + taskId + " not in cache");
                    callback.onCacheEmpty();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading task from cache", e);
                callback.onError(e);
            }
        });
    }
    
    public void saveTaskToCache(Task task) {
        executorService.execute(() -> {
            try {
                TaskEntity entity = TaskEntityMapper.toEntity(task);
                taskDao.insert(entity);
                Log.d(TAG, "✓ Saved task " + task.getId() + " to cache");
            } catch (Exception e) {
                Log.e(TAG, "Error saving task to cache", e);
            }
        });
    }
    
    public void deleteTaskFromCache(String taskId) {
        executorService.execute(() -> {
            try {
                TaskEntity entity = taskDao.getById(taskId);
                if (entity != null) {
                    taskDao.delete(entity);
                    Log.d(TAG, "✓ Deleted task " + taskId + " from cache");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting task from cache", e);
            }
        });
    }
    
    public void clearCache() {
        executorService.execute(() -> {
            try {
                taskDao.deleteAll();
                Log.d(TAG, "✓ Task cache cleared");
            } catch (Exception e) {
                Log.e(TAG, "Error clearing task cache", e);
            }
        });
    }
    
    public interface TaskCallback {
        void onSuccess(List<Task> tasks);
        void onCacheEmpty();
        void onError(Exception e);
    }
    
    public interface SingleTaskCallback {
        void onSuccess(Task task);
        void onCacheEmpty();
        void onError(Exception e);
    }
}
