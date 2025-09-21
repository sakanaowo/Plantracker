package com.example.tralalero.utils;

import android.util.Log;
import com.example.tralalero.auth.remote.AuthApi;
import com.example.tralalero.auth.remote.dto.LoginRequest;
import com.example.tralalero.auth.remote.dto.LoginResponse;
import com.example.tralalero.network.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiTestUtil {
    private static final String TAG = "ApiTestUtil";

    public static void testSignupEndpoint(String email, String password, TestCallback callback) {
        Log.d(TAG, "Testing signup endpoint with:");
        Log.d(TAG, "Email: " + email);
        Log.d(TAG, "Password length: " + password.length());
        Log.d(TAG, "Base URL: " + ApiClient.get().baseUrl());

        AuthApi api = ApiClient.get().create(AuthApi.class);
        LoginRequest request = new LoginRequest(email, password);

        Log.d(TAG, "Request JSON would be: {\"email\":\"" + email + "\", \"password\":\"[HIDDEN]\"}");

        Call<LoginResponse> call = api.register(request);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                Log.d(TAG, "=== API Response ===");
                Log.d(TAG, "Status Code: " + response.code());
                Log.d(TAG, "Status Message: " + response.message());
                Log.d(TAG, "Request URL: " + call.request().url());
                Log.d(TAG, "Request Method: " + call.request().method());
                Log.d(TAG, "Request Headers: " + call.request().headers());

                if (response.isSuccessful()) {
                    LoginResponse body = response.body();
                    Log.d(TAG, "Success Response Body: " + body);
                    if (body != null) {
                        Log.d(TAG, "Message: " + body.message);
                        Log.d(TAG, "Token: " + (body.token != null ? "[PRESENT]" : "[NULL]"));
                        Log.d(TAG, "User: " + (body.user != null ? body.user.email : "[NULL]"));
                    }
                    callback.onSuccess("Signup successful");
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                            Log.e(TAG, "Error Response Body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to read error body", e);
                    }

                    String message = "HTTP " + response.code() + ": " + response.message();
                    if (!errorBody.isEmpty()) {
                        message += "\nError: " + errorBody;
                    }
                    callback.onError(message);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "=== API Failure ===");
                Log.e(TAG, "Error Type: " + t.getClass().getSimpleName());
                Log.e(TAG, "Error Message: " + t.getMessage());
                Log.e(TAG, "Request URL: " + call.request().url());
                Log.e(TAG, "Full Error: ", t);

                String errorMessage = "Network Error: " + t.getClass().getSimpleName();
                if (t.getMessage() != null) {
                    errorMessage += " - " + t.getMessage();
                }
                callback.onError(errorMessage);
            }
        });
    }

    public interface TestCallback {
        void onSuccess(String message);
        void onError(String error);
    }
}
