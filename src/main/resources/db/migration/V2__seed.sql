insert into categories (slug, name, sort_order) values
                                                    ('cakes', 'Cakes', 1),
                                                    ('sweets', 'Sweets', 2)
    on conflict (slug) do nothing;

-- find ids
with c as (
    select id, slug from categories where slug in ('cakes','sweets')
)
insert into products (category_id, slug, name, description, price_cents, is_active)
values
  ((select id from c where slug='cakes'), 'chocolate-cake', 'Chocolate Cake', 'Rich cocoa sponge with ganache', 2600, true),
  ((select id from c where slug='cakes'), 'strawberry-cake', 'Strawberry Cake', 'Fresh berries & cream', 2800, true),
  ((select id from c where slug='sweets'), 'macarons-box', 'Macarons (Box of 6)', 'Assorted flavors', 1200, true)
on conflict (slug) do nothing;