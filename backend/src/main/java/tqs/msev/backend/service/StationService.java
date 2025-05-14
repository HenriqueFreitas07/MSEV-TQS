package tqs.msev.backend.service;

import org.springframework.stereotype.Service;
import tqs.msev.backend.entity.Station;
import tqs.msev.backend.repository.StationRepository;

import java.util.List;

@Service
public class StationService {
    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    public List<Station> searchByName(String query) {
        List<Station> stations = stationRepository.findAll();

        return stations.stream()
                .filter(station -> station.getName().toLowerCase().startsWith(query.toLowerCase()))
                .toList();
    }
}
