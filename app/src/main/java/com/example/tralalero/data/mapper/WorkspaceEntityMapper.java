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
        
        // Convert domain Workspace to Room entity
        // Entity has different structure (description, userId, dates)
        return new WorkspaceEntity(
            parseId(workspace.getId()),
            workspace.getName(),
            null, // description - not in domain model
            workspace.getOwnerId(), // userId -> ownerId
            null, // createdAt - not in domain model
            null  // updatedAt - not in domain model
        );
    }
    
    public static Workspace toDomain(WorkspaceEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // Convert Room entity to domain Workspace (4 parameters)
        return new Workspace(
            String.valueOf(entity.getId()),
            entity.getName(),
            "TEAM", // type - default to TEAM, not stored in entity
            entity.getUserId() // ownerId
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
