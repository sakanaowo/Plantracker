package com.example.tralalero.App;

import android.app.Application;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.tralalero.auth.remote.AuthManager;
import com.example.tralalero.auth.storage.TokenManager;
import com.example.tralalero.sync.StartupSyncWorker;

public class App extends Application {
    public static AuthManager authManager;
    public static TokenManager tokenManager;

    @Override
    public void onCreate() {
        super.onCreate();
        authManager = new AuthManager(this);
        tokenManager = new TokenManager(this);

        OneTimeWorkRequest syncWork =
                new OneTimeWorkRequest.Builder(StartupSyncWorker.class)
                        .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                        .build();

        WorkManager.getInstance(this).enqueueUniqueWork(
                "startup_sync",
                ExistingWorkPolicy.REPLACE,
                syncWork
        );
    }
}
