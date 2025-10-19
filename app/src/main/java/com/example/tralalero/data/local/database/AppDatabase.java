package com.example.tralalero.data.local.database;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.tralalero.data.local.database.converter.DateConverter;
import com.example.tralalero.data.local.database.dao.BoardDao;
import com.example.tralalero.data.local.database.dao.CacheMetadataDao;
import com.example.tralalero.data.local.database.dao.ProjectDao;
import com.example.tralalero.data.local.database.dao.TaskDao;
import com.example.tralalero.data.local.database.dao.WorkspaceDao;
import com.example.tralalero.data.local.database.entity.BoardEntity;
import com.example.tralalero.data.local.database.entity.CacheMetadata;
import com.example.tralalero.data.local.database.entity.ProjectEntity;
import com.example.tralalero.data.local.database.entity.TaskEntity;
import com.example.tralalero.data.local.database.entity.WorkspaceEntity;

@Database(
    entities = {
        TaskEntity.class,
        ProjectEntity.class,
        WorkspaceEntity.class,
        BoardEntity.class,
        CacheMetadata.class  // ← ADDED for cache TTL tracking
    },
    version = 4,  // ← INCREMENTED from 3 to 4
    exportSchema = false
)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "plantracker_db";
    private static AppDatabase instance;
    
    public abstract TaskDao taskDao();
    public abstract ProjectDao projectDao();
    public abstract WorkspaceDao workspaceDao();
    public abstract BoardDao boardDao();
    public abstract CacheMetadataDao cacheMetadataDao();  // ← ADDED

    /**
     * Migration from version 3 to 4
     * Adds cache_metadata table for TTL tracking
     */
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS cache_metadata (" +
                "cacheKey TEXT PRIMARY KEY NOT NULL, " +
                "lastUpdated INTEGER NOT NULL, " +
                "itemCount INTEGER NOT NULL)"
            );
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class,
                DATABASE_NAME
            )
            .addMigrations(MIGRATION_3_4)  // ← ADDED migration
            .fallbackToDestructiveMigration()
            .build();
        }
        return instance;
    }
    
    public static void destroyInstance() {
        instance = null;
    }
}
