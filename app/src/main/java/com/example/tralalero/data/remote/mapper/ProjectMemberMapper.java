package com.example.tralalero.data.remote.mapper;

import com.example.tralalero.data.remote.dto.project.ProjectMemberDTO;
import com.example.tralalero.domain.model.ProjectMember;

public class ProjectMemberMapper {
    
    public static ProjectMember toDomain(ProjectMemberDTO dto) {
        if (dto == null || dto.getUsers() == null) {
            return null;
        }
        
        ProjectMemberDTO.UserInfo user = dto.getUsers();
        
        // Use userId from DTO if available, otherwise use user.id
        String userId = dto.getUserId() != null ? dto.getUserId() : user.getId();
        
        return new ProjectMember(
                dto.getId(),
                userId,  // ✅ Use fallback logic
                user.getName(),
                user.getEmail(),
                user.getAvatarUrl(),
                dto.getRole(),
                user.getFirebaseUid()
        );
    }
}
