package tqs.msev.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import tqs.msev.backend.entity.Reservation;
import java.util.UUID;
import tqs.msev.backend.service.ReservationService;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @Operation(summary = "Create a new reservation")
    public Reservation createReservation(@Valid @RequestBody Reservation reservation) {
        return reservationService.createReservation(reservation);
    }

    @GetMapping("/{reservationId}")
    @Operation(summary = "Get a reservation details, by id")
    public Reservation getReservationById(@PathVariable("reservationId") UUID reservationId) {
        return reservationService.getReservationById(reservationId);
    }

    @DeleteMapping("/{reservationId}")
    @Operation(summary = "Cancel a reservation")
    public Reservation cancelReservation(@PathVariable("reservationId") UUID reservationId) {
        return reservationService.cancelReservation(reservationId);
    }

    @PutMapping("/{reservationId}/used")
    @Operation(summary = "Marks a reservation as used")
    public Reservation markReservationAsUsed(@PathVariable("reservationId") UUID reservationId) {
        return reservationService.markReservationAsUsed(reservationId);
    }

    @GetMapping
    @Operation(summary = "Get all reservations for a user or a for a charger")
    public List<Reservation> getReservations(
            @Parameter(description = "Charger id to filter") @RequestParam(value="chargerId", required= false) UUID chargerId,
            @Parameter(description = "User id to filter") @RequestParam(value = "userId", required = false) UUID userId
    ) {
        if (chargerId != null && userId != null) {
            throw new IllegalArgumentException("Only one of chargerId or userId should be provided");
        }
        if (chargerId != null) {
            return reservationService.getChargerReservations(chargerId);
        } else if (userId != null) {
            return reservationService.getUserReservations(userId);
        } else {
            throw new IllegalArgumentException("Either chargerId or userId must be provided");
        }
    }
}
