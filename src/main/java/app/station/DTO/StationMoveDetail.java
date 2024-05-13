package app.station.DTO;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import persistence.annotations.Attribute;
import persistence.annotations.View;

import java.time.LocalDate;

@Data
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@View(name = "station_details")
public class StationMoveDetail {
  @Attribute(name = "station_id")
  private String stationId;

  @Attribute(name = "station_full_location")
  private String stationFullLocation;

  @Attribute(name = "product_id")
  private String productId;

  @Attribute(name = "product_name")
  private String productName;

  @Attribute(name = "remaining_quantity")
  private Double remainingQuantity;

  @Attribute(name = "total_entered_quantity")
  private Double totalEnteredQuantity;

  @Attribute(name = "total_sales_price")
  private Double totalAmountSold;

  @Attribute(name = "total_sold_quantity")
  private Double totalQuantitySold;

  @Attribute(name = "move_date")
  private LocalDate date;
}
