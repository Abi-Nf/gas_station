package app.station.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import persistence.annotations.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product_price")
public class ProductPrice {
  @Id
  @GeneratedValue(strategy = Generative.UUID)
  private String id;

  @ManyToOne
  @JoinColumn(name = "product_template")
  private ProductTemplate productTemplate;

  @Column(nullable = false)
  private Double price;

  @Column(name = "created_at", defaultValue = "now()")
  private String createdAt;
}
