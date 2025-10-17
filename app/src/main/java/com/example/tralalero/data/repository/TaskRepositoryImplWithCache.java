package com.example.tralalero.data.repository;

import android.util.Log;

import com.example.tralalero.auth.storage.TokenManager;
import com.example.tralalero.data.local.database.dao.TaskDao;
import com.example.tralalero.data.mapper.TaskEntityMapper;
import com.example.tralalero.domain.model.Task;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class TaskRepositoryImplWithCache {
    
    private static final String TAG = "TaskRepoCache";
    
    private final TaskDao taskDao;
    private final ExecutorService executorService;
    private final TokenManager tokenManager;
    
    public TaskRepositoryImplWithCache(TaskDao taskDao, ExecutorService executorService, TokenManager tokenManager) {
        this.taskDao = taskDao;
        this.executorService = executorService;
        this.tokenManager = tokenManager;
    }
    
    public void getAllTasks(TaskCallback callback) {
        executorService.execute(() -> {
            try {
                String userId = tokenManager.getUserId();
                
                List<com.example.tralalero.data.local.database.entity.TaskEntity> entities = taskDao.getAllByUserId(userId);
                
                if (entities != null && !entities.isEmpty()) {
                    Log.d(TAG, "✓ Loaded " + entities.size() + " tasks from cache");
                    List<Task> tasks = TaskEntityMapper.toDomainList(entities);
                    callback.onSuccess(tasks);
                } else {
                    Log.d(TAG, "Cache empty, need to fetch from API");
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
                List<com.example.tralalero.data.local.database.entity.TaskEntity> entities = TaskEntityMapper.toEntityList(tasks);
                taskDao.insertAll(entities);
                Log.d(TAG, "✓ Saved " + tasks.size() + " tasks to cache");
            } catch (Exception e) {
                Log.e(TAG, "Error saving tasks to cache", e);
            }
        });
    }
    
    public void getTaskById(int taskId, SingleTaskCallback callback) {
        executorService.execute(() -> {
            try {
                com.example.tralalero.data.local.database.entity.TaskEntity entity = taskDao.getById(taskId);
                
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
                com.example.tralalero.data.local.database.entity.TaskEntity entity = TaskEntityMapper.toEntity(task);
                taskDao.insert(entity);
                Log.d(TAG, "✓ Saved task " + task.getId() + " to cache");
            } catch (Exception e) {
                Log.e(TAG, "Error saving task to cache", e);
            }
        });
    }
    
    public void deleteTaskFromCache(int taskId) {
        executorService.execute(() -> {
            try {
                com.example.tralalero.data.local.database.entity.TaskEntity entity = taskDao.getById(taskId);
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
