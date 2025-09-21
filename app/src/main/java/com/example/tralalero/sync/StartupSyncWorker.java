package com.example.tralalero.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.tralalero.App.App;
import com.example.tralalero.auth.remote.AuthApi;
import com.example.tralalero.auth.remote.dto.UserDto;
import com.example.tralalero.network.ApiClient;

import retrofit2.Response;
import retrofit2.Retrofit;

public class StartupSyncWorker extends Worker {
    public StartupSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (!App.authManager.isSignedIn()) return Result.success();
        try {
            Retrofit r = ApiClient.get(App.authManager);
            AuthApi api = r.create(AuthApi.class);

            Response<UserDto> res = api.getMe().execute();
            if (res.isSuccessful()) {
//                TODO: save profile in cache/room if necessary
                return Result.success();
            } else if (res.code() == 401) {
                App.authManager.signOut();
                return Result.success();
            } else {
                return Result.retry();
            }
        } catch (Exception e) {
            return Result.retry();
        }
    }
}
