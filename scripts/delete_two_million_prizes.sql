\set ON_ERROR_STOP on

BEGIN;

CREATE TEMP TABLE prize_ids_to_delete ON COMMIT DROP AS
SELECT id
FROM prizes
ORDER BY id
LIMIT 2000000;

ALTER TABLE prize_ids_to_delete
ADD PRIMARY KEY (id);

DO $$
DECLARE
    selected_count BIGINT;
BEGIN
    SELECT COUNT(*) INTO selected_count
    FROM prize_ids_to_delete;

    IF selected_count <> 2000000 THEN
        RAISE EXCEPTION 'Expected 2000000 prize rows to delete, selected %', selected_count;
    END IF;
END $$;

WITH deleted_inventory AS (
    DELETE FROM inventory i
    USING prize_ids_to_delete d
    WHERE i.id = d.id
    RETURNING i.id
)
SELECT COUNT(*) AS deleted_inventory_rows
FROM deleted_inventory;

WITH deleted_prizes AS (
    DELETE FROM prizes p
    USING prize_ids_to_delete d
    WHERE p.id = d.id
    RETURNING p.id
)
SELECT COUNT(*) AS deleted_prize_rows
FROM deleted_prizes;

COMMIT;
