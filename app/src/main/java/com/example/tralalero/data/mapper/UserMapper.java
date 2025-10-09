package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.auth.UserDto;
import com.example.tralalero.domain.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserMapper {
    public static User toDomain(UserDto dto) {
        if (dto == null) {
            return null;
        }

        return new User(
                dto.id,
                dto.name,
                dto.email,
                dto.avatarUrl,
                dto.firebaseUid
        );
    }

    public static UserDto toDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.id = user.getId();
        dto.name = user.getName();
        dto.email = user.getEmail();
        dto.avatarUrl = user.getAvatarUrl();
        dto.firebaseUid = user.getFirebaseUid();

        return dto;
    }

    public static List<User> toDomainList(List<UserDto> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }

        List<User> users = new ArrayList<>();
        for (UserDto dto : dtos) {
            User user = toDomain(dto);
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }
}
