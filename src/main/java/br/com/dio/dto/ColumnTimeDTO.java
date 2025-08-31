package br.com.dio.dto;

import java.time.Duration;

public record ColumnTimeDTO (String columnName,
                             Duration timeSpent // ou Timestamp se preferir
){
}
