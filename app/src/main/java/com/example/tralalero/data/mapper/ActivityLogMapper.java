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

        // Extract assigneeId from newValue for ASSIGNED action
        String assigneeId = null;
        if ("ASSIGNED".equals(dto.getAction()) && dto.getNewValue() != null) {
            try {
                if (dto.getNewValue() instanceof java.util.Map) {
                    java.util.Map<String, Object> newValueMap = (java.util.Map<String, Object>) dto.getNewValue();
                    Object assigneeIdObj = newValueMap.get("assigneeId");
                    if (assigneeIdObj != null) {
                        assigneeId = assigneeIdObj.toString();
                    }
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }

        // Extract inviter name from metadata for ADDED MEMBERSHIP action (invitation accepted)
        String inviterName = null;
        if ("ADDED".equals(dto.getAction()) && "MEMBERSHIP".equals(dto.getEntityType()) && dto.getMetadata() != null) {
            try {
                if (dto.getMetadata() instanceof java.util.Map) {
                    java.util.Map<String, Object> metadataMap = (java.util.Map<String, Object>) dto.getMetadata();
                    Object inviterNameObj = metadataMap.get("inviterName");
                    if (inviterNameObj != null) {
                        inviterName = inviterNameObj.toString();
                    }
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }

        // Extract project name
        String projectName = null;
        if (dto.getProjects() != null) {
            projectName = dto.getProjects().getName();
        }

        ActivityLog activityLog = new ActivityLog(
            dto.getId(),
            dto.getUserId(),
            dto.getAction(),
            dto.getEntityType(),
            dto.getEntityName(),
            dto.getCreatedAt(),
            userName,
            userAvatar,
            assigneeId,
            dto.getOldValue(),
            dto.getNewValue(),
            dto.getMetadata()
        );
        
        // Set additional fields from DTO
        activityLog.setEntityId(dto.getEntityId());
        activityLog.setProjectId(dto.getProjectId());
        activityLog.setWorkspaceId(dto.getWorkspaceId());
        activityLog.setBoardId(dto.getBoardId());
        activityLog.setTaskId(dto.getTaskId());
        activityLog.setProjectName(projectName);
        activityLog.setInviterName(inviterName);
        
        return activityLog;
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
