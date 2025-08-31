package br.com.dio.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record MovementsDetailsDTO (Long boardId,
                                   String boardName,
                                   Long cardId,
                                   String cardName,
                                   OffsetDateTime start,
                                   OffsetDateTime end,
                                   List<ColumnTimeDTO> columnTimes
){
}
