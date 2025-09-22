package com.example.tralalero;

import android.app.Application;

public class App extends Application {
    public static AuthManager authManager;

    @Override
    public void onCreate() {
        super.onCreate();
        authManager = new AuthManager();
    }
}