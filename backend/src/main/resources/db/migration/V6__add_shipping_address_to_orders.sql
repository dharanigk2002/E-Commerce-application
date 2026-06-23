ALTER TABLE orders
    ADD COLUMN shipping_address_line VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN shipping_city VARCHAR(120) NOT NULL DEFAULT '',
    ADD COLUMN shipping_state VARCHAR(120) NOT NULL DEFAULT '',
    ADD COLUMN shipping_postal_code VARCHAR(30) NOT NULL DEFAULT '',
    ADD COLUMN shipping_country VARCHAR(120) NOT NULL DEFAULT '';
