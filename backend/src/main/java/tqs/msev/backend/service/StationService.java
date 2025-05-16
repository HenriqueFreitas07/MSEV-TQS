package tqs.msev.backend.service;

import org.springframework.stereotype.Service;
import tqs.msev.backend.entity.Station;
import tqs.msev.backend.repository.StationRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class StationService {
    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    public Station getStationById(UUID id) {
        return stationRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Invalid station id"));
    }

    public List<Station> searchByName(String query) {
        List<Station> stations = stationRepository.findAll();

        return stations.stream()
                .filter(station -> station.getName().toLowerCase().startsWith(query.toLowerCase()))
                .toList();
    }

    public List<Station> searchByAddress(String address) {
        List<Station> stations = stationRepository.findAll();

        return stations.stream()
                .filter(station -> station.getAddress().toLowerCase().startsWith(address.toLowerCase()))
                .toList();
    }
}
