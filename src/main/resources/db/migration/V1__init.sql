-- Basic MegyCakes schema (minimal to start)
create table categories (
                            id            bigserial primary key,
                            slug          varchar(100) unique not null,
                            name          varchar(120) not null,
                            sort_order    int not null default 0,
                            created_at    timestamp not null default now()
);

create table products (
                          id              bigserial primary key,
                          category_id     bigint not null references categories(id) on delete restrict,
                          slug            varchar(120) unique not null,
                          name            varchar(160) not null,
                          description     text,
                          price_cents     int not null,
                          is_active       boolean not null default true,
                          created_at      timestamp not null default now(),
                          updated_at      timestamp not null default now()
);

create index idx_products_category on products(category_id);
create index idx_products_active on products(is_active);

create table product_images (
                                id           bigserial primary key,
                                product_id   bigint not null references products(id) on delete cascade,
                                cloudinary_public_id varchar(255) not null,
                                width        int,
                                height       int,
                                sort_order   int not null default 0,
                                created_at   timestamp not null default now()
);

create index idx_prod_images_product on product_images(product_id);

create table orders (
                        id             bigserial primary key,
                        order_number   varchar(40) unique not null,
                        customer_email varchar(255) not null,
                        customer_name  varchar(160),
                        total_cents    int not null,
                        currency       char(3) not null default 'EUR',
                        status         varchar(30) not null default 'PENDING',
                        delivery_method varchar(30) not null default 'LOCAL_PICKUP',
                        delivery_fee_cents int not null default 0,
                        stripe_session_id varchar(255),
                        created_at     timestamp not null default now(),
                        updated_at     timestamp not null default now()
);

create table order_items (
                             id           bigserial primary key,
                             order_id     bigint not null references orders(id) on delete cascade,
                             product_id   bigint not null references products(id) on delete restrict,
                             name_snapshot varchar(160) not null,
                             price_cents_snapshot int not null,
                             quantity     int not null check (quantity > 0)
);

create index idx_order_items_order on order_items(order_id);