package app.station.controllers;

import app.station.models.Product;
import app.station.models.ProductPrice;
import app.station.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {
  private final ProductService productService;

  @GetMapping
  public List<Product> getAllProducts(){
    return productService.getAll();
  }

  @GetMapping("/{id}")
  public Product getProductById(
    @PathVariable String id
  ){
    return productService.getById(id);
  }

  @PostMapping("/{stationId}")
  public List<Product> createStationProduct(
    @PathVariable String stationId
  ){
    return productService.createProductsForStation(stationId);
  }

  @DeleteMapping("/{id}")
  public Product deleteProductById(@PathVariable String id){
    return productService.deleteById(id);
  }

  @PatchMapping("/{templateId}/price")
  public ProductPrice updateProductPrice(
    @PathVariable String templateId,
    @RequestParam Double price
  ){
    return productService.setNewProductPrice(templateId, price);
  }
}
