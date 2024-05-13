package app.station.repository;

import app.station.DTO.StationMoveDetail;
import app.station.models.Move;
import app.station.models.MoveType;
import app.station.models.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import persistence.CommonRepository;
import persistence.Database;
import persistence.core.interfaces.DatabaseTemplate;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MoveRepository extends CommonRepository<Move, String> {
  private final Database database;

  public List<Move> findAllByStationIdAndInterval(
    String stationId,
    LocalDate startDate,
    LocalDate endDate
  ) {
    String sql = """
      SELECT * FROM "move"
      LEFT JOIN "product" p on p.id = move.product
      LEFT JOIN "product_template" pt on pt.id = p.product_template
      LEFT JOIN "station" s on s.id = p.station
      WHERE p.station = ? AND move.created_at BETWEEN ? AND ?
      """;
    return database
      .prepare(sql, Move.class)
      .all(stationId, startDate, endDate);
  }

  public Move saveNewProductQuantityAtDate(
    String productId,
    Double quantity,
    LocalDate date
  ){
    return database.transaction(transaction -> {
      String sqlCheckMoves = "SELECT count(*) FROM \"move\" WHERE created_at >= ?";
      int checkDate = transaction
        .prepare(sqlCheckMoves, int.class)
        .get(date);
      if(checkDate > 0){
        throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "There are some moves after this date"
        );
      }
      Product product = updateProduct(transaction, quantity, productId);
      return save(Move
        .builder()
        .type(MoveType.ENTER)
        .product(null)
        .givenQuantity(quantity)
        .remainingQuantity(product.getQuantity())
        .build());
    });
  }

  public Move saveNewProductQuantity(
    String productId,
    Double quantity
  ){
    return database.transaction(transaction -> {
      Product product = updateProduct(transaction, quantity, productId);
      return save(Move
        .builder()
        .type(MoveType.ENTER)
        .product(product)
        .givenQuantity(quantity)
        .remainingQuantity(product.getQuantity())
        .build());
    });
  }

  public List<StationMoveDetail> findStationDetailsWithIntervals(
    String stationId,
    LocalDate startDate,
    LocalDate endDate
  ) {
    String sql = "SELECT * FROM station_details WHERE station_id = ? AND move_date BETWEEN ? AND ?";
    return database
      .prepare(sql, StationMoveDetail.class).
      all(stationId, startDate, endDate);
  }

  public List<StationMoveDetail> findStationsDetailsWithIntervals(
    LocalDate startDate,
    LocalDate endDate
  ){
    String sql = "SELECT * FROM station_details WHERE move_date BETWEEN ? AND ?";
    return database
      .prepare(sql, StationMoveDetail.class).
      all(startDate, endDate);
  }

  private <T extends DatabaseTemplate> Product updateProduct(T database, Double quantity, String productId){
    String sqlProductUpdate = """
        WITH remaining AS (
            SELECT
                quantity - daily_evaporation_quantity * extract(days from age(now(), updated_at))
            FROM "@table"
        ) UPDATE "@table" SET
            quantity = (CASE WHEN remaining >= 0 THEN remaining ELSE 0 END) + ?,
            updated_at = now()
        WHERE id = ?
        RETURNING *
        """;
    return database
      .prepare(sqlProductUpdate, Product.class)
      .optional(quantity, productId)
      .orElseThrow(() -> new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Cannot update product"
      ));
  }
}
