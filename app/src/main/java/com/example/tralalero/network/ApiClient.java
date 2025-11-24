package com.example.tralalero.network;

import android.util.Log;

import com.example.tralalero.BuildConfig;
import com.example.tralalero.auth.remote.AuthManager;
import com.example.tralalero.auth.remote.FirebaseAuthenticator;
import com.example.tralalero.auth.remote.FirebaseInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
                .connectTimeout(60, TimeUnit.SECONDS)  // Increased for Render cold start
                .readTimeout(60, TimeUnit.SECONDS)     // Increased for slow responses
                .writeTimeout(60, TimeUnit.SECONDS)    // Increased for upload operations
                .retryOnConnectionFailure(true);        // Auto retry on connection failure

        if (authManager != null) {
            clientBuilder.addInterceptor(new FirebaseInterceptor(authManager));

            clientBuilder.authenticator(new FirebaseAuthenticator(authManager));
        }

        OkHttpClient client = clientBuilder
                .addInterceptor(logging)
                .build();

        // ✅ FIXED: Create Gson with proper configuration for nested objects
        Gson gson = new GsonBuilder()
                .setLenient()
                .serializeNulls()  // ✅ Include null fields in JSON
                .create();

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson)) // ✅ Use custom Gson
                .client(client)
                .build();
    }
}
