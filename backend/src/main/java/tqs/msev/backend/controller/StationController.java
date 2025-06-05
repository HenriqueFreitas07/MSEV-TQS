package tqs.msev.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import tqs.msev.backend.entity.ChargeSession;
import tqs.msev.backend.entity.Station;
import tqs.msev.backend.service.ChargerService;
import tqs.msev.backend.service.StationService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stations")
public class StationController {
    private final StationService stationService;

    private final ChargerService chargerService;

    public StationController(StationService stationService, ChargerService chargerService) {
        this.stationService = stationService;
        this.chargerService = chargerService;
    }

    @GetMapping
    public List<Station> getAllStations() {
        return stationService.getAllStations();
    }

    @GetMapping("/{id}")
    public Station getStationById(@PathVariable UUID id) {
        return stationService.getStationById(id);
    }

    @GetMapping("/search-by-name")
    public List<Station> searchStationByName(@RequestParam String name) {
        return stationService.searchByName(name);
    }

    @GetMapping("/search-by-address")
    public List<Station> searchStationByAddress(@RequestParam String address) {
        return stationService.searchByAddress(address);
    }
    @PreAuthorize("@userService.getCurrentUser(authentication).isOperator()")
    @PatchMapping("/{id}/disable")
    public void disableStation(@PathVariable UUID id) {
        Station s = stationService.getStationById(id);
        stationService.disableStation(s);
    }

    @PreAuthorize("@userService.getCurrentUser(authentication).isOperator()")
    @PatchMapping("/{id}/enable")
    public void enableStation(@PathVariable UUID id) {
        Station s = stationService.getStationById(id);
        stationService.enableStation(s);
    }
    @PreAuthorize("@userService.getCurrentUser(authentication).isOperator()")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Station createStation(@Valid @RequestBody Station station) {
        return stationService.createStation(station);
    }

    @PreAuthorize("@userService.getCurrentUser(authentication).isOperator()")
    @GetMapping("/stats/{stationId}")
    public List<ChargeSession> getStationStats(@PathVariable UUID stationId) {
        if (stationId == null) {
            throw new IllegalArgumentException("Station ID cannot be null");
        }
        if (stationService.getStationById(stationId) == null) {
            throw new NoSuchElementException("Invalid station id");
        }
        return chargerService.getStationStats(stationId);
    }
}
