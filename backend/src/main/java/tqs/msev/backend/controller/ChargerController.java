package tqs.msev.backend.controller;

import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tqs.msev.backend.entity.Station;
import tqs.msev.backend.entity.Charger;
import java.util.UUID;
import tqs.msev.backend.service.ChargerService;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chargers")
public class ChargerController {
    private final ChargerService chargerService;

    public ChargerController(ChargerService chargerService) {
        this.chargerService = chargerService;
    }

    @GetMapping("/station/{stationId}")
    public List<Charger> getChargersByStation(@PathVariable("stationId") UUID stationId) {
        return chargerService.getChargersByStation(stationId);
    }

    @GetMapping("/{chargerId}")
    public Charger getChargerById(@PathVariable("chargerId") UUID chargerId) {
        return chargerService.getChargerById(chargerId);
    }

    @GetMapping("/{chargerId}/status")
    public Charger.ChargerStatus getChargerStatus(@PathVariable("chargerId") UUID chargerId) {
        return chargerService.getChargerStatus(chargerId);
    }
}