package tqs.msev.backend.service;

import org.springframework.stereotype.Service;
import tqs.msev.backend.dto.Coordinates;
import tqs.msev.backend.entity.Charger;
import tqs.msev.backend.entity.Station;
import tqs.msev.backend.repository.StationRepository;
import tqs.msev.backend.util.Util;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class StationService {
    private final StationRepository stationRepository;
    private final GeocodingService geocodingService;
    private final ChargerService chargerService;

    public StationService(StationRepository stationRepository, GeocodingService geocodingService, ChargerService chargerService) {
        this.stationRepository = stationRepository;
        this.geocodingService = geocodingService;
        this.chargerService = chargerService;
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
        Coordinates coordinates = geocodingService.getCoordinatesForAddress(address);

        if (coordinates == null) return List.of();

        List<Station> stations = stationRepository.findAll();

        return stations.stream().sorted(Comparator.comparingDouble(station ->
            Util.distanceBetweenCoordinates(coordinates.getLat(), coordinates.getLon(),
                    station.getLatitude(), station.getLongitude())
        )).toList();
    }
    public void disableStation(Station station) {
        // put all chargers of the station out of order
        List<Charger> chargers = station.getChargers();
        for (Charger charger : chargers) {
            chargerService.outOfOrderCharger(charger);
        }
        station.setStatus(Station.StationStatus.DISABLED);
        stationRepository.save(station);
    }
    public Station createStation(Station station) {
        return stationRepository.save(station);
    }
}
