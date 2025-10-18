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

    // ==================== STANDARD ROOM METHODS ====================

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

    // ==================== QUERY METHODS ====================

    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    ProjectEntity getById(String projectId);

    @Query("SELECT * FROM projects WHERE `key` = :projectKey LIMIT 1")
    ProjectEntity getByKey(String projectKey);

    @Query("SELECT * FROM projects WHERE workspaceId = :workspaceId")
    List<ProjectEntity> getByWorkspaceId(String workspaceId);

    @Query("SELECT * FROM projects WHERE boardType = :boardType")
    List<ProjectEntity> getByBoardType(String boardType);

    @Query("SELECT * FROM projects WHERE workspaceId = :workspaceId AND boardType = :boardType")
    List<ProjectEntity> getByWorkspaceAndBoardType(String workspaceId, String boardType);

    // ==================== REPOSITORY ALIAS METHODS ====================

    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    ProjectEntity getProjectByIdSync(String projectId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProject(ProjectEntity project);

    @Update
    void updateProject(ProjectEntity project);

    @Query("DELETE FROM projects WHERE id = :projectId")
    void deleteProjectById(String projectId);

    // ==================== UPDATE METHODS ====================

    @Query("UPDATE projects SET `key` = :newKey WHERE id = :projectId")
    void updateProjectKey(String projectId, String newKey);

    @Query("UPDATE projects SET boardType = :newBoardType WHERE id = :projectId")
    void updateBoardType(String projectId, String newBoardType);

    // ==================== DELETE METHODS ====================

    @Query("DELETE FROM projects WHERE workspaceId = :workspaceId")
    void deleteByWorkspaceId(String workspaceId);

    // ==================== COUNT METHODS ====================

    @Query("SELECT COUNT(*) FROM projects WHERE workspaceId = :workspaceId")
    int countByWorkspaceId(String workspaceId);
}

