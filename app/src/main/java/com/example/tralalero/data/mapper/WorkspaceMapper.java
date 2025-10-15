package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.workspace.WorkspaceDTO;
import com.example.tralalero.domain.model.Workspace;

import java.util.ArrayList;
import java.util.List;


public class WorkspaceMapper {


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
