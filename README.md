# Projeto de board para gerenciamento de tarefas

Objetivo primário: Escreva um código que irá criar um board customizável para acompanhamento de tarefas

## Requisitos opcionais que busquei adicionar ao projeto
    1 - Um card deve armazenar a data e hora em que foi colocado em uma coluna e a data e hora que foi movido pra a próxima coluna;
    2 - O código deve gerar um relatório do board selecionado com o tempo que cada tarefa demorou para ser concluída com informações do tempo que levou em cada coluna
    3 - O código dever gerar um relatório do board selecionado com o os bloqueios dos cards, com o tempo que ficaram bloqueados e com a justificativa dos bloqueios e desbloqueios.

## Diagrama com as adições utilizadas 
```mermaid
---
title: Diagrama Mermaid
---
classDiagram
    class Board {
        +long id
        +string name
    }

    class BoardColumn {
        +long id
        +string name
        +string kind
        +int order
    }

    class Card {
        +long id
        +string title
        +string description
        +OffsetDateTime createdAt
    }

    class Block {
        +long id
        +string blockCause
        +OffsetDateTime blockOn
        +string unblockCause
        +OffsetDateTime unblockOn
    }
    
    class Movements_Register {
        +long id
        +OffSetDateTime register
    }

    class Blocks_Register {
        +long id
        +OffSetDateTime register
    }

    Board "1" -- "N" BoardColumn
    BoardColumn "1" -- "N" Card
    Card "1" -- "0" Block
    Card "N" -- "N" Movements_Register 
    Movements_Register "N" -- "N" BoardColumn
    Card "N" -- "N" Blocks_Register
    Blocks_Register "N" -- "N" BoardColumn

```