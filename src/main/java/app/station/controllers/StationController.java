package app.station.controllers;

import app.station.DTO.StationPayload;
import app.station.models.Station;
import app.station.services.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stations")
public class StationController {
  private final StationService stationService;

  @PostMapping
  public Station createStation(
    @RequestBody StationPayload payload
  ) {
    return stationService.save(payload.toStation());
  }

  @GetMapping("/{id}")
  public Station getStationById(
    @PathVariable String id
  ) {
    return stationService.getStationById(id);
  }

  @GetMapping
  public List<Station> getAllStations() {
    return stationService.getAllStations();
  }

  @DeleteMapping("/{id}")
  public Station deleteStationById(
    @PathVariable String id
  ){
    return stationService.deleteStation(id);
  }
}
