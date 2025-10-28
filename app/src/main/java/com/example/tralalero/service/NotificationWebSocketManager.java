package com.example.tralalero.service;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.tralalero.BuildConfig;
import com.example.tralalero.data.remote.dto.websocket.NotificationPayload;
import com.example.tralalero.data.remote.dto.websocket.SubscribeRequest;
import com.example.tralalero.data.remote.dto.websocket.WebSocketMessage;
import com.google.gson.Gson;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * WebSocket Manager for real-time notifications
 * Handles connection, reconnection, and message processing
 */
public class NotificationWebSocketManager {
    
    private static final String TAG = "WSManager";
    
    // WebSocket instance
    private WebSocket webSocket;
    private OkHttpClient client;
    private final Gson gson = new Gson();
    
    // Connection state
    private boolean isConnected = false;
    private boolean shouldReconnect = true;
    
    // Reconnection strategy
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final long INITIAL_RECONNECT_DELAY = 1000; // 1 second
    private static final long MAX_RECONNECT_DELAY = 30000; // 30 seconds
    
    // Handler for reconnection
    private final Handler reconnectHandler = new Handler(Looper.getMainLooper());
    private Runnable reconnectRunnable;
    
    // Ping/Pong for connection health
    private final Handler pingHandler = new Handler(Looper.getMainLooper());
    private static final long PING_INTERVAL = 30000; // 30 seconds
    
    // Message deduplication
    private final Set<String> receivedNotificationIds = new HashSet<>();
    private static final int MAX_DEDUP_SIZE = 100;
    
    // Callbacks
    public interface OnNotificationReceivedListener {
        void onNotificationReceived(NotificationPayload notification);
    }
    
    private OnNotificationReceivedListener notificationListener;
    
    // Current user ID and token
    private String currentUserId;
    private String currentToken;
    
    // Constructor
    public NotificationWebSocketManager() {
        initializeClient();
    }
    
    /**
     * Initialize OkHttp client with WebSocket support
     */
    private void initializeClient() {
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .pingInterval(30, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * Connect to WebSocket server
     * @param token JWT token for authentication
     */
    public void connect(String token) {
        if (isConnected) {
            Log.d(TAG, "Already connected to WebSocket");
            return;
        }
        
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Cannot connect: Token is null or empty");
            return;
        }
        
        this.currentToken = token;
        shouldReconnect = true;
        reconnectAttempts = 0;
        
        String wsUrl = BuildConfig.WS_URL;
        Log.d(TAG, "🔄 Connecting to WebSocket: " + wsUrl);
        
        Request request = new Request.Builder()
                .url(wsUrl)
                .addHeader("Authorization", "Bearer " + token)
                .build();
        
        webSocket = client.newWebSocket(request, webSocketListener);
    }
    
    /**
     * Disconnect from WebSocket server
     */
    public void disconnect() {
        Log.d(TAG, "🔴 Disconnecting WebSocket");
        shouldReconnect = false;
        
        // Cancel reconnection
        if (reconnectRunnable != null) {
            reconnectHandler.removeCallbacks(reconnectRunnable);
        }
        
        // Stop ping
        pingHandler.removeCallbacksAndMessages(null);
        
        // Close WebSocket
        if (webSocket != null) {
            webSocket.close(1000, "Client disconnect");
            webSocket = null;
        }
        
        isConnected = false;
    }
    
    /**
     * Mark notification as read
     * @param notificationId ID of the notification
     */
    public void markAsRead(String notificationId) {
        if (!isConnected || webSocket == null) {
            Log.w(TAG, "Cannot mark as read: Not connected");
            return;
        }
        
        try {
            String message = gson.toJson(new MarkAsReadMessage(notificationId));
            webSocket.send(message);
            Log.d(TAG, "✅ Marked as read: " + notificationId);
        } catch (Exception e) {
            Log.e(TAG, "Error marking as read", e);
        }
    }
    
    /**
     * Send ping to keep connection alive
     */
    private void sendPing() {
        if (!isConnected || webSocket == null) {
            return;
        }
        
        try {
            webSocket.send("{\"type\":\"ping\"}");
            Log.d(TAG, "📡 Ping sent");
            
            // Schedule next ping
            pingHandler.postDelayed(this::sendPing, PING_INTERVAL);
        } catch (Exception e) {
            Log.e(TAG, "Error sending ping", e);
        }
    }
    
    /**
     * Handle incoming notification
     */
    private void handleNotification(NotificationPayload notification) {
        if (notification == null || notification.getId() == null) {
            Log.w(TAG, "Invalid notification received");
            return;
        }
        
        // Check for duplicates
        if (receivedNotificationIds.contains(notification.getId())) {
            Log.d(TAG, "⚠️ Duplicate notification ignored: " + notification.getId());
            return;
        }
        
        // Add to deduplication set
        receivedNotificationIds.add(notification.getId());
        
        // Limit deduplication set size
        if (receivedNotificationIds.size() > MAX_DEDUP_SIZE) {
            receivedNotificationIds.clear();
        }
        
        Log.d(TAG, "📬 New notification: " + notification.getTitle());
        
        // Notify listener on main thread
        if (notificationListener != null) {
            new Handler(Looper.getMainLooper()).post(() -> 
                notificationListener.onNotificationReceived(notification)
            );
        }
    }
    
    /**
     * Schedule reconnection with exponential backoff
     */
    private void scheduleReconnect() {
        if (!shouldReconnect) {
            Log.d(TAG, "Reconnection disabled");
            return;
        }
        
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            Log.e(TAG, "❌ Max reconnection attempts reached");
            return;
        }
        
        reconnectAttempts++;
        
        // Exponential backoff
        long delay = Math.min(
            INITIAL_RECONNECT_DELAY * (long) Math.pow(2, reconnectAttempts - 1),
            MAX_RECONNECT_DELAY
        );
        
        Log.d(TAG, "🔄 Scheduling reconnect #" + reconnectAttempts + " in " + delay + "ms");
        
        reconnectRunnable = () -> {
            if (shouldReconnect && !isConnected) {
                connect(currentToken);
            }
        };
        
        reconnectHandler.postDelayed(reconnectRunnable, delay);
    }
    
    /**
     * WebSocket listener
     */
    private final WebSocketListener webSocketListener = new WebSocketListener() {
        
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            isConnected = true;
            reconnectAttempts = 0;
            Log.d(TAG, "✅ WebSocket connected");
            
            // Start ping
            pingHandler.postDelayed(() -> sendPing(), PING_INTERVAL);
            
            // TODO: Send subscribe message if userId is available
            // This will be done when we integrate with App.java
        }
        
        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.d(TAG, "📨 Message received: " + text);
            
            try {
                WebSocketMessage message = gson.fromJson(text, WebSocketMessage.class);
                
                if (message == null || message.getType() == null) {
                    Log.w(TAG, "Invalid message format");
                    return;
                }
                
                switch (message.getType()) {
                    case "notification":
                        if (message.getPayload() != null) {
                            handleNotification(message.getPayload());
                        }
                        break;
                        
                    case "pong":
                        Log.d(TAG, "📡 Pong received");
                        break;
                        
                    case "subscribe-success":
                        Log.d(TAG, "✅ Subscribed successfully");
                        break;
                        
                    case "error":
                        Log.e(TAG, "❌ Error from server: " + message.getMessage());
                        break;
                        
                    default:
                        Log.d(TAG, "Unknown message type: " + message.getType());
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error parsing message", e);
            }
        }
        
        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            Log.d(TAG, "📨 Binary message received (ignored)");
        }
        
        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            Log.d(TAG, "🔴 WebSocket closing: " + code + " - " + reason);
        }
        
        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            isConnected = false;
            pingHandler.removeCallbacksAndMessages(null);
            Log.d(TAG, "🔴 WebSocket closed: " + code + " - " + reason);
            
            // Schedule reconnect
            if (shouldReconnect) {
                scheduleReconnect();
            }
        }
        
        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            isConnected = false;
            pingHandler.removeCallbacksAndMessages(null);
            Log.e(TAG, "❌ WebSocket failure: " + t.getMessage(), t);
            
            // Schedule reconnect
            if (shouldReconnect) {
                scheduleReconnect();
            }
        }
    };
    
    // Setters for callbacks
    public void setOnNotificationReceivedListener(OnNotificationReceivedListener listener) {
        this.notificationListener = listener;
    }
    
    // Getters
    public boolean isConnected() {
        return isConnected;
    }
    
    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
        
        // If already connected, send subscribe message
        if (isConnected && webSocket != null && userId != null) {
            try {
                SubscribeRequest subscribeRequest = new SubscribeRequest(userId);
                String message = gson.toJson(subscribeRequest);
                webSocket.send(message);
                Log.d(TAG, "📤 Subscribe message sent for user: " + userId);
            } catch (Exception e) {
                Log.e(TAG, "Error sending subscribe message", e);
            }
        }
    }
    
    /**
     * Helper class for mark as read message
     */
    private static class MarkAsReadMessage {
        private final String type = "mark-read";
        private final String notificationId;
        
        public MarkAsReadMessage(String notificationId) {
            this.notificationId = notificationId;
        }
    }
}
