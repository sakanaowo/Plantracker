package com.example.tralalero.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.tralalero.data.local.database.entity.BoardEntity;
import java.util.List;

@Dao
public interface BoardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BoardEntity board);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BoardEntity> boards);

    @Update
    void update(BoardEntity board);

    @Delete
    void delete(BoardEntity board);

    @Query("DELETE FROM boards")
    void deleteAll();

    @Query("SELECT * FROM boards WHERE id = :boardId LIMIT 1")
    BoardEntity getById(String boardId);

    @Query("SELECT * FROM boards WHERE projectId = :projectId ORDER BY `order` ASC")
    List<BoardEntity> getBoardsByProjectSync(String projectId);

    @Query("SELECT * FROM boards WHERE projectId = :projectId AND name = :name LIMIT 1")
    BoardEntity getBoardByName(String projectId, String name);

    @Query("DELETE FROM boards WHERE projectId = :projectId")
    void deleteByProjectId(String projectId);

    @Query("SELECT COUNT(*) FROM boards WHERE projectId = :projectId")
    int countByProjectId(String projectId);

    @Query("UPDATE boards SET `order` = :newOrder WHERE id = :boardId")
    void updateBoardOrder(String boardId, int newOrder);
}

