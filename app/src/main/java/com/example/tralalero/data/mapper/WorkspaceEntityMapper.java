package com.example.tralalero.data.mapper;

import com.example.tralalero.data.local.database.entity.WorkspaceEntity;
import com.example.tralalero.domain.model.Workspace;

import java.util.ArrayList;
import java.util.List;

public class WorkspaceEntityMapper {
    
    public static WorkspaceEntity toEntity(Workspace workspace) {
        if (workspace == null) {
            return null;
        }
        
        // FIXED: Updated to use ownerId and type fields
        return new WorkspaceEntity(
            workspace.getId(),
            workspace.getName(),
            null, // description - not in domain model, keep for backward compatibility
            workspace.getOwnerId(), // FIXED: Now correctly maps ownerId
            workspace.getType() != null ? workspace.getType() : "TEAM", // FIXED: Map type field
            null, // createdAt - not in domain model
            null  // updatedAt - not in domain model
        );
    }
    
    public static Workspace toDomain(WorkspaceEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // FIXED: Updated to use ownerId and type from entity
        return new Workspace(
            entity.getId(),
            entity.getName(),
            entity.getType(), // FIXED: Get type from entity
            entity.getOwnerId() // FIXED: Get ownerId (was getUserId)
        );
    }
    
    public static List<WorkspaceEntity> toEntityList(List<Workspace> workspaces) {
        if (workspaces == null) {
            return null;
        }
        
        List<WorkspaceEntity> entities = new ArrayList<>();
        for (Workspace workspace : workspaces) {
            entities.add(toEntity(workspace));
        }
        return entities;
    }
    
    public static List<Workspace> toDomainList(List<WorkspaceEntity> entities) {
        if (entities == null) {
            return null;
        }
        
        List<Workspace> workspaces = new ArrayList<>();
        for (WorkspaceEntity entity : entities) {
            workspaces.add(toDomain(entity));
        }
        return workspaces;
    }
}
