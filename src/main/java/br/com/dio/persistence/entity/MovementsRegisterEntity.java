package br.com.dio.persistence.entity;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class MovementsRegisterEntity {
    private int id;
    private int cardId;
    private int columnId;
    private OffsetDateTime MovedAt;
    private OffsetDateTime RemovedAt;
}
