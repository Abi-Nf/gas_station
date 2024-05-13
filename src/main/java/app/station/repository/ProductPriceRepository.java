package app.station.repository;

import app.station.models.ProductPrice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import persistence.CommonRepository;
import persistence.Database;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductPriceRepository extends CommonRepository<ProductPrice, String> {
  private final Database database;

  public Optional<ProductPrice> findLatestByTemplate(String templateId) {
    String sql = """
      SELECT * FROM "@table" t
      LEFT JOIN "product_template" pt ON pt.id = t.product_template
      WHERE t.product_template = ?
      ORDER BY created_at DESC LIMIT 1
      """;
    return database
      .prepare(sql, ProductPrice.class)
      .optional(templateId);
  }
}
