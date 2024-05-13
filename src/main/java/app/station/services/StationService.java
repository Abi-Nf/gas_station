package app.station.services;

import app.station.models.Station;
import app.station.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StationService {
  private final StationRepository stationRepository;

  public Station save(Station station){
    return stationRepository.save(station);
  }

  public Station getStationById(String id){
    return stationRepository
      .findById(id)
      .orElseThrow(() -> new ResponseStatusException(
        HttpStatus.NOT_FOUND,
        "Station not found"
      ));
  }

  public List<Station> getAllStations(){
    return stationRepository.findAll();
  }

  public Station deleteStation(String id){
    return stationRepository.deleteById(id);
  }
}
