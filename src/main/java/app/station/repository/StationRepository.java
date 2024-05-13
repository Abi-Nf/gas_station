package app.station.repository;

import app.station.models.Station;
import org.springframework.stereotype.Repository;
import persistence.CommonRepository;

@Repository
public class StationRepository extends CommonRepository<Station, String> {
}
