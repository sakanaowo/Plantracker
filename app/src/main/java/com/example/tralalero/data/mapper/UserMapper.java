package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.auth.UserDto;
import com.example.tralalero.domain.model.User;

public class UserMapper {
    
    public static User toDomain(UserDto dto) {
        if (dto == null) {
            return null;
        }
        
        return new User(
            dto.getId(),
            dto.getName(),
            dto.getEmail(),
            dto.getAvatarUrl(),
            dto.getFirebaseUid()
        );
    }
    
    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        
        UserDto dto = new UserDto();
        dto.id = user.getId();
        dto.name = user.getName();
        dto.email = user.getEmail();
        dto.avatarUrl = user.getAvatarUrl();
        dto.firebaseUid = user.getFirebaseUid();
        // passwordHash, createdAt, updatedAt are set by backend
        
        return dto;
    }
}
