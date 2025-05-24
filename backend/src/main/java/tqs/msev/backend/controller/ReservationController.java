package tqs.msev.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/user/{userId}")
    public List<Reservation> getUserReservations(@PathVariable("userId") UUID userId) {
        return reservationService.getUserReservations(userId);
    }

    @PostMapping("/create")
    public Reservation createReservation(@Valid @RequestBody Reservation reservation) {
        return reservationService.createReservation(reservation);
    }

    @GetMapping("/{reservationId}")
    public Reservation getReservationById(@PathVariable("reservationId") UUID reservationId) {
        return reservationService.getReservationById(reservationId);
    }

    @PostMapping("/{reservationId}/cancel")
    public Reservation cancelReservation(@PathVariable("reservationId") UUID reservationId) {
        return reservationService.cancelReservation(reservationId);
    }

    @PutMapping("/{reservationId}/used")
    public Reservation markReservationAsUsed(@PathVariable("reservationId") UUID reservationId) {
        return reservationService.markReservationAsUsed(reservationId);
    }

    @GetMapping("/charger/{chargerId}")
    public List<Reservation> getChargerReservations(@PathVariable("chargerId") UUID chargerId) {
        return reservationService.getChargerReservations(chargerId);
    }

}