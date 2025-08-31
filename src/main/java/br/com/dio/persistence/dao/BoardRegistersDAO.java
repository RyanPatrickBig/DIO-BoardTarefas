package br.com.dio.persistence.dao;

import br.com.dio.dto.BoardBlocksDTO;
import br.com.dio.dto.ColumnTimeDTO;
import br.com.dio.dto.MovementsDetailsDTO;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class BoardRegistersDAO {

    private final Connection connection;

    public List<MovementsDetailsDTO> findByBoardIdMovements(final Long boardId) throws SQLException {
        List<MovementsDetailsDTO> dtos = new ArrayList<>();
        var sql =
                """
                 SELECT b.id          AS board_id,
                        b.name        AS board_name,
                        cd.id         AS card_id,
                        cd.title       AS card_name,
                        
                        MIN(CASE WHEN bc.kind = 'INITIAL' THEN mv.moved_at END) AS start_time,
                        MAX(CASE WHEN bc.kind = 'FINAL' THEN mv.moved_at END) AS end_time
                        
                       FROM CARDS cd
                       JOIN MOVEMENTS_REGISTER mv ON mv.card_id = cd.id
                       JOIN BOARDS_COLUMNS bc    ON mv.column_id = bc.id
                       JOIN BOARDS b             ON bc.board_id = b.id
                      WHERE b.id = ?
                      GROUP BY b.id, b.name, cd.id, cd.title
                      ORDER BY cd.id;
                """;

        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1, boardId);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            while (resultSet.next()){
                Timestamp startNull = resultSet.getTimestamp("start_time");
                OffsetDateTime startTime = startNull != null ? startNull.toInstant().atOffset(ZoneOffset.UTC) : null;
                Timestamp endNull = resultSet.getTimestamp("end_time");
                OffsetDateTime endTime = endNull != null ? endNull.toInstant().atOffset(ZoneOffset.UTC) : null;
                var dto = new MovementsDetailsDTO(
                        resultSet.getLong("board_id"),
                        resultSet.getString("board_name"),
                        resultSet.getLong("card_id"),
                        resultSet.getString("card_name"),
                        startTime,
                        endTime,
                        findDurationByMovement(resultSet.getLong("card_id"))
                );
                dtos.add(dto);
            }
            return dtos;
        }
    }

    public List<ColumnTimeDTO> findDurationByMovement(final Long cardId) throws SQLException {
        List<ColumnTimeDTO> dtos = new ArrayList<>();
        var sql =
                """
                        SELECT bc.name        AS column_name,
                               mv.moved_at,
                               mv.removed_at,
                               bc.kind
                          FROM MOVEMENTS_REGISTER mv
                          JOIN CARDS cd          ON mv.card_id = cd.id
                          JOIN BOARDS_COLUMNS bc ON mv.column_id = bc.id
                         WHERE card_id = ?
                         ORDER BY cd.id, mv.moved_at;
                """;

        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1, cardId);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            while (resultSet.next()){
                Timestamp movedNull = resultSet.getTimestamp("mv.moved_at");
                OffsetDateTime moveTime = movedNull != null ? movedNull.toInstant().atOffset(ZoneOffset.UTC) : null;
                Timestamp removedNull = resultSet.getTimestamp("mv.removed_at");
                OffsetDateTime removeTime = removedNull != null ? removedNull.toInstant().atOffset(ZoneOffset.UTC) : null;
                Duration durationMove;
                if(removeTime != null){
                    durationMove = Duration.between(resultSet.getObject("mv.moved_at", OffsetDateTime.class),
                            resultSet.getObject("mv.removed_at", OffsetDateTime.class));
                } else if (resultSet.getString("bc.kind").equals("FINAL")){
                    durationMove = Duration.between(resultSet.getObject("mv.moved_at", OffsetDateTime.class),
                            OffsetDateTime.now(ZoneOffset.UTC));
                }else {
                    durationMove = Duration.ZERO;
                }
                var dto = new ColumnTimeDTO(
                        resultSet.getString("column_name"),
                        durationMove
                        );
                dtos.add(dto);
            }
            return dtos;
        }
    }

    public List<BoardBlocksDTO> findByBoardIdBlocks(final Long boardId) throws SQLException {
        List<BoardBlocksDTO> dtos = new ArrayList<>();
        var sql =
                """
                    SELECT
                        b.id          AS board_id,
                        b.name        AS board_name,
                        cd.id         AS card_id,
                        cd.title       AS card_name,
                    
                        bk.blocked_at,
                        bk.block_reason,
                        MAX(CASE WHEN bk.unblocked_at IS NOT NULL THEN bk.unblocked_at END) AS unblocked_at,
                        MAX(CASE WHEN bk.unblock_reason IS NOT NULL THEN bk.unblock_reason END) AS unblock_reason
                    
                    FROM CARDS cd
                    JOIN BLOCKS bk             ON bk.card_id = cd.id
                    JOIN MOVEMENTS_REGISTER mv ON mv.card_id = cd.id
                    JOIN BOARDS_COLUMNS bc     ON mv.column_id = bc.id
                    JOIN BOARDS b              ON bc.board_id = b.id
                    WHERE b.id = ?
                    GROUP BY b.id, b.name, cd.id, cd.title, bk.blocked_at, bk.block_reason
                    ORDER BY cd.id;
                """;

        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1, boardId);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            while (resultSet.next()){
                Timestamp blockedNull = resultSet.getTimestamp("blocked_at");
                OffsetDateTime blockedAt = blockedNull != null ? blockedNull.toInstant().atOffset(ZoneOffset.UTC) : null;
                Timestamp unblockedNull = resultSet.getTimestamp("unblocked_at");
                OffsetDateTime unblockedAt = unblockedNull != null ? unblockedNull.toInstant().atOffset(ZoneOffset.UTC) : null;
                var dto = new BoardBlocksDTO(
                        resultSet.getLong("board_id"),
                        resultSet.getString("board_name"),
                        resultSet.getLong("card_id"),
                        resultSet.getString("card_name"),
                        blockedAt,
                        resultSet.getString("block_reason"),
                        unblockedAt,
                        resultSet.getString("unblock_reason")
                );
                dtos.add(dto);
            }
            return dtos;
        }
    }
}
