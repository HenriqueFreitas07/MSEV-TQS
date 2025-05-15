package tqs.msev.backend.service;

import org.springframework.stereotype.Service;
import tqs.msev.backend.entity.Charger;
import tqs.msev.backend.repository.ChargerRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ChargerService {
    private final ChargerRepository chargerRepository;

    public ChargerService(ChargerRepository chargerRepository) {
        this.chargerRepository = chargerRepository;
    }

    public List<Charger> getChargersByStation(UUID stationId) {
        return chargerRepository.findByStationId(stationId);
    }

    public Charger getChargerById(UUID chargerId) {
        return chargerRepository.findById(chargerId)
                .orElseThrow(() -> new NoSuchElementException("Charger not found"));
    }
}