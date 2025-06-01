package tqs.msev.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import tqs.msev.backend.entity.Charger;
import tqs.msev.backend.entity.Reservation;

import java.util.UUID;

import tqs.msev.backend.entity.User;
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

    @PatchMapping("/{chargerId}/unlock")
    public void unlockCharger(@PathVariable UUID chargerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        chargerService.unlockCharger(chargerId, user.getId());
    }

    @PatchMapping("/{chargerId}/lock")
    public void lockCharger(@PathVariable UUID chargerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        chargerService.lockCharger(chargerId, user.getId());
    }

    @PreAuthorize("@userService.getCurrentUser(authentication).isOperator()")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Charger createCharger(@Valid @RequestBody Charger charger) {
        return chargerService.createCharger(charger);
    }
}