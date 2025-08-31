--liquibase formatted sql
--changeset ryan:202508301118
--comment: register card movements

CREATE TABLE MOVEMENTS_REGISTER(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id BIGINT NOT NULL,
    column_id BIGINT NOT NULL,
    moved_at TIMESTAMP NOT NULL,
    removed_at TIMESTAMP NULL,
    CONSTRAINT card_id_fk FOREIGN KEY (card_id) REFERENCES CARDS(id) ON DELETE CASCADE,
    CONSTRAINT column_id_fk FOREIGN KEY (column_id) REFERENCES BOARDS_COLUMNS(id) ON DELETE CASCADE
) ENGINE=InnoDB;

--rollback DROP TABLE MOVEMENTS_REGISTER