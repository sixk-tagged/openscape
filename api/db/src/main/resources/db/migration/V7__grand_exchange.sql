-- Grand Exchange. Offers, history and buy-limit windows are written per character, together with
-- that character's account save (see api/grand-exchange). The book itself is held in memory.

CREATE TABLE ge_offers (
    character_id INTEGER NOT NULL,
    slot INTEGER NOT NULL,
    -- Order of placing, across all characters: the time priority of the offer.
    offer_id INTEGER NOT NULL,
    -- 0 buy, 1 sell.
    type INTEGER NOT NULL,
    obj INTEGER NOT NULL,
    price INTEGER NOT NULL,
    count INTEGER NOT NULL,
    completed_count INTEGER NOT NULL,
    completed_gold INTEGER NOT NULL,
    tax_paid INTEGER NOT NULL,
    -- 0 active, 1 completed, 2 aborted.
    status INTEGER NOT NULL,
    collect_items INTEGER NOT NULL,
    collect_coins INTEGER NOT NULL,
    PRIMARY KEY (character_id, slot),
    FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE
);

CREATE INDEX idx_ge_offers_obj ON ge_offers(obj);

-- Newest first: position 0 is the latest trade.
CREATE TABLE ge_history (
    character_id INTEGER NOT NULL,
    position INTEGER NOT NULL,
    obj INTEGER NOT NULL,
    type INTEGER NOT NULL,
    count INTEGER NOT NULL,
    gold INTEGER NOT NULL,
    tax INTEGER NOT NULL,
    PRIMARY KEY (character_id, position),
    FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE
);

-- Epoch millis of the first purchase of the window, and how many were bought in it.
CREATE TABLE ge_buy_limits (
    character_id INTEGER NOT NULL,
    obj INTEGER NOT NULL,
    window_start INTEGER NOT NULL,
    bought INTEGER NOT NULL,
    PRIMARY KEY (character_id, obj),
    FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE
);

CREATE TABLE ge_prices (
    obj INTEGER PRIMARY KEY,
    price INTEGER NOT NULL
);
