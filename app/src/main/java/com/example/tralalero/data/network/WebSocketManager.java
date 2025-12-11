package com.example.tralalero.data.network;

import android.util.Log;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

/**
 * Singleton WebSocket manager using Socket.io for real-time notifications
 */
public class WebSocketManager {
    private static final String TAG = "WebSocketManager";
    private static WebSocketManager instance;
    
    private Socket socket;
    private String authToken;
    private boolean isConnected = false;
    private final List<ActivityLogListener> activityLogListeners = new ArrayList<>();
    
    public interface ActivityLogListener {
        void onActivityLogReceived(JSONObject log);
    }
    
    private WebSocketManager() {
        // Private constructor
    }
    
    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }
    
    /**
     * Initialize and connect to WebSocket server
     */
    public void connect(String serverUrl, String token) {
        if (socket != null && socket.connected()) {
            Log.d(TAG, "Already connected");
            return;
        }
        
        this.authToken = token;
        
        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            options.reconnection = true;
            options.reconnectionDelay = 1000;
            options.reconnectionAttempts = Integer.MAX_VALUE;
            
            // Add auth token to connection
            if (token != null && !token.isEmpty()) {
                options.auth = new java.util.HashMap<String, String>() {{
                    put("token", token);
                }};
            }
            
            socket = IO.socket(serverUrl, options);
            
            // Connection event listeners
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    isConnected = true;
                    Log.d(TAG, "✅ WebSocket connected");
                }
            });
            
            socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    isConnected = false;
                    Log.d(TAG, "❌ WebSocket disconnected");
                }
            });
            
            socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e(TAG, "Connection error: " + args[0]);
                }
            });
            
            // Activity log event listener
            socket.on("activity_log", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (args.length > 0 && args[0] instanceof JSONObject) {
                        JSONObject logData = (JSONObject) args[0];
                        Log.d(TAG, "📝 Activity log received: " + logData.toString());
                        notifyActivityLogListeners(logData);
                    }
                }
            });
            
            socket.connect();
            Log.d(TAG, "Connecting to: " + serverUrl);
            
        } catch (URISyntaxException e) {
            Log.e(TAG, "Invalid WebSocket URL: " + serverUrl, e);
        }
    }
    
    /**
     * Disconnect from WebSocket server
     */
    public void disconnect() {
        if (socket != null) {
            socket.disconnect();
            socket.off();
            socket = null;
            isConnected = false;
            Log.d(TAG, "WebSocket disconnected manually");
        }
    }
    
    /**
     * Check if WebSocket is connected
     */
    public boolean isConnected() {
        return isConnected && socket != null && socket.connected();
    }
    
    /**
     * Add activity log listener
     */
    public void addActivityLogListener(ActivityLogListener listener) {
        if (!activityLogListeners.contains(listener)) {
            activityLogListeners.add(listener);
            Log.d(TAG, "Activity log listener added. Total: " + activityLogListeners.size());
        }
    }
    
    /**
     * Remove activity log listener
     */
    public void removeActivityLogListener(ActivityLogListener listener) {
        activityLogListeners.remove(listener);
        Log.d(TAG, "Activity log listener removed. Remaining: " + activityLogListeners.size());
    }
    
    /**
     * Notify all listeners about new activity log
     */
    private void notifyActivityLogListeners(JSONObject log) {
        for (ActivityLogListener listener : activityLogListeners) {
            listener.onActivityLogReceived(log);
        }
    }
}
