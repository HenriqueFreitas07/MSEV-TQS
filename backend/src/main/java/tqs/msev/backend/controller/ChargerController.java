package tqs.msev.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tqs.msev.backend.entity.Charger;
import tqs.msev.backend.entity.Reservation;
import java.util.UUID;
import tqs.msev.backend.service.ChargerService;
import tqs.msev.backend.service.ReservationService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chargers")
public class ChargerController {
    private final ChargerService chargerService;
    private final ReservationService reservationService;

    public ChargerController(ChargerService chargerService, ReservationService reservationService) {
        this.chargerService = chargerService;
        this.reservationService = reservationService;
    }

    @GetMapping("/station/{stationId}")
    public List<Charger> getChargersByStation(@PathVariable("stationId") UUID stationId) {
        return chargerService.getChargersByStation(stationId);
    }

    @GetMapping("/{chargerId}")
    public Charger getChargerById(@PathVariable("chargerId") UUID chargerId) {
        return chargerService.getChargerById(chargerId);
    }

    @GetMapping("/{chargerId}/reservations")
    public List<Reservation> getChargerAvailability(@PathVariable("chargerId") UUID chargerId) {
        return reservationService.getFutureReservationsOnCharger(chargerId);
    }
}