package app.station.models;

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
public class Move {
  @Id
  @GeneratedValue(strategy = Generative.UUID)
  private String id;

  @Enumerated(EnumType.STRING)
  private MoveType type;

  @ManyToOne
  @JoinColumn(nullable = false)
  private Product product;

  @Column(name = "given_quantity", nullable = false)
  private Double givenQuantity;

  @Column(name = "remaining_quantity", nullable = false)
  private Double remainingQuantity;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "created_at", defaultValue = "now()")
  private Instant createdAt;
}
