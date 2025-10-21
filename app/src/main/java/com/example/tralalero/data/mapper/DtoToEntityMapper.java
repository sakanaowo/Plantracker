package com.example.tralalero.data.mapper;

import com.example.tralalero.data.local.database.entity.ProjectEntity;
import com.example.tralalero.data.local.database.entity.WorkspaceEntity;
import com.example.tralalero.data.remote.dto.project.ProjectDTO;
import com.example.tralalero.data.remote.dto.workspace.WorkspaceDTO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Direct mapper from API DTOs to Room Entities
 * Preserves all fields including those not in domain models (issueSeq, timestamps, etc.)
 *
 * Use this when caching API responses to avoid data loss through domain model conversion
 */
public class DtoToEntityMapper {

    private static final SimpleDateFormat ISO_DATE_FORMAT;

    static {
        ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    // ==================== PROJECT MAPPING ====================

    /**
     * Convert ProjectDTO to ProjectEntity
     * Preserves issueSeq, createdAt, updatedAt from API
     */
    public static ProjectEntity projectDtoToEntity(ProjectDTO dto) {
        if (dto == null) {
            return null;
        }

        ProjectEntity entity = new ProjectEntity(
            dto.getId(),
            dto.getWorkspaceId(),
            dto.getName(),
            dto.getKey(),
            dto.getBoardType() != null ? dto.getBoardType() : "KANBAN"
        );

        entity.setDescription(dto.getDescription());
        entity.setIssueSeq(dto.getIssueSeq()); // Preserve from API
        entity.setCreatedAt(parseIsoDate(dto.getCreatedAt())); // Preserve from API
        entity.setUpdatedAt(parseIsoDate(dto.getUpdatedAt())); // Preserve from API

        return entity;
    }

    /**
     * Convert list of ProjectDTO to ProjectEntity
     */
    public static List<ProjectEntity> projectDtoListToEntityList(List<ProjectDTO> dtoList) {
        if (dtoList == null) {
            return new ArrayList<>();
        }

        List<ProjectEntity> entities = new ArrayList<>();
        for (ProjectDTO dto : dtoList) {
            ProjectEntity entity = projectDtoToEntity(dto);
            if (entity != null) {
                entities.add(entity);
            }
        }
        return entities;
    }

    // ==================== WORKSPACE MAPPING ====================

    /**
     * Convert WorkspaceDTO to WorkspaceEntity
     * Preserves ownerId, type, timestamps from API
     */
    public static WorkspaceEntity workspaceDtoToEntity(WorkspaceDTO dto) {
        if (dto == null) {
            return null;
        }

        return new WorkspaceEntity(
            dto.getId(),
            dto.getName(),
            null, // description - not in DTO
            dto.getOwnerId(), // Preserve from API
            dto.getType() != null ? dto.getType() : "TEAM", // Preserve from API
            parseIsoDate(dto.getCreatedAt()), // Preserve from API
            parseIsoDate(dto.getUpdatedAt())  // Preserve from API
        );
    }

    /**
     * Convert list of WorkspaceDTO to WorkspaceEntity
     */
    public static List<WorkspaceEntity> workspaceDtoListToEntityList(List<WorkspaceDTO> dtoList) {
        if (dtoList == null) {
            return new ArrayList<>();
        }

        List<WorkspaceEntity> entities = new ArrayList<>();
        for (WorkspaceDTO dto : dtoList) {
            WorkspaceEntity entity = workspaceDtoToEntity(dto);
            if (entity != null) {
                entities.add(entity);
            }
        }
        return entities;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Parse ISO 8601 date string to Date object
     * Returns null if parsing fails or input is null
     */
    private static Date parseIsoDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            return ISO_DATE_FORMAT.parse(dateString);
        } catch (ParseException e) {
            // Try alternative formats
            try {
                // Format with milliseconds: 2024-01-01T12:00:00.123Z
                SimpleDateFormat altFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                altFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                return altFormat.parse(dateString);
            } catch (ParseException ex) {
                // If all parsing fails, return null
                return null;
            }
        }
    }
}

