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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product_template")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProductTemplate {
  @Id
  @GeneratedValue(strategy = Generative.UUID)
  private String id;

  @Column(nullable = false)
  private String name;

  @Column(name = "created_at")
  private Instant createdAt;

  public ProductTemplate(String name){
    this.name = name;
  }
}
