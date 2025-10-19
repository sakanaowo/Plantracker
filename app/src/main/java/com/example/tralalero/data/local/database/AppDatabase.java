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
        CacheMetadata.class
    },
    version = 5,  // ← INCREMENTED from 4 to 5 for schema changes
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

    /**
     * Migration from version 4 to 5
     * ISSUE #1 FIX: WorkspaceEntity - Rename userId to ownerId, add type field
     * ISSUE #2 FIX: ProjectEntity - Add issueSeq, createdAt, updatedAt fields
     */
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // FIX ISSUE #1: WorkspaceEntity changes
            // Step 1: Add new columns
            database.execSQL("ALTER TABLE workspaces ADD COLUMN ownerId TEXT");
            database.execSQL("ALTER TABLE workspaces ADD COLUMN type TEXT NOT NULL DEFAULT 'TEAM'");

            // Step 2: Copy data from userId to ownerId
            database.execSQL("UPDATE workspaces SET ownerId = userId");

            // Note: Cannot drop column in SQLite, userId will remain but unused
            // New code will use ownerId field instead

            // FIX ISSUE #2: ProjectEntity changes
            database.execSQL("ALTER TABLE projects ADD COLUMN issueSeq INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE projects ADD COLUMN createdAt INTEGER");
            database.execSQL("ALTER TABLE projects ADD COLUMN updatedAt INTEGER");
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class,
                DATABASE_NAME
            )
            .addMigrations(MIGRATION_3_4, MIGRATION_4_5)  // ← ADDED migration 4→5
            .fallbackToDestructiveMigration()
            .build();
        }
        return instance;
    }
    
    public static void destroyInstance() {
        instance = null;
    }
}
