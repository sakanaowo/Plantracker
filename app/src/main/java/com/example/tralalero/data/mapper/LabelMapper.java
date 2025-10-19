package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.label.LabelDTO;
import com.example.tralalero.domain.model.Label;

import java.util.ArrayList;
import java.util.List;

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
    
    public static LabelDTO toDto(Label label) {
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
    
    public static List<Label> toDomainList(List<LabelDTO> dtoList) {
        if (dtoList == null) {
            return null;
        }
        
        List<Label> labels = new ArrayList<>();
        for (LabelDTO dto : dtoList) {
            labels.add(toDomain(dto));
        }
        return labels;
    }
    
    public static List<LabelDTO> toDtoList(List<Label> labels) {
        if (labels == null) {
            return null;
        }
        
        List<LabelDTO> dtoList = new ArrayList<>();
        for (Label label : labels) {
            dtoList.add(toDto(label));
        }
        return dtoList;
    }
}
