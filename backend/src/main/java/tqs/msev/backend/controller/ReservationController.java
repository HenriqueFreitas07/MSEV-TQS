package tqs.msev.backend.controller;

import org.springframework.data.jpa.repository.Query;
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
    public Reservation createReservation(@Valid @RequestBody Reservation reservation) {
        return reservationService.createReservation(reservation);
    }

    @GetMapping("/{reservationId}")
    public Reservation getReservationById(@PathVariable("reservationId") UUID reservationId) {
        return reservationService.getReservationById(reservationId);
    }

    @DeleteMapping("/{reservationId}")
    public Reservation cancelReservation(@PathVariable("reservationId") UUID reservationId) {
        return reservationService.cancelReservation(reservationId);
    }

    @PutMapping("/{reservationId}/used")
    public Reservation markReservationAsUsed(@PathVariable("reservationId") UUID reservationId) {
        return reservationService.markReservationAsUsed(reservationId);
    }

    @GetMapping()
    public List<Reservation> getReservations(@RequestParam(value="chargerId", required= false) UUID chargerId, 
                                                    @RequestParam(value = "userId", required = false) UUID userId) {
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