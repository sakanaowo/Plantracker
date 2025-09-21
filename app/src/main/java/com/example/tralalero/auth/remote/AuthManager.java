package com.example.tralalero.auth.remote;

import android.app.Application;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

public class AuthManager {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private volatile String cachedIdToken;
    private volatile long cachedExpiryEpochSec;

    public AuthManager(Application app) {
        auth.addIdTokenListener((FirebaseAuth.IdTokenListener) firebaseAuth -> {
            FirebaseUser u = firebaseAuth.getCurrentUser();
            if (u != null) {
                u.getIdToken(false).addOnSuccessListener(res -> {
                    cachedIdToken = res.getToken();
                    cachedExpiryEpochSec = res.getExpirationTimestamp();
                }).addOnFailureListener(e -> {
                    cachedIdToken = null;
                    cachedExpiryEpochSec = 0;
                });
            } else {
                cachedIdToken = null;
                cachedExpiryEpochSec = 0;
            }
        });
    }

    //get id token for interceptor, auto refresh if nearly outdate
    public String getIdTokenBlocking() throws Exception {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return null;
        boolean needRefresh = cachedIdToken == null ||
                (cachedExpiryEpochSec > 0 && (cachedExpiryEpochSec - 60) <= (System.currentTimeMillis() / 1000));
        Task<GetTokenResult> task = user.getIdToken(needRefresh);
        GetTokenResult res = com.google.android.gms.tasks.Tasks.await(task);
        cachedIdToken = res.getToken();
        cachedExpiryEpochSec = res.getExpirationTimestamp();
        return cachedIdToken;
    }

    public boolean isSignedIn() {
        return auth.getCurrentUser() != null;
    }

    public void signOut() {
        auth.signOut();
        cachedExpiryEpochSec = 0;
        cachedIdToken = null;
    }
}
