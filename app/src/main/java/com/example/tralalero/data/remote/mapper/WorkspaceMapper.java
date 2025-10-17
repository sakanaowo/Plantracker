package com.example.tralalero.data.remote.mapper;

import com.example.tralalero.data.remote.dto.workspace.WorkspaceDTO;
import com.example.tralalero.domain.model.Workspace;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class WorkspaceMapper {
    private static final SimpleDateFormat ISO_DATE_FORMAT;

    static {
        ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Converts WorkspaceDTO to Workspace domain model
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
     * Converts Workspace domain model to WorkspaceDTO
     */
    public static WorkspaceDTO toDto(Workspace workspace) {
        if (workspace == null) {
            return null;
        }

        WorkspaceDTO dto = new WorkspaceDTO();
        dto.setId(workspace.getId());
        dto.setName(workspace.getName());
        dto.setType(workspace.getType());
        dto.setOwnerId(workspace.getOwnerId());
        // Note: createdAt and updatedAt are typically set by the backend
        // and are omitted here when converting from domain to DTO
        return dto;
    }

    /**
     * Converts list of WorkspaceDTO to list of Workspace domain models
     */
    public static List<Workspace> toDomainList(List<WorkspaceDTO> dtoList) {
        if (dtoList == null) {
            return new ArrayList<>();
        }

        List<Workspace> workspaces = new ArrayList<>();
        for (WorkspaceDTO dto : dtoList) {
            Workspace workspace = toDomain(dto);
            if (workspace != null) {
                workspaces.add(workspace);
            }
        }
        return workspaces;
    }

    /**
     * Converts list of Workspace domain models to list of WorkspaceDTO
     */
    public static List<WorkspaceDTO> toDtoList(List<Workspace> workspaces) {
        if (workspaces == null) {
            return new ArrayList<>();
        }

        List<WorkspaceDTO> dtoList = new ArrayList<>();
        for (Workspace workspace : workspaces) {
            WorkspaceDTO dto = toDto(workspace);
            if (dto != null) {
                dtoList.add(dto);
            }
        }
        return dtoList;
    }

    /**
     * Formats Date object to ISO 8601 string
     * Returns null if input is null
     */
    private static String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return ISO_DATE_FORMAT.format(date);
    }
}
