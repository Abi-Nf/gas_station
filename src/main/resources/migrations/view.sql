CREATE VIEW station_details AS
WITH last_move_per_day AS (
    SELECT DISTINCT ON (m.product, m.created_at::date) *
    FROM "move" m
    WHERE m.created_at::date = (
        SELECT MAX(created_at::date) FROM "move"
    )
    ORDER BY m.product, m.created_at::date, m.created_at DESC
)
SELECT
    MAX(m.created_at)::date as move_date,
    s.id as station_id,
    CONCAT(s.country, '. ', s.city, ', ', s.suburb) station_full_location,
    p.id as product_id,
    pt.name as product_name,
    m.remaining_quantity,
    COALESCE((SELECT SUM(given_quantity) FROM "move" WHERE type = 'OUTER' AND product = m.product), 0) as total_sold_quantity,
    COALESCE((SELECT SUM(given_quantity) FROM "move" WHERE type = 'ENTER' AND product = m.product), 0) as total_entered_quantity,
    (SELECT
         SUM(given_quantity * ppp.price)
     FROM "move" m2
              JOIN LATERAL (
         SELECT pp.price FROM "product_price" pp
         WHERE pp.created_at <= m2.created_at
         ORDER BY pp.created_at DESC LIMIT 1
         ) ppp ON TRUE
     WHERE type = 'OUTER' AND product = m.product
    ) as total_sales_price
FROM last_move_per_day m
         JOIN "product" p on p.id = m.product
         JOIN "station" s on s.id = p.station
         JOIN "product_template" pt on pt.id = p.product_template
GROUP BY s.id, p.id, pt.name, m.remaining_quantity, m.product, m.created_at;