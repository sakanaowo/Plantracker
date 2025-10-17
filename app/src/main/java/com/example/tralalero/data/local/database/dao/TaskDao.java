package com.example.tralalero.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import com.example.tralalero.data.local.database.entity.TaskEntity;
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
    TaskEntity getById(String taskId);
    
    @Query("SELECT * FROM tasks WHERE assigneeId = :userId OR createdBy = :userId ORDER BY position ASC")
    List<TaskEntity> getAllByUserId(String userId);
    
    @Query("SELECT * FROM tasks WHERE boardId = :boardId ORDER BY position ASC")
    List<TaskEntity> getTasksByBoardSync(String boardId);
    
    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY position ASC")
    List<TaskEntity> getByProjectId(String projectId);
    
    @Query("SELECT * FROM tasks WHERE assigneeId = :assigneeId ORDER BY position ASC")
    List<TaskEntity> getTasksByAssigneeSync(String assigneeId);
    
    @Query("SELECT * FROM tasks WHERE sprintId = :sprintId ORDER BY position ASC")
    List<TaskEntity> getTasksBySprintSync(String sprintId);
    
    @Query("UPDATE tasks SET position = :newPosition WHERE id = :taskId")
    void updateTaskPosition(String taskId, double newPosition);
    
    @Query("UPDATE tasks SET boardId = :newBoardId, position = :newPosition WHERE id = :taskId")
    void moveTaskToBoard(String taskId, String newBoardId, double newPosition);
    
    @Query("UPDATE tasks SET status = :newStatus WHERE id = :taskId")
    void updateTaskStatus(String taskId, String newStatus);
    
    @Query("DELETE FROM tasks WHERE projectId = :projectId")
    void deleteByProjectId(String projectId);
    
    @Query("DELETE FROM tasks WHERE boardId = :boardId")
    void deleteByBoardId(String boardId);
    
    @Query("SELECT COUNT(*) FROM tasks WHERE boardId = :boardId")
    int countByBoardId(String boardId);
    
    @Query("SELECT COUNT(*) FROM tasks WHERE projectId = :projectId")
    int countByProjectId(String projectId);
}
