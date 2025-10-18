package com.example.tralalero.data.mapper;

import com.example.tralalero.data.local.database.entity.BoardEntity;
import com.example.tralalero.domain.model.Board;
import java.util.ArrayList;
import java.util.List;

public class BoardEntityMapper {

    public static BoardEntity toEntity(Board board) {
        if (board == null) {
            return null;
        }

        BoardEntity entity = new BoardEntity(
            board.getId(),
            board.getProjectId(),
            board.getName(),
            board.getOrder()
        );

        return entity;
    }

    public static Board toDomain(BoardEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Board(
            entity.getId(),
            entity.getProjectId(),
            entity.getName(),
            entity.getOrder()
        );
    }

    public static List<Board> toDomainList(List<BoardEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }

        List<Board> boards = new ArrayList<>();
        for (BoardEntity entity : entities) {
            Board board = toDomain(entity);
            if (board != null) {
                boards.add(board);
            }
        }
        return boards;
    }

    public static List<BoardEntity> toEntityList(List<Board> boards) {
        if (boards == null) {
            return new ArrayList<>();
        }

        List<BoardEntity> entities = new ArrayList<>();
        for (Board board : boards) {
            BoardEntity entity = toEntity(board);
            if (entity != null) {
                entities.add(entity);
            }
        }
        return entities;
    }
}

