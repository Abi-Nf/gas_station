package app.station.services;

import app.station.DTO.BuyProductPayload;
import app.station.DTO.StationMoveDetail;
import app.station.models.Move;
import app.station.models.MoveType;
import app.station.models.Product;
import app.station.models.ProductPrice;
import app.station.repository.MoveRepository;
import app.station.repository.ProductPriceRepository;
import app.station.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MoveService {
  private final MoveRepository moveRepository;
  private final ProductRepository productRepository;
  private final ProductPriceRepository productPriceRepository;

  public List<Move> getStationMovesByTimeInterval(
    String stationId,
    LocalDate startDate,
    LocalDate endDate
  ) {
    return moveRepository.findAllByStationIdAndInterval(
      stationId,
      startDate,
      endDate
    );
  }

  public List<StationMoveDetail> getStationDetails(
    String stationId,
    LocalDate startDate,
    LocalDate endDate
  ) {
    return moveRepository.findStationDetailsWithIntervals(stationId, startDate, endDate);
  }

  public Move supplyProduct(String productId, Double quantity, LocalDate date) {
    if(date == null)
      return moveRepository.saveNewProductQuantity(productId, quantity);
    return moveRepository.saveNewProductQuantityAtDate(productId, quantity, date);
  }

  public Move buyProduct(BuyProductPayload payload) {
    Product product = productRepository
      .findByIdAndCheckEvaporation(payload.getProductId())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    Double requiredQuantity = getRequiredQuantity(product, payload);
    product = productRepository.subtractQuantityById(product.getId(), requiredQuantity);
    return moveRepository.save(Move
      .builder()
      .type(MoveType.OUTER)
      .product(product)
      .givenQuantity(requiredQuantity)
      .remainingQuantity(product.getQuantity())
      .build());
  }

  private Double getRequiredQuantity(Product product, BuyProductPayload  payload){
    Double remainingQuantity = product.getQuantity();
    if(remainingQuantity == 0){
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Not enough fuel");
    }
    ProductPrice productPrice = productPriceRepository
      .findLatestByTemplate(product.getProductTemplate().getId())
      .orElseThrow(() -> new ResponseStatusException(
        HttpStatus.EXPECTATION_FAILED, "No price attributed to this product"
      ));
    return getQuantityValue(payload, productPrice, remainingQuantity);
  }

  private Double getQuantityValue(
    BuyProductPayload payload,
    ProductPrice productPrice,
    Double remainingQuantity
  ) {
    double requiredQuantity;
    if(payload.getWithQuantity() != null){
      requiredQuantity = payload.getWithQuantity();
    }else if(productPrice.getPrice() != null){
      Double unitPrice = productPrice.getPrice();
      requiredQuantity = payload.getWithPrice() / unitPrice;
    }else
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Specify how much you need");
    if(requiredQuantity > remainingQuantity)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough fuel to sell");
    if(requiredQuantity > Product.MAX_AMOUNT_TO_BUY)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You exceed maximum quantity to buy");
    return requiredQuantity;
  }
}
