package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.label.LabelDTO;
import com.example.tralalero.domain.model.Label;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for Label entity
 */
public class LabelMapper {

    public static Label toDomain(LabelDTO dto) {
        if (dto == null) {
            return null;
        }

        return new Label(
                dto.getId(),
                dto.getWorkspaceId(),
                dto.getName(),
                dto.getColor()
        );
    }

    public static LabelDTO toDTO(Label label) {
        if (label == null) {
            return null;
        }

        LabelDTO dto = new LabelDTO();
        dto.setId(label.getId());
        dto.setWorkspaceId(label.getWorkspaceId());
        dto.setName(label.getName());
        dto.setColor(label.getColor());

        return dto;
    }

    public static List<Label> toDomainList(List<LabelDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }

        List<Label> labels = new ArrayList<>();
        for (LabelDTO dto : dtos) {
            Label label = toDomain(dto);
            if (label != null) {
                labels.add(label);
            }
        }
        return labels;
    }

    public static List<LabelDTO> toDTOList(List<Label> labels) {
        if (labels == null) {
            return new ArrayList<>();
        }

        List<LabelDTO> dtos = new ArrayList<>();
        for (Label label : labels) {
            LabelDTO dto = toDTO(label);
            if (dto != null) {
                dtos.add(dto);
            }
        }
        return dtos;
    }
}

