package com.example.tralalero.data.mapper;

import com.example.tralalero.data.local.database.entity.ProjectEntity;
import com.example.tralalero.domain.model.Project;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper between Project domain model and ProjectEntity
 * CRITICAL: Maps all 6 fields including key and boardType
 * CRITICAL: Uses String IDs directly (no int conversion)
 * NOTE: boardType is String in entity, not enum
 */
public class ProjectEntityMapper {
    
    /**
     * Convert domain Project to ProjectEntity
     * Maps all 6 fields: id, workspaceId, name, description, key, boardType
     */
    public static ProjectEntity toEntity(Project project) {
        if (project == null) {
            return null;
        }
        
        // Create entity with all required fields
        ProjectEntity entity = new ProjectEntity(
            project.getId(),
            project.getWorkspaceId(),
            project.getName(),
            project.getKey(),
            project.getBoardType() != null ? project.getBoardType() : "KANBAN"
        );
        
        // Set optional fields
        entity.setDescription(project.getDescription());
        
        return entity;
    }
    
    /**
     * Convert ProjectEntity to domain Project
     * Maps all 6 fields: id, workspaceId, name, description, key, boardType
     */
    public static Project toDomain(ProjectEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // Create domain Project with all 6 fields
        return new Project(
            entity.getId(),
            entity.getWorkspaceId(),
            entity.getName(),
            entity.getDescription(),
            entity.getKey(),
            entity.getBoardType() != null ? entity.getBoardType() : "KANBAN"
        );
    }
    
    /**
     * Convert list of ProjectEntities to domain Projects
     */
    public static List<Project> toDomainList(List<ProjectEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        
        List<Project> projects = new ArrayList<>();
        for (ProjectEntity entity : entities) {
            Project project = toDomain(entity);
            if (project != null) {
                projects.add(project);
            }
        }
        return projects;
    }
    
    /**
     * Convert list of domain Projects to ProjectEntities
     */
    public static List<ProjectEntity> toEntityList(List<Project> projects) {
        if (projects == null) {
            return new ArrayList<>();
        }
        
        List<ProjectEntity> entities = new ArrayList<>();
        for (Project project : projects) {
            ProjectEntity entity = toEntity(project);
            if (entity != null) {
                entities.add(entity);
            }
        }
        return entities;
    }
}
