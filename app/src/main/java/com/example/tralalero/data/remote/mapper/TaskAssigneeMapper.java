package com.example.tralalero.data.remote.mapper;

import com.example.tralalero.data.remote.dto.task.TaskAssigneeDTO;
import com.example.tralalero.domain.model.TaskAssignee;

public class TaskAssigneeMapper {
    
    public static TaskAssignee toDomain(TaskAssigneeDTO dto) {
        if (dto == null || dto.getUsers() == null) {
            return null;
        }
        
        TaskAssigneeDTO.UserInfo user = dto.getUsers();
        
        // Use userId from DTO if available, otherwise use user.id
        String userId = dto.getUserId() != null ? dto.getUserId() : user.getId();
        
        return new TaskAssignee(
                dto.getTaskId(),
                userId,
                user.getName(),
                user.getEmail(),
                user.getAvatarUrl(),
                dto.getAssignedAt(),
                dto.getAssignedBy()
        );
    }
}
