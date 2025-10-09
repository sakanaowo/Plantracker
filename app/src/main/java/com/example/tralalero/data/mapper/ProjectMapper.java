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

        return new Project(
                dto.getId(),
                dto.getWorkspaceId(),
                dto.getName(),
                dto.getDescription(),
                dto.getKey(),
                dto.getBoardType()
        );
    }

    public static ProjectDTO toDTO(Project project) {
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

        return dto;
    }

    public static List<Project> toDomainList(List<ProjectDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }

        List<Project> projects = new ArrayList<>();
        for (ProjectDTO dto : dtos) {
            Project project = toDomain(dto);
            if (project != null) {
                projects.add(project);
            }
        }
        return projects;
    }

    public static List<ProjectDTO> toDTOList(List<Project> projects) {
        if (projects == null) {
            return new ArrayList<>();
        }

        List<ProjectDTO> dtos = new ArrayList<>();
        for (Project project : projects) {
            ProjectDTO dto = toDTO(project);
            if (dto != null) {
                dtos.add(dto);
            }
        }
        return dtos;
    }
}
