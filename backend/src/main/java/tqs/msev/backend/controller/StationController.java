package tqs.msev.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tqs.msev.backend.entity.Station;
import tqs.msev.backend.service.StationService;

import java.util.List;

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

    @GetMapping("/search")
    public List<Station> searchStationByName(@RequestParam String name) {
        return stationService.searchByName(name);
    }
}
