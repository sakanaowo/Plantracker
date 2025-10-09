package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.workspace.WorkspaceDTO;
import com.example.tralalero.domain.model.Workspace;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for converting between WorkspaceDTO and Workspace domain model
 */
public class WorkspaceMapper {

    /**
     * Convert WorkspaceDTO (from API) to Workspace (Domain Model)
     */
    public static Workspace toDomain(WorkspaceDTO dto) {
        if (dto == null) {
            return null;
        }

        return new Workspace(
                dto.getId(),
                dto.getName(),
                dto.getType(),
                dto.getOwnerId()
        );
    }

    /**
     * Convert Workspace (Domain Model) to WorkspaceDTO (for API request)
     */
    public static WorkspaceDTO toDTO(Workspace workspace) {
        if (workspace == null) {
            return null;
        }

        WorkspaceDTO dto = new WorkspaceDTO();
        dto.setId(workspace.getId());
        dto.setName(workspace.getName());
        dto.setType(workspace.getType());
        dto.setOwnerId(workspace.getOwnerId());

        return dto;
    }

    /**
     * Convert list of WorkspaceDTO to list of Workspace
     */
    public static List<Workspace> toDomainList(List<WorkspaceDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }

        List<Workspace> workspaces = new ArrayList<>();
        for (WorkspaceDTO dto : dtos) {
            Workspace workspace = toDomain(dto);
            if (workspace != null) {
                workspaces.add(workspace);
            }
        }
        return workspaces;
    }

    /**
     * Convert list of Workspace to list of WorkspaceDTO
     */
    public static List<WorkspaceDTO> toDTOList(List<Workspace> workspaces) {
        if (workspaces == null) {
            return new ArrayList<>();
        }

        List<WorkspaceDTO> dtos = new ArrayList<>();
        for (Workspace workspace : workspaces) {
            WorkspaceDTO dto = toDTO(workspace);
            if (dto != null) {
                dtos.add(dto);
            }
        }
        return dtos;
    }
}
