ALTER TABLE users
    ADD COLUMN shipping_address_line VARCHAR(255),
    ADD COLUMN shipping_city VARCHAR(120),
    ADD COLUMN shipping_state VARCHAR(120),
    ADD COLUMN shipping_postal_code VARCHAR(30),
    ADD COLUMN shipping_country VARCHAR(120);
