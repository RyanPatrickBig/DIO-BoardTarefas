package br.com.dio.dto;

import java.time.OffsetDateTime;

public record BoardBlocksDTO (Long boardId,
                              String boardName,

                              Long cardId,
                              String cardName,
                              OffsetDateTime blockedAt,
                              String blockReason,
                              OffsetDateTime unblockedAt,
                              String unblockReason
) {
}
