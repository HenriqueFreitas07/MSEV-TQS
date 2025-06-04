package tqs.msev.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
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
    @Operation(summary = "Get all registered stations")
    public List<Station> getAllStations() {
        return stationService.getAllStations();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get station details, by id")
    public Station getStationById(@PathVariable UUID id) {
        return stationService.getStationById(id);
    }

    @GetMapping("/search-by-name")
    @Operation(summary = "Search for a station, by name")
    public List<Station> searchStationByName(@Parameter(description = "Name to search for") @RequestParam String name) {
        return stationService.searchByName(name);
    }

    @GetMapping("/search-by-address")
    @Operation(summary = "Get the stations ordered by distance from the specified address")
    public List<Station> searchStationByAddress(@Parameter(description = "Address to search for") @RequestParam String address) {
        return stationService.searchByAddress(address);
    }

    @PreAuthorize("@userService.getCurrentUser(authentication).isOperator()")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a station")
    public Station createStation(@Valid @RequestBody Station station) {
        return stationService.createStation(station);
    }
}
