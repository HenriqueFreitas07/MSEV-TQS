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
    public void disableCharger(Charger charger) {
        charger.setStatus(Charger.ChargerStatus.TEMPORARILY_DISABLED);
        chargerRepository.save(charger);
    }

    public void outOfOrderCharger(Charger charger) {
        charger.setStatus(Charger.ChargerStatus.OUT_OF_ORDER);
        chargerRepository.save(charger);
    }

    public void enableCharger(Charger charger) {
        charger.setStatus(Charger.ChargerStatus.AVAILABLE);
        chargerRepository.save(charger);
    }

}