package com.example.tralalero.data.local.database;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.tralalero.data.local.database.converter.DateConverter;
import com.example.tralalero.data.local.database.dao.BoardDao;
import com.example.tralalero.data.local.database.dao.ProjectDao;
import com.example.tralalero.data.local.database.dao.TaskDao;
import com.example.tralalero.data.local.database.dao.WorkspaceDao;
import com.example.tralalero.data.local.database.entity.BoardEntity;
import com.example.tralalero.data.local.database.entity.ProjectEntity;
import com.example.tralalero.data.local.database.entity.TaskEntity;
import com.example.tralalero.data.local.database.entity.WorkspaceEntity;

@Database(
    entities = {
        TaskEntity.class,
        ProjectEntity.class,
        WorkspaceEntity.class,
        BoardEntity.class
    },
    version = 3,  // INCREMENTED from 2 to 3
    exportSchema = false
)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "plantracker_db";
    private static AppDatabase instance;
    
    public abstract TaskDao taskDao();
    public abstract ProjectDao projectDao();
    public abstract WorkspaceDao workspaceDao();
    public abstract BoardDao boardDao();  // ADDED

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class,
                DATABASE_NAME
            )
            .fallbackToDestructiveMigration()  // This handles schema changes
            .build();
        }
        return instance;
    }
    
    public static void destroyInstance() {
        instance = null;
    }
}
