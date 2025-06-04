package tqs.msev.backend.service;

import org.springframework.stereotype.Service;
import tqs.msev.backend.entity.Charger;
import tqs.msev.backend.entity.Reservation;
import tqs.msev.backend.repository.ReservationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private static final String RESERVATION_NOT_FOUND = "Reservation not found";

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<Reservation> getUserReservations(UUID userId) {
        return reservationRepository.findByUserId(userId);
    }

    public List<Reservation> getFutureReservationsOnCharger(UUID chargerId) {
        List<Reservation> reservations = reservationRepository.findByChargerId(chargerId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveDaysFromNow = now.plusDays(5);
        return reservations.stream()
                .filter(reservation -> reservation.getStartTimestamp().isAfter(now))
                .filter(reservation -> reservation.getStartTimestamp().isBefore(fiveDaysFromNow))
                .toList();
        
    }

    public Reservation createReservation(Reservation reservation) {

        LocalDateTime now = LocalDateTime.now();

        if (reservation.getStartTimestamp().isBefore(now) || reservation.getEndTimestamp().isBefore(now)) {
            throw new IllegalArgumentException("Reservation cannot be in the past");
        }
        if (reservation.getStartTimestamp().isAfter(reservation.getEndTimestamp())) {
            throw new IllegalArgumentException("Start timestamp must be before end timestamp");
        }
        List<Reservation> existingReservations = reservationRepository.findByChargerId(reservation.getCharger().getId());
        for (Reservation existingReservation : existingReservations) {
            if (reservation.getStartTimestamp().isBefore(existingReservation.getEndTimestamp()) &&
                reservation.getEndTimestamp().isAfter(existingReservation.getStartTimestamp())) {
                throw new IllegalArgumentException("Reservation overlaps with an existing reservation");
            }
        }
        return reservationRepository.save(reservation);
    }

    public Reservation cancelReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException(RESERVATION_NOT_FOUND));
        reservationRepository.delete(reservation);
        reservationRepository.flush();
        return reservation;
    }

    public Reservation getReservationById(UUID reservationId){
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException(RESERVATION_NOT_FOUND));
    }

    public Reservation markReservationAsUsed(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(RESERVATION_NOT_FOUND));
        if(reservation.isUsed()) {
            throw new IllegalArgumentException("Reservation already marked as used");
        }
        if(reservation.getStartTimestamp().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reservation not started yet");
        }
        if(reservation.getEndTimestamp().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reservation already ended");
        }
        reservation.setUsed(true);
        reservationRepository.save(reservation);
        reservationRepository.flush();
        return reservation;
    }

    public List<Reservation> getChargerReservations(UUID chargerId) {
        return reservationRepository.findByChargerId(chargerId);
    }
    
}
