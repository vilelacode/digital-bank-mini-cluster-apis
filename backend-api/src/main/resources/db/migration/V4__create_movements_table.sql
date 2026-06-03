CREATE TABLE movements (
    id CHAR(36) PRIMARY KEY,
    account_id CHAR(36) NOT NULL,
    transfer_id CHAR(36) NOT NULL,
    type VARCHAR(30) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_movements_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id),

    CONSTRAINT fk_movements_transfer
        FOREIGN KEY (transfer_id)
        REFERENCES transfers(id),

    CONSTRAINT chk_movements_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_movements_account_id_created_at
    ON movements(account_id, created_at DESC);

CREATE INDEX idx_movements_transfer_id
    ON movements(transfer_id);
