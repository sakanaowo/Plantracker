package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.project.ProjectDTO;
import com.example.tralalero.domain.model.Project;

import java.util.ArrayList;
import java.util.List;

public class ProjectMapper {
    
    public static Project toDomain(ProjectDTO dto) {
        if (dto == null) {
            return null;
        }
        
        // Extract workspace name if available
        String workspaceName = null;
        if (dto.getWorkspaces() != null) {
            workspaceName = dto.getWorkspaces().getName();
        }
        
        Project project = new Project(
            dto.getId(),
            dto.getWorkspaceId(),
            dto.getName(),
            dto.getDescription(),
            dto.getKey(),
            dto.getBoardType(),
            dto.getType(),  // Add type field
            workspaceName
        );
        
        return project;
    }
    
    public static ProjectDTO toDto(Project project) {
        if (project == null) {
            return null;
        }
        
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setWorkspaceId(project.getWorkspaceId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setKey(project.getKey());
        dto.setBoardType(project.getBoardType());
        dto.setType(project.getType());  // Add type field
        
        return dto;
    }
    
    public static List<Project> toDomainList(List<ProjectDTO> dtoList) {
        if (dtoList == null) {
            return null;
        }
        
        List<Project> projects = new ArrayList<>();
        for (ProjectDTO dto : dtoList) {
            projects.add(toDomain(dto));
        }
        return projects;
    }
    
    public static List<ProjectDTO> toDtoList(List<Project> projects) {
        if (projects == null) {
            return null;
        }
        
        List<ProjectDTO> dtoList = new ArrayList<>();
        for (Project project : projects) {
            dtoList.add(toDto(project));
        }
        return dtoList;
    }
}
