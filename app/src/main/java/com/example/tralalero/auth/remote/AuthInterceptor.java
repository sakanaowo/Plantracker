package com.example.tralalero.auth.remote;

import java.io.IOException;

import okhttp3.Request;

public class AuthInterceptor implements okhttp3.Interceptor {
    private final AuthManager authManager;

    public AuthInterceptor(AuthManager authManager) {
        this.authManager = authManager;
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        // Bỏ qua nếu endpoint public
        if (shouldSkipAuth(original.url().encodedPath())) {
            return chain.proceed(original);
        }

        String token = null;
        try {
            token = authManager.getIdTokenBlocking();
        } catch (Exception e) {
            // Có thể token hết hạn/revoked → fallback signOut hoặc forward 401
        }

        if (token == null) {
            // Không có token → proceed không header, BE sẽ trả 401 → app điều hướng login
            return chain.proceed(original);
        }

        Request authed = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();

        okhttp3.Response resp = chain.proceed(authed);

        // Nếu BE trả 401 → có thể token bị revoke → signOut và để UI điều hướng login
        if (resp.code() == 401) {
            try { authManager.signOut(); } catch (Exception ignored) {}
        }
        return resp;
    }

    private boolean shouldSkipAuth(String path) {
        // Khai báo các route public của BE nếu có
        return path.startsWith("/auth/public") || path.startsWith("/health");
    }
}
