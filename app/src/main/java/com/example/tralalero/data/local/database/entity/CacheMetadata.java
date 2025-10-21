package com.example.tralalero.data.local.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Cache metadata to track cache freshness
 * Stores timestamp of last cache update per entity type
 *
 * @author Cache Enhancement
 * @date October 19, 2025
 */
@Entity(tableName = "cache_metadata")
public class CacheMetadata {

    @PrimaryKey
    @NonNull
    private String cacheKey;  // e.g., "workspaces", "tasks_userId"

    private long lastUpdated;  // Timestamp in milliseconds

    private int itemCount;     // Number of items cached

    // Constructors
    public CacheMetadata(@NonNull String cacheKey, long lastUpdated, int itemCount) {
        this.cacheKey = cacheKey;
        this.lastUpdated = lastUpdated;
        this.itemCount = itemCount;
    }

    // Getters & Setters
    @NonNull
    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(@NonNull String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    /**
     * Check if cache is expired based on TTL
     *
     * @param ttlMillis Time to live in milliseconds
     * @return true if expired
     */
    public boolean isExpired(long ttlMillis) {
        return System.currentTimeMillis() - lastUpdated > ttlMillis;
    }

    /**
     * Get age of cache in seconds
     */
    public long getAgeInSeconds() {
        return (System.currentTimeMillis() - lastUpdated) / 1000;
    }
}

