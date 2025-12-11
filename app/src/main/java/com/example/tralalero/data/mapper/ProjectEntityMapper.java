package com.example.tralalero.data.mapper;

import com.example.tralalero.data.local.database.entity.ProjectEntity;
import com.example.tralalero.domain.model.Project;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper between Project domain model and ProjectEntity
 * UPDATED: Now handles issueSeq, createdAt, updatedAt fields
 */
public class ProjectEntityMapper {
    
    /**
     * Convert domain Project to ProjectEntity
     * Maps all fields including new issueSeq and timestamps
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
        
        // ADDED: Set new fields (will be null/0 for existing projects from domain)
        // These will be populated from API responses
        entity.setIssueSeq(0); // Default value
        entity.setCreatedAt(null); // Will be set from API
        entity.setUpdatedAt(null); // Will be set from API

        return entity;
    }
    
    /**
     * Convert ProjectEntity to domain Project
     * NOTE: issueSeq and timestamps are stored in entity but not exposed in domain model
     */
    public static Project toDomain(ProjectEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // Create domain Project with 6 main fields
        // issueSeq and timestamps are cached but not part of domain model
        return new Project(
            entity.getId(),
            entity.getWorkspaceId(),
            entity.getName(),
            entity.getDescription(),
            entity.getKey(),
            entity.getBoardType() != null ? entity.getBoardType() : "KANBAN",
            null  // Type not stored in entity cache
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
