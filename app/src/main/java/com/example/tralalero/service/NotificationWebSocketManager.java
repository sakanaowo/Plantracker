package com.example.tralalero.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.tralalero.BuildConfig;
import com.example.tralalero.auth.remote.AuthManager;
import com.example.tralalero.data.remote.dto.websocket.NotificationPayload;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * NotificationWebSocketManager - Quản lý Socket.IO connection cho notifications
 * 
 * MIGRATION FROM OkHttp WebSocket TO Socket.IO:
 * - Backend đã migrate sang Socket.IO
 * - Client cần dùng socket.io-client library
 * - Support auto-reconnect, authentication, và lifecycle management
 * 
 * Features:
 * - Auto-connect khi app foreground
 * - Auto-disconnect khi app background  
 * - JWT authentication via handshake
 * - Notification delivery với callbacks
 * - Deduplication để tránh duplicate với FCM
 * 
 * Usage:
 *   NotificationWebSocketManager manager = new NotificationWebSocketManager();
 *   manager.setOnNotificationReceivedListener(notification -> { ... });
 *   manager.connect(token);
 */
public class NotificationWebSocketManager {
    private static final String TAG = "WSManager";
    
    private final Handler mainHandler;
    private final Gson gson;
    private SharedPreferences notifPrefs;
    private Context appContext;
    
    private Socket socket;
    private boolean isConnecting = false;
    private boolean shouldBeConnected = false;
    private final List<OnNotificationReceivedListener> listeners = new ArrayList<>();
    
    // Connection state callbacks
    public interface OnConnectionStateListener {
        void onConnected();
        void onDisconnected();
        void onError(String error);
    }
    
    private OnConnectionStateListener connectionStateListener;
    
    // Notification listener (legacy - for backward compatibility)
    public interface OnNotificationReceivedListener {
        void onNotificationReceived(NotificationPayload notification);
    }
    
    // Constructor
    public NotificationWebSocketManager() {
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.gson = new Gson();
    }
    
    /**
     * Initialize with context (call this before connect)
     */
    public void initialize(Context context) {
        this.appContext = context.getApplicationContext();
        this.notifPrefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE);
    }
    
    /**
     * Add notification listener
     */
    public void setOnNotificationReceivedListener(OnNotificationReceivedListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            Log.d(TAG, "Listener added. Total: " + listeners.size());
        }
    }
    
    /**
     * Remove notification listener
     */
    public void removeListener(OnNotificationReceivedListener listener) {
        listeners.remove(listener);
        Log.d(TAG, "Listener removed. Total: " + listeners.size());
    }
    
    /**
     * Set connection state listener
     */
    public void setOnConnectionStateListener(OnConnectionStateListener listener) {
        this.connectionStateListener = listener;
    }
    
    /**
     * Connect to Socket.IO server
     * @param token JWT token for authentication
     */
    public void connect(String token) {
        if (socket != null && socket.connected()) {
            Log.d(TAG, "Already connected");
            return;
        }
        
        if (isConnecting) {
            Log.d(TAG, "Connection in progress");
            return;
        }
        
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Cannot connect: No auth token");
            if (connectionStateListener != null) {
                mainHandler.post(() -> connectionStateListener.onError("No auth token"));
            }
            return;
        }
        
        shouldBeConnected = true;
        isConnecting = true;
        
        try {
            String wsUrl = BuildConfig.WS_URL;
            Log.d(TAG, "🔄 Connecting to: " + wsUrl);
            Log.d(TAG, "   Token: " + token.substring(0, Math.min(20, token.length())) + "...");
            
            IO.Options options = IO.Options.builder()
                    .setAuth(java.util.Collections.singletonMap("token", token))
                    .setReconnection(true)
                    .setReconnectionDelay(1000)
                    .setReconnectionDelayMax(5000)
                    .setReconnectionAttempts(Integer.MAX_VALUE)
                    .setTransports(new String[]{"websocket"})
                    .build();
            
            socket = IO.socket(wsUrl, options);
            setupSocketListeners();
            socket.connect();
            
        } catch (URISyntaxException e) {
            Log.e(TAG, "Invalid WebSocket URL", e);
            isConnecting = false;
            if (connectionStateListener != null) {
                mainHandler.post(() -> connectionStateListener.onError("Invalid URL: " + e.getMessage()));
            }
        }
    }
    
    /**
     * Disconnect from Socket.IO server
     */
    public void disconnect() {
        shouldBeConnected = false;
        
        // Re-enable FCM notifications when disconnecting (app going to background)
        if (notifPrefs != null) {
            notifPrefs.edit().putBoolean("show_fcm_notifications", true).apply();
            Log.d(TAG, "📊 WebSocket disconnecting → FCM ENABLED (background mode)");
        }
        
        if (socket != null) {
            socket.off(); // Remove all listeners
            socket.disconnect();
            socket.close();
            socket = null;
        }
        
        isConnecting = false;
        Log.d(TAG, "Disconnected from WebSocket");
    }
    
    /**
     * Check if currently connected
     */
    public boolean isConnected() {
        return socket != null && socket.connected();
    }
    
    /**
     * Subscribe to specific notification types
     */
    public void subscribe(String[] types) {
        if (socket == null || !socket.connected()) {
            Log.w(TAG, "Cannot subscribe - not connected");
            return;
        }
        
        try {
            JSONObject data = new JSONObject();
            data.put("types", new org.json.JSONArray(types));
            socket.emit("subscribe", data);
            Log.d(TAG, "Subscribed to: " + java.util.Arrays.toString(types));
        } catch (JSONException e) {
            Log.e(TAG, "Failed to subscribe", e);
        }
    }
    
    /**
     * Setup all Socket.IO event listeners
     */
    private void setupSocketListeners() {
        // Connection events
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                isConnecting = false;
                Log.d(TAG, "✅ Socket.IO CONNECTED");
                
                // Disable FCM when WebSocket is active (foreground only)
                if (notifPrefs != null) {
                    notifPrefs.edit().putBoolean("show_fcm_notifications", false).apply();
                    Log.d(TAG, "📊 WebSocket connected → FCM DISABLED (foreground mode)");
                }
                
                if (connectionStateListener != null) {
                    mainHandler.post(() -> connectionStateListener.onConnected());
                }
            }
        });
        
        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "❌ Socket.IO DISCONNECTED");
                
                // Re-enable FCM when WebSocket disconnects (background mode)
                if (notifPrefs != null) {
                    notifPrefs.edit().putBoolean("show_fcm_notifications", true).apply();
                    Log.d(TAG, "📊 WebSocket disconnected → FCM ENABLED (background mode)");
                }
                
                if (connectionStateListener != null) {
                    mainHandler.post(() -> connectionStateListener.onDisconnected());
                }
            }
        });
        
        socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                isConnecting = false;
                String error = args.length > 0 ? args[0].toString() : "Unknown error";
                Log.e(TAG, "Socket.IO connect error: " + error);
                
                // Ensure FCM is enabled on error
                if (notifPrefs != null) {
                    notifPrefs.edit().putBoolean("show_fcm_notifications", true).apply();
                }
                
                if (connectionStateListener != null) {
                    mainHandler.post(() -> connectionStateListener.onError(error));
                }
            }
        });
        
        // Welcome message
        socket.on("connected", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length > 0) {
                    Log.d(TAG, "Welcome: " + args[0].toString());
                }
            }
        });
        
        // Notification event
        socket.on("notification", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length > 0) {
                    try {
                        String json = args[0].toString();
                        Log.d(TAG, "📬 Received notification: " + json);
                        
                        NotificationPayload notification = gson.fromJson(json, NotificationPayload.class);
                        
                        // Notify all listeners on main thread
                        mainHandler.post(() -> {
                            for (OnNotificationReceivedListener listener : listeners) {
                                listener.onNotificationReceived(notification);
                            }
                        });
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse notification", e);
                    }
                }
            }
        });
        
        // Activity log event (for activity feed updates)
        socket.on("activity_log", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length > 0) {
                    try {
                        String json = args[0].toString();
                        Log.d(TAG, "📋 Received activity_log: " + json);
                        
                        // Broadcast activity_log event for ActivityFragment to refresh
                        if (appContext != null) {
                            Intent intent = new Intent("ACTIVITY_LOG_UPDATED");
                            LocalBroadcastManager.getInstance(appContext).sendBroadcast(intent);
                            Log.d(TAG, "🔔 Broadcasted ACTIVITY_LOG_UPDATED");
                        }
                        
                        NotificationPayload notification = gson.fromJson(json, NotificationPayload.class);
                        
                        // Notify all listeners on main thread (same as notification)
                        mainHandler.post(() -> {
                            for (OnNotificationReceivedListener listener : listeners) {
                                listener.onNotificationReceived(notification);
                            }
                        });
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse activity_log", e);
                    }
                }
            }
        });
        
        // Status updates
        socket.on("status", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length > 0) {
                    Log.d(TAG, "Status update: " + args[0].toString());
                }
            }
        });
        
        // Subscription confirmation
        socket.on("subscribed", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length > 0) {
                    Log.d(TAG, "Subscription confirmed: " + args[0].toString());
                }
            }
        });
        
        // Workspace updates (simple: just trigger refresh)
        socket.on("workspace_updated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length > 0) {
                    Log.d(TAG, "🏢 Workspace updated: " + args[0].toString());
                    // Broadcast to app: workspace list needs refresh
                    if (appContext != null) {
                        mainHandler.post(() -> {
                            android.content.Intent intent = new android.content.Intent("com.example.tralalero.WORKSPACE_UPDATED");
                            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(
                                appContext
                            ).sendBroadcast(intent);
                        });
                    }
                }
            }
        });
        
        // Task updates (simple: just trigger refresh)
        socket.on("task_updated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length > 0) {
                    Log.d(TAG, "📝 Task updated: " + args[0].toString());
                    // Broadcast to app: task list needs refresh
                    if (appContext != null) {
                        mainHandler.post(() -> {
                            android.content.Intent intent = new android.content.Intent("com.example.tralalero.TASK_UPDATED");
                            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(
                                appContext
                            ).sendBroadcast(intent);
                        });
                    }
                }
            }
        });
        
        // Event updates (simple: just trigger refresh)
        socket.on("event_updated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length > 0) {
                    Log.d(TAG, "📅 Event updated: " + args[0].toString());
                    // Broadcast to app: event list needs refresh
                    if (appContext != null) {
                        mainHandler.post(() -> {
                            android.content.Intent intent = new android.content.Intent("com.example.tralalero.EVENT_UPDATED");
                            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(
                                appContext
                            ).sendBroadcast(intent);
                        });
                    }
                }
            }
        });
    }
    
    /**
     * Mark notification as read (legacy method - keep for backward compatibility)
     */
    public void markAsRead(String notificationId) {
        Log.d(TAG, "markAsRead called (not implemented in Socket.IO version)");
        // TODO: Implement if backend adds this feature
    }
    
    /**
     * Set current user ID (legacy method - keep for backward compatibility)
     */
    public void setCurrentUserId(String userId) {
        Log.d(TAG, "setCurrentUserId: " + userId);
        // Not needed - userId extracted from JWT token in backend
    }
    
    /**
     * Cleanup resources
     */
    public void destroy() {
        disconnect();
        listeners.clear();
        connectionStateListener = null;
    }
}
