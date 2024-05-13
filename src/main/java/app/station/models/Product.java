package app.station.models;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import persistence.annotations.*;

import java.time.Instant;

@Data
@Table
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Product {
  public final static Double MAX_AMOUNT_TO_BUY = 200.0;

  @Id
  @GeneratedValue(strategy = Generative.UUID)
  private String id;

  @ManyToOne
  @JoinColumn(nullable = false)
  private Station station;

  @Column(defaultValue = "0")
  private Double quantity;

  @Column(name = "daily_evaporation_quantity", nullable = false)
  private Double dailyEvaporationQuantity;

  @ManyToOne
  @JoinColumn(name = "product_template")
  private ProductTemplate productTemplate;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "created_at", defaultValue = "now()")
  private Instant createdAt;
}
