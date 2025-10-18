package com.example.tralalero.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.tralalero.data.local.database.entity.WorkspaceEntity;

import java.util.List;

@Dao
public interface WorkspaceDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WorkspaceEntity workspace);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WorkspaceEntity> workspaces);
    
    @Update
    void update(WorkspaceEntity workspace);
    
    @Delete
    void delete(WorkspaceEntity workspace);
    
    @Query("DELETE FROM workspaces")
    void deleteAll();
    
    @Query("SELECT * FROM workspaces WHERE id = :workspaceId LIMIT 1")
    WorkspaceEntity getById(String workspaceId);  // ✅ FIXED: Changed from int to String

    @Query("SELECT * FROM workspaces")
    List<WorkspaceEntity> getAll();

    @Query("SELECT * FROM workspaces WHERE userId = :userId")
    List<WorkspaceEntity> getAllByUserId(String userId);
    
    @Query("SELECT COUNT(*) FROM workspaces")
    int getCount();
}
