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
@Table
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Station {
  @Id
  @GeneratedValue(strategy = Generative.UUID)
  private String id;

  @Column(nullable = false)
  private String country;

  @Column(nullable = false)
  private String city;

  @Column(nullable = false)
  private String suburb;

  @Column(name = "created_at", defaultValue = "now()")
  private Instant createdAt;
}
