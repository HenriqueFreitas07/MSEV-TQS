package tqs.msev.backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tqs.msev.backend.entity.Station;
import tqs.msev.backend.service.StationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stations")
public class StationController {
    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
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
    @PreAuthorize("@userService.getCurrentUser(authentication).operator()")
    @PatchMapping("/{id}/disable")
    public void disableStation(@PathVariable UUID id) {
        Station s = stationService.getStationById(id);
        stationService.disableStation(s);
    }
}
