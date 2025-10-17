package com.example.tralalero.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.tralalero.data.local.database.entity.ProjectEntity;

import java.util.List;

@Dao
public interface ProjectDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ProjectEntity project);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ProjectEntity> projects);
    
    @Update
    void update(ProjectEntity project);
    
    @Delete
    void delete(ProjectEntity project);
    
    @Query("DELETE FROM projects")
    void deleteAll();
    
    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    ProjectEntity getById(int projectId);
    
    @Query("SELECT * FROM projects WHERE userId = :userId")
    List<ProjectEntity> getAllByUserId(String userId);
    
    @Query("SELECT * FROM projects WHERE workspaceId = :workspaceId")
    List<ProjectEntity> getByWorkspaceId(int workspaceId);
    
    @Query("DELETE FROM projects WHERE userId = :userId")
    void deleteByUserId(String userId);
    
    @Query("DELETE FROM projects WHERE workspaceId = :workspaceId")
    void deleteByWorkspaceId(int workspaceId);
    
    @Query("SELECT COUNT(*) FROM projects WHERE userId = :userId")
    int getProjectCount(String userId);
}
