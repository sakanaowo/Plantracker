package com.example.tralalero.data.remote.mapper;

import com.example.tralalero.data.remote.dto.project.ProjectMemberDTO;
import com.example.tralalero.domain.model.ProjectMember;

public class ProjectMemberMapper {
    
    public static ProjectMember toDomain(ProjectMemberDTO dto) {
        if (dto == null || dto.getUsers() == null) {
            return null;
        }
        
        ProjectMemberDTO.UserInfo user = dto.getUsers();
        
        return new ProjectMember(
                dto.getId(),
                dto.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getAvatarUrl(),
                dto.getRole()
        );
    }
}
