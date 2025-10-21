package com.example.tralalero.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.tralalero.data.local.database.entity.CacheMetadata;

/**
 * DAO for cache metadata operations
 * Manages cache freshness tracking
 *
 * @author Cache Enhancement
 * @date October 19, 2025
 */
@Dao
public interface CacheMetadataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CacheMetadata metadata);

    @Query("SELECT * FROM cache_metadata WHERE cacheKey = :key")
    CacheMetadata getMetadata(String key);

    @Query("DELETE FROM cache_metadata WHERE cacheKey = :key")
    void deleteMetadata(String key);

    @Query("DELETE FROM cache_metadata")
    void deleteAll();
}

