package com.example.tralalero.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.tralalero.data.local.database.entity.TaskEntity;

import java.util.List;

@Dao
public interface TaskDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TaskEntity task);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TaskEntity> tasks);
    
    @Update
    void update(TaskEntity task);
    
    @Delete
    void delete(TaskEntity task);
    
    @Query("DELETE FROM tasks")
    void deleteAll();
    
    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    TaskEntity getById(int taskId);
    
    @Query("SELECT * FROM tasks WHERE userId = :userId")
    List<TaskEntity> getAllByUserId(String userId);
    
    @Query("SELECT * FROM tasks WHERE projectId = :projectId")
    List<TaskEntity> getByProjectId(int projectId);
    
    @Query("SELECT * FROM tasks WHERE userId = :userId AND completed = 0")
    List<TaskEntity> getIncompleteTasks(String userId);
    
    @Query("SELECT * FROM tasks WHERE userId = :userId AND completed = 1")
    List<TaskEntity> getCompletedTasks(String userId);
    
    @Query("DELETE FROM tasks WHERE userId = :userId")
    void deleteByUserId(String userId);
    
    @Query("DELETE FROM tasks WHERE projectId = :projectId")
    void deleteByProjectId(int projectId);
    
    @Query("SELECT COUNT(*) FROM tasks WHERE userId = :userId")
    int getTaskCount(String userId);
}
