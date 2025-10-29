package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.activity.ActivityLogDTO;
import com.example.tralalero.domain.model.ActivityLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper: ActivityLogDTO <-> ActivityLog (Domain Model)
 */
public class ActivityLogMapper {

    /**
     * Convert DTO to Domain Model
     */
    public static ActivityLog toDomain(ActivityLogDTO dto) {
        if (dto == null) {
            return null;
        }

        String userName = "Unknown User";
        String userAvatar = null;

        if (dto.getUsers() != null) {
            userName = dto.getUsers().getName() != null ? dto.getUsers().getName() : "Unknown User";
            userAvatar = dto.getUsers().getAvatarUrl();
        }

        return new ActivityLog(
            dto.getId(),
            dto.getUserId(),
            dto.getAction(),
            dto.getEntityType(),
            dto.getEntityName(),
            dto.getCreatedAt(),
            userName,
            userAvatar
        );
    }

    /**
     * Convert list of DTOs to Domain Models
     */
    public static List<ActivityLog> toDomainList(List<ActivityLogDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }

        List<ActivityLog> result = new ArrayList<>();
        for (ActivityLogDTO dto : dtos) {
            ActivityLog log = toDomain(dto);
            if (log != null) {
                result.add(log);
            }
        }
        return result;
    }
}
