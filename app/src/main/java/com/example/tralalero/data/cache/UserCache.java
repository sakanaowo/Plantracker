package com.example.tralalero.data.cache;

import com.example.tralalero.data.remote.dto.user.UserDTO;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple in-memory cache for user information
 */
public class UserCache {
    private static UserCache instance;
    private final Map<String, UserDTO> userCache = new HashMap<>();
    
    private UserCache() {}
    
    public static UserCache getInstance() {
        if (instance == null) {
            instance = new UserCache();
        }
        return instance;
    }
    
    public void putUser(String userId, UserDTO user) {
        userCache.put(userId, user);
    }
    
    public UserDTO getUser(String userId) {
        return userCache.get(userId);
    }
    
    public boolean hasUser(String userId) {
        return userCache.containsKey(userId);
    }
    
    public void removeUser(String userId) {
        userCache.remove(userId);
    }
    
    public void clearCache() {
        userCache.clear();
    }
}