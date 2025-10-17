package com.example.tralalero.data.mapper;

import com.example.tralalero.data.local.database.entity.ProjectEntity;
import com.example.tralalero.domain.model.Project;

import java.util.ArrayList;
import java.util.List;

public class ProjectEntityMapper {
    
    public static ProjectEntity toEntity(Project project) {
        if (project == null) {
            return null;
        }
        
        // Convert domain Project to Room entity
        // Note: Some fields like color, userId, createdAt, updatedAt are not in domain model
        // We'll use default/empty values for Room-specific fields
        return new ProjectEntity(
            parseId(project.getId()),
            project.getName(),
            project.getDescription(),
            null, // color - not in domain model
            parseId(project.getWorkspaceId()),
            null, // userId - not in domain model
            null, // createdAt - not in domain model
            null  // updatedAt - not in domain model
        );
    }
    
    public static Project toDomain(ProjectEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // Convert Room entity to domain Project
        // Map available fields, use empty string for missing fields
        return new Project(
            String.valueOf(entity.getId()),
            String.valueOf(entity.getWorkspaceId()),
            entity.getName(),
            entity.getDescription(),
            "", // key - not in entity, would come from API
            "KANBAN" // boardType - default to KANBAN, not stored in entity
        );
    }
    
    private static int parseId(String id) {
        if (id == null || id.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public static List<ProjectEntity> toEntityList(List<Project> projects) {
        if (projects == null) {
            return null;
        }
        
        List<ProjectEntity> entities = new ArrayList<>();
        for (Project project : projects) {
            entities.add(toEntity(project));
        }
        return entities;
    }
    
    public static List<Project> toDomainList(List<ProjectEntity> entities) {
        if (entities == null) {
            return null;
        }
        
        List<Project> projects = new ArrayList<>();
        for (ProjectEntity entity : entities) {
            projects.add(toDomain(entity));
        }
        return projects;
    }
}
