package app.station.DTO;

import app.station.models.Station;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StationPayload {
  private String country;
  private String city;
  private String suburb;

  public Station toStation(){
    return Station
      .builder()
      .country(country)
      .city(city)
      .suburb(suburb)
      .build();
  }
}
