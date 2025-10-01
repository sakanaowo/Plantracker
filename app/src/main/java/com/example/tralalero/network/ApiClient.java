package com.example.tralalero.network;

import android.util.Log;

import com.example.tralalero.BuildConfig;
import com.example.tralalero.auth.remote.AuthManager;
import com.example.tralalero.auth.remote.FirebaseAuthenticator;
import com.example.tralalero.auth.remote.FirebaseInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static volatile Retrofit retrofit;
    private static volatile Retrofit authedRetrofit;

    public static Retrofit get() {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    retrofit = buildRetrofit(null);
                }
            }
        }
        return retrofit;
    }

    public static Retrofit get(AuthManager authManager) {
        if (authManager == null) {
            return get();
        }

        if (authedRetrofit == null) {
            synchronized (ApiClient.class) {
                if (authedRetrofit == null) {
                    authedRetrofit = buildRetrofit(authManager);
                }
            }
        }
        return authedRetrofit;
    }

    private static Retrofit buildRetrofit(AuthManager authManager) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message ->
                Log.d("API", message));
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);

        if (authManager != null) {
            // Add Firebase Interceptor to automatically add token to requests
            clientBuilder.addInterceptor(new FirebaseInterceptor(authManager));

            // Add Firebase Authenticator to handle token refresh on 401/404 errors
            clientBuilder.authenticator(new FirebaseAuthenticator(authManager));
        }

        OkHttpClient client = clientBuilder
                .addInterceptor(logging)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }
}
