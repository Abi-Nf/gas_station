package app.station.repository;

import app.station.models.ProductTemplate;
import org.springframework.stereotype.Repository;
import persistence.CommonRepository;

@Repository
public class ProductTemplateRepository extends CommonRepository<ProductTemplate, String> {
}
