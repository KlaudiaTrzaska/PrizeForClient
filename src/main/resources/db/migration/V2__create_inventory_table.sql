CREATE TABLE inventory (
                           id VARCHAR(255) PRIMARY KEY,
                           prize_amount INT,
                           FOREIGN KEY (id) REFERENCES prizes(id) ON DELETE CASCADE
);
