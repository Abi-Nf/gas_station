package app.station.repository;

import app.station.models.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import persistence.CommonRepository;
import persistence.Database;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepository extends CommonRepository<Product, String> {
  private final Database database;

  @Override
  public List<Product> findAll() {
    String sql = """
      SELECT * FROM "@table" t
      LEFT JOIN "station" s ON t.station = s.id
      LEFT JOIN "product_template" pt ON pt.id = t.product_template
      """;
    return database
      .prepare(sql, Product.class)
      .all();
  }

  @Override
  public Optional<Product> findById(String s) {
    String sql = """
      SELECT * FROM "@table" t
      LEFT JOIN "station" s ON t.station = s.id
      LEFT JOIN "product_template" pt ON pt.id = t.product_template
      WHERE t.id = ?
      """;
    return database
      .prepare(sql, Product.class)
      .optional(s);
  }

  public Optional<Product> findByIdAndCheckEvaporation(String s) {
    return database.transaction(transaction -> {
      String sql = """
        WITH remaining AS (
            SELECT
                "product".id as id,
                quantity - daily_evaporation_quantity * extract(days from age(now(), coalesce(updated_at, created_at))) as value
            FROM "product"
        ) UPDATE "product" p SET
            quantity = (CASE WHEN (SELECT (remaining.value > 0) FROM remaining WHERE remaining.id = p.id) THEN (SELECT value FROM remaining WHERE remaining.id = p.id) ELSE quantity END),
            updated_at = now()
      WHERE p.id = ?
      """;
      transaction
        .prepare(sql, Product.class)
        .run(s);
      sql = """
        SELECT * FROM "@table" t
        LEFT JOIN "station" s ON t.station = s.id
        LEFT JOIN "product_template" pt ON pt.id = t.product_template
        WHERE t.id = ?
        """;
      return transaction
        .prepare(sql, Product.class)
        .optional(s);
    });
  }

  public void checkAndUpdateEvaporation(){
    String sqlEvaporationChecker = """
      WITH n AS (
          SELECT
              id,
              quantity - (daily_evaporation_quantity * extract(days from age(now(), updated_at))) as new_quantity
          FROM product
      ) UPDATE product p SET
          quantity = (
              CASE
                  WHEN (SELECT n.new_quantity > 0 FROM n WHERE p.id = n.id)
                  THEN (SELECT n.new_quantity FROM n WHERE p.id = n.id)
                  ELSE 0 END
          ),
          updated_at = now()
      """;
    database.execute(sqlEvaporationChecker);
  }
}
