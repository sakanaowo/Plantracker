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

    public static BoardDTO toDTO(Board board) {
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

    public static List<Board> toDomainList(List<BoardDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }

        List<Board> boards = new ArrayList<>();
        for (BoardDTO dto : dtos) {
            Board board = toDomain(dto);
            if (board != null) {
                boards.add(board);
            }
        }
        return boards;
    }

    public static List<BoardDTO> toDTOList(List<Board> boards) {
        if (boards == null) {
            return new ArrayList<>();
        }

        List<BoardDTO> dtos = new ArrayList<>();
        for (Board board : boards) {
            BoardDTO dto = toDTO(board);
            if (dto != null) {
                dtos.add(dto);
            }
        }
        return dtos;
    }
}
