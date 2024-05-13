create table if not exists "station" (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid(),
    country VARCHAR(255) NOT NULL,
    city varchar(255) NOT NULL,
    suburb varchar(255) NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);

create table if not exists "product_template" (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);

create table if not exists "product_price" (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid(),
    product_template TEXT REFERENCES "product_template"(id),
    price DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);

create table if not exists "product" (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid(),
    station TEXT REFERENCES "station"(id) NOT NULL,
    quantity DOUBLE PRECISION DEFAULT 0,
    daily_evaporation_quantity DOUBLE PRECISION NOT NULL,
    product_template TEXT REFERENCES "product_template"(id),
    updated_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT now()
);

create table if not exists "move" (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid(),
    type TEXT CHECK ( type = 'ENTER' OR type = 'OUTER' ),
    product TEXT REFERENCES "product"(id),
    given_quantity DOUBLE PRECISION NOT NULL,
    remaining_quantity DOUBLE PRECISION NOT NULL,
    updated_at TIMESTAMP,
    created_at timestamp default now()
);
