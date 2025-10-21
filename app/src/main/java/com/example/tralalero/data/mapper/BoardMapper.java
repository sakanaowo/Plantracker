package com.example.tralalero.data.mapper;

import com.example.tralalero.data.remote.dto.board.BoardDTO;
import com.example.tralalero.domain.model.Board;

import java.util.ArrayList;
import java.util.List;

public class BoardMapper {
    
    public static Board toDomain(BoardDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return new Board(
            dto.getId(),
            dto.getProjectId(),
            dto.getName(),
            dto.getOrder()
        );
    }
    
    public static BoardDTO toDto(Board board) {
        if (board == null) {
            return null;
        }
        
        BoardDTO dto = new BoardDTO();
        dto.setId(board.getId());
        dto.setProjectId(board.getProjectId());
        dto.setName(board.getName());
        dto.setOrder(board.getOrder());
        
        return dto;
    }
    
    public static List<Board> toDomainList(List<BoardDTO> dtoList) {
        if (dtoList == null) {
            return null;
        }
        
        List<Board> boards = new ArrayList<>();
        for (BoardDTO dto : dtoList) {
            boards.add(toDomain(dto));
        }
        return boards;
    }
    
    public static List<BoardDTO> toDtoList(List<Board> boards) {
        if (boards == null) {
            return null;
        }
        
        List<BoardDTO> dtoList = new ArrayList<>();
        for (Board board : boards) {
            dtoList.add(toDto(board));
        }
        return dtoList;
    }
}
