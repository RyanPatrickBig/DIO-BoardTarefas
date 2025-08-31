package br.com.dio.persistence.dao;

import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import static br.com.dio.persistence.converter.OffsetDateTimeConverter.toTimestamp;

@AllArgsConstructor
public class MovementsRegisterDAO {

    private final Connection connection;

    public void move(final Long columnId, final Long cardId) throws SQLException {
        var sql = "INSERT INTO MOVEMENTS_REGISTER " +
                "(card_id, column_id, moved_at) VALUES (?, ?, ?);";
        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1, cardId);
            statement.setLong(2, columnId);
            statement.setTimestamp(3, toTimestamp(OffsetDateTime.now()));
            statement.executeUpdate();
        }
    }

    public void remove(final Long cardId) throws SQLException{
        var sql = "UPDATE MOVEMENTS_REGISTER " +
                "SET removed_at = ?" +
                "WHERE card_id = ? AND removed_at IS NULL;";
        try(var statement = connection.prepareStatement(sql)){
            statement.setTimestamp(1, toTimestamp(OffsetDateTime.now()));
            statement.setLong(2, cardId);
            statement.executeUpdate();
        }
    }


}
