package app.station.controllers;

import app.station.DTO.BuyProductPayload;
import app.station.DTO.StationMoveDetail;
import app.station.models.Move;
import app.station.services.MoveService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/moves")
public class MoveController {
  private final MoveService moveService;

  @PostMapping("/supply/{productId}")
  public Move updateProduct(
    @PathVariable String productId,
    @RequestParam(required = false) LocalDate date,
    @RequestPart Double quantity
  ){
    return moveService.supplyProduct(productId, quantity, date);
  }

  @PostMapping("/buy")
  public Move buyProduct(
    @RequestBody BuyProductPayload payload
  ){
    return moveService.buyProduct(payload);
  }

  @GetMapping("/{stationId}")
  public List<Move> getStationMoves(
    @PathVariable String stationId,
    @RequestParam("start_date") LocalDate startDate,
    @RequestParam("end_date") LocalDate endDate
  ){
    return moveService.getStationMovesByTimeInterval(
      stationId,
      startDate,
      endDate
    );
  }

  @GetMapping("/details/{stationId}")
  public List<StationMoveDetail> getStationDetails(
    @PathVariable String stationId,
    @RequestParam("start_date") LocalDate startDate,
    @RequestParam("end_date") LocalDate endDate
  ){
    return moveService.getStationDetails(
      stationId,
      startDate,
      endDate
    );
  }
}
