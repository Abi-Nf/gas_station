package app.station.services;

import app.station.models.Product;
import app.station.models.ProductPrice;
import app.station.models.ProductTemplate;
import app.station.models.Station;
import app.station.repository.ProductPriceRepository;
import app.station.repository.ProductRepository;
import app.station.repository.ProductTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
  private final ProductRepository productRepository;
  private final ProductPriceRepository productPriceRepository;
  private final ProductTemplateRepository productTemplateRepository;

  public List<Product> getAll() {
    return productRepository.findAll();
  }

  public Product getById(String id) {
    return productRepository
      .findById(id)
      .orElseThrow(() -> new ResponseStatusException(
        HttpStatus.NOT_FOUND,
        "Product not found"
      ));
  }

  public Product deleteById(String id){
    return productRepository.deleteById(id);
  }

  public ProductPrice setNewProductPrice(String templateId, Double price) {
    return productPriceRepository.save(ProductPrice
      .builder()
        .productTemplate(ProductTemplate
          .builder()
          .id(templateId)
          .build())
        .price(price)
      .build());
  }

  public List<Product> createProductsForStation(String stationId){
    List<Product> products = new ArrayList<>();
    for (ProductTemplate productTemplate : productTemplateRepository.findAll()) {
      Product product = productRepository.save(Product
        .builder()
          .station(Station
            .builder()
            .id(stationId)
            .build()
          )
          .productTemplate(productTemplate)
          .dailyEvaporationQuantity(Math.random() * 200)
        .build());
      products.add(product);
    }
    return products;
  }
}
