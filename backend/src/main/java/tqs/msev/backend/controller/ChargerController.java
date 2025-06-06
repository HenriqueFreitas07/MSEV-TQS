package tqs.msev.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import tqs.msev.backend.dto.UpdateChargerPriceDTO;
import tqs.msev.backend.dto.UpdateChargerStatusDTO;
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
    @Operation(summary = "Get all the chargers of a station")
    public List<Charger> getChargersByStation(@PathVariable("stationId") UUID stationId) {
        return chargerService.getChargersByStation(stationId);
    }

    @GetMapping("/{chargerId}")
    @Operation(summary = "Get a charger details, by id")
    public Charger getChargerById(@PathVariable("chargerId") UUID chargerId) {
        return chargerService.getChargerById(chargerId);
    }

    @GetMapping("/{chargerId}/reservations")
    @Operation(summary = "Get all reservations for the specified charger")
    public List<Reservation> getChargerAvailability(@PathVariable("chargerId") UUID chargerId) {
        return reservationService.getFutureReservationsOnCharger(chargerId);
    }

    @PreAuthorize("@userService.getCurrentUser(authentication).isOperator()")
    @PatchMapping("/{chargerId}/disable")
    @Operation(summary = "Disables a charger")
    public void disableCharger(@PathVariable UUID chargerId) {
        Charger charger = chargerService.getChargerById(chargerId);
        chargerService.disableCharger(charger);
    }

    @PreAuthorize("@userService.getCurrentUser(authentication).isOperator()")
    @PatchMapping("/{chargerId}/enable")
    @Operation(summary = "Enables a charger")
    public void enableCharger(@PathVariable UUID chargerId) {
        Charger charger = chargerService.getChargerById(chargerId);
        chargerService.enableCharger(charger);
    }

    @PreAuthorize("@userService.getCurrentUser(authentication).isOperator()")
    @PatchMapping("/{chargerId}")
    @Operation(summary = "Update a charger status")
    public void updateChargerStatus(@PathVariable UUID chargerId, @Valid @RequestBody UpdateChargerStatusDTO dto) {
        chargerService.updateChargerStatus(chargerId, dto.getStatus());
    }

    @PatchMapping("/{chargerId}/unlock")
    @Operation(summary = "Unlocks a charger")
    public void unlockCharger(@PathVariable UUID chargerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        chargerService.unlockCharger(chargerId, user.getId());
    }

    @PatchMapping("/{chargerId}/lock")
    @Operation(summary = "Locks a charger")
    public void lockCharger(@PathVariable UUID chargerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        chargerService.lockCharger(chargerId, user.getId());
    }

    @PreAuthorize("@userService.getCurrentUser(authentication).isOperator()")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Creates a charger")
    public Charger createCharger(@Valid @RequestBody Charger charger) {
        return chargerService.createCharger(charger);
    }

    @PreAuthorize("@userService.getCurrentUser(authentication).isOperator()")
    @PatchMapping("/{chargerId}/update")
    @Operation(summary = "Updates the charging price of a charger")
    public Charger updateChargerPrice(@PathVariable UUID chargerId, @RequestBody UpdateChargerPriceDTO dto) {
        return chargerService.updateChargerPrice(chargerId, dto.getPrice());
    }
}
