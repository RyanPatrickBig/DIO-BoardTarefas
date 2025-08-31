package br.com.dio.ui;

import br.com.dio.dto.BoardColumnInfoDTO;
import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.persistence.entity.CardEntity;
import br.com.dio.service.BoardColumnQueryService;
import br.com.dio.service.BoardQueryService;
import br.com.dio.service.CardQueryService;
import br.com.dio.service.CardService;
import lombok.AllArgsConstructor;

import java.sql.SQLException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Scanner;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

@AllArgsConstructor
public class BoardMenu {

    private final Scanner scanner = new Scanner(System.in).useDelimiter("\n");

    private final BoardEntity entity;

    public void execute() {
        try {
            System.out.printf("Bem vindo ao board %s, selecione a operação desejada\n", entity.getId());
            var option = -1;
            while (option != 11) {
                System.out.println("\n1 - Criar um card");
                System.out.println("2 - Mover um card");
                System.out.println("3 - Bloquear um card");
                System.out.println("4 - Desbloquear um card");
                System.out.println("5 - Cancelar um card");
                System.out.println("6 - Ver board");
                System.out.println("7 - Ver coluna com cards");
                System.out.println("8 - Ver card");
                System.out.println("9 - Ver relatório de movimentações de um card");
                System.out.println("10 - Ver relatório de bloqueios de um card");
                System.out.println("11 - Voltar para o menu anterior");
                System.out.println("12 - Sair");
                option = scanner.nextInt();
                switch (option) {
                    case 1 -> createCard();
                    case 2 -> moveCardToNextColumn();
                    case 3 -> blockCard();
                    case 4 -> unblockCard();
                    case 5 -> cancelCard();
                    case 6 -> showBoard();
                    case 7 -> showColumn();
                    case 8 -> showCard();
                    case 9 -> showMovements();
                    case 10 -> ShowBlocks();
                    case 11 -> System.out.println("Voltando para o menu anterior");
                    case 12 -> System.exit(0);
                    default -> System.out.println("Opção inválida, informe uma opção do menu");
                }
            }
        }catch (SQLException ex){
            ex.printStackTrace();
            System.exit(0);
        }
    }

    private void showMovements() throws SQLException {
        System.out.println("Pegando informações do atual Board:");

        try (var connection = getConnection()) {
            new BoardQueryService(connection).showMovementsBoard(entity.getId())
                    .ifPresentOrElse(movements -> {
                                movements.forEach(i -> {
                                    OffsetDateTime start = i.start();
                                    Optional<OffsetDateTime> endOpt = Optional.ofNullable(i.end());

                                    if (endOpt.isPresent()) {
                                        OffsetDateTime end = endOpt.get();
                                        Duration duration = Duration.between(start, end);

                                        System.out.printf(
                                                "Card %d (%s)\nBoard %d (%s)\nInicio: %s\nFim: %s\nDuracao até ser concluido: %s\n\n",
                                                i.cardId(), i.cardName(),
                                                i.boardId(), i.boardName(),
                                                start, end, duration
                                        );
                                        i.columnTimes().forEach(c -> System.out.printf(
                                                "Coluna: %s - Tempo fixado na coluna: %s\n",
                                                c.columnName(),c.timeSpent()
                                        ));
                                    } else {
                                        System.out.printf(
                                                "Card %d (%s)\nBoard %d (%s)\nInicio: %s\nAinda em andamento (não foi concluída)\n\n",
                                                i.cardId(), i.cardName(),
                                                i.boardId(), i.boardName(),
                                                start
                                        );
                                        i.columnTimes().forEach(c -> System.out.printf(
                                                "Coluna: %s - Tempo fixado na coluna: %s\n",
                                                c.columnName(),c.timeSpent()
                                        ));
                                    }
                                });
                            }, () -> System.out.printf("Houve um imprevisto com o Board selecionado\n")
                    );
        }   catch (RuntimeException ex){
            System.out.println(ex.getMessage());
        }
    }


    private void ShowBlocks() throws SQLException {
        System.out.println("Pegando informações do atual Board:");

        try (var connection = getConnection()) {
            new BoardQueryService(connection).showBlocksBoard(entity.getId())
                    .ifPresentOrElse(blocks -> {
                                blocks.forEach(i -> {
                                    OffsetDateTime blockedAt = i.blockedAt();
                                    Optional<OffsetDateTime> unblockedAtOpt = Optional.ofNullable(i.unblockedAt());

                                    if (unblockedAtOpt.isPresent()) {
                                        OffsetDateTime unblockedAt = unblockedAtOpt.get();
                                        Duration duration = Duration.between(blockedAt,unblockedAt);

                                        System.out.printf(
                                                "Card %d (%s)\nBoard %d (%s)\n" +
                                                "Motivo do Bloqueio: %s\n" +
                                                "Motivo do Desbloqueio: %s\n" +
                                                "Duração: %s\n\n",
                                                i.cardId(), i.cardName(),
                                                i.boardId(), i.boardName(),
                                                i.blockReason(), i.unblockReason(),
                                                duration
                                        );
                                    } else {
                                        System.out.printf(
                                                "Card %d (%s)\nBoard %d (%s)\n" +
                                                        "Motivo do Bloqueio: %s\n" +
                                                        "Ainda não desbloqueado\n\n",
                                                i.cardId(), i.cardName(),
                                                i.boardId(), i.boardName(),
                                                i.blockReason()
                                        );
                                    }
                                });
                            }, () -> System.out.print("Não existem bloqueios no Board no momento\n")
                    );
        }   catch (RuntimeException ex){
            System.out.println(ex.getMessage());
        }
    }

    private void createCard() throws SQLException{
        var card = new CardEntity();
        System.out.println("Informe o título do card");
        card.setTitle(scanner.next());
        System.out.println("Informe a descrição do card");
        card.setDescription(scanner.next());
        card.setBoardColumn(entity.getInitialColumn());
        try(var connection = getConnection()){
            new CardService(connection).create(card);
        }
    }

    private void moveCardToNextColumn() throws SQLException {
        System.out.println("Informe o id do card que deseja mover para a próxima coluna");
        var cardId = scanner.nextLong();
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();
        try(var connection = getConnection()){
            new CardService(connection).moveToNextColumn(cardId, boardColumnsInfo);
        } catch (RuntimeException ex){
            System.out.println(ex.getMessage());
        }
    }

    private void blockCard() throws SQLException {
        System.out.println("Informe o id do card que será bloqueado");
        var cardId = scanner.nextLong();
        System.out.println("Informe o motivo do bloqueio do card");
        var reason = scanner.next();
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();
        try(var connection = getConnection()){
            new CardService(connection).block(cardId, reason, boardColumnsInfo);
        } catch (RuntimeException ex){
            System.out.println(ex.getMessage());
        }
    }

    private void unblockCard() throws SQLException {
        System.out.println("Informe o id do card que será desbloqueado");
        var cardId = scanner.nextLong();
        System.out.println("Informe o motivo do desbloqueio do card");
        var reason = scanner.next();
        try(var connection = getConnection()){
            new CardService(connection).unblock(cardId, reason);
        } catch (RuntimeException ex){
            System.out.println(ex.getMessage());
        }
    }

    private void cancelCard() throws SQLException {
        System.out.println("Informe o id do card que deseja mover para a coluna de cancelamento");
        var cardId = scanner.nextLong();
        var cancelColumn = entity.getCancelColumn();
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();
        try(var connection = getConnection()){
            new CardService(connection).cancel(cardId, cancelColumn.getId(), boardColumnsInfo);
        } catch (RuntimeException ex){
            System.out.println(ex.getMessage());
        }
    }

    private void showBoard() throws SQLException {
        try(var connection = getConnection()){
            var optional = new BoardQueryService(connection).showBoardDetails(entity.getId());
            optional.ifPresent(b -> {
                System.out.printf("Board [%s,%s]\n", b.id(), b.name());
                b.columns().forEach(c ->
                        System.out.printf("Coluna [%s] tipo: [%s] tem %s cards\n", c.name(), c.kind(), c.cardsAmount())
                );
            });
        }
    }

    private void showColumn() throws SQLException {
        var columnsIds = entity.getBoardColumns().stream().map(BoardColumnEntity::getId).toList();
        var selectedColumnId = -1L;
        while (!columnsIds.contains(selectedColumnId)){
            System.out.printf("Escolha uma coluna do board %s pelo id\n", entity.getName());
            entity.getBoardColumns().forEach(c -> System.out.printf("%s - %s [%s]\n", c.getId(), c.getName(), c.getKind()));
            selectedColumnId = scanner.nextLong();
        }
        try(var connection = getConnection()){
            var column = new BoardColumnQueryService(connection).findById(selectedColumnId);
            column.ifPresent(co -> {
                System.out.printf("Coluna %s tipo %s\n", co.getName(), co.getKind());
                co.getCards().forEach(ca -> System.out.printf("Card %s - %s\nDescrição: %s",
                        ca.getId(), ca.getTitle(), ca.getDescription()));
            });
        }
    }

    private void showCard() throws SQLException {
        System.out.println("Informe o id do card que deseja visualizar");
        var selectedCardId = scanner.nextLong();
        try(var connection  = getConnection()){
            new CardQueryService(connection).findById(selectedCardId)
                    .ifPresentOrElse(
                            c -> {
                                System.out.printf("Card %s - %s.\n", c.id(), c.title());
                                System.out.printf("Descrição: %s\n", c.description());
                                System.out.println(c.blocked() ?
                                        "Está bloqueado. Motivo: " + c.blockReason() :
                                        "Não está bloqueado");
                                System.out.printf("Já foi bloqueado %s vezes\n", c.blocksAmount());
                                System.out.printf("Está no momento na coluna %s - %s\n", c.columnId(), c.columnName());
                            },
                            () -> System.out.printf("Não existe um card com o id %s\n", selectedCardId));
        }
    }

}
