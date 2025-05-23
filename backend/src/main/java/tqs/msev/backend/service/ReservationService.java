package tqs.msev.backend.service;

import org.springframework.stereotype.Service;
import tqs.msev.backend.entity.Reservation;
import tqs.msev.backend.repository.ReservationRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Date;
import java.util.UUID;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<Reservation> getUserReservations(UUID userId) {
        return reservationRepository.findByUserId(userId);
    }

    public List<Reservation> getFutureReservationsOnCharger(UUID chargerId) {
        List<Reservation> reservations = reservationRepository.findByChargerId(chargerId);
        Date now = new Date();
        Date fiveDaysFromNow = new Date(now.getTime() + 5 * 24 * 60 * 60 * 1000);
        return reservations.stream()
                .filter(reservation -> reservation.getStartTimestamp().after(now))
                .filter(reservation -> reservation.getStartTimestamp().before(fiveDaysFromNow))
                .toList();
        
    }

    public Reservation createReservation(Reservation reservation) {

        Date now = new Date();
        if (reservation.getStartTimestamp().before(now) || reservation.getEndTimestamp().before(now)) {
            throw new IllegalArgumentException("Reservation cannot be in the past");
        }
        if (reservation.getStartTimestamp().after(reservation.getEndTimestamp())) {
            throw new IllegalArgumentException("Start timestamp must be before end timestamp");
        }
        List<Reservation> existingReservations = reservationRepository.findByChargerId(reservation.getCharger().getId());
        for (Reservation existingReservation : existingReservations) {
            if (reservation.getStartTimestamp().before(existingReservation.getEndTimestamp()) &&
                reservation.getEndTimestamp().after(existingReservation.getStartTimestamp())) {
                throw new IllegalArgumentException("Reservation overlaps with an existing reservation");
            }
        }
        return reservationRepository.save(reservation);
    }

    public Reservation cancelReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException("Reservation not found"));
        reservationRepository.delete(reservation);
        reservationRepository.flush();
        return reservation;
    }

    public Reservation getReservationById(UUID reservationId){
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        return reservation;
    }

    public Reservation markReservationAsUsed(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        if(reservation.isUsed()) {
            throw new IllegalArgumentException("Reservation already marked as used");
        }
        if(reservation.getStartTimestamp().after(new Date())) {
            throw new IllegalArgumentException("Reservation not started yet");
        }
        if(reservation.getEndTimestamp().before(new Date())) {
            throw new IllegalArgumentException("Reservation already ended");
        }
        reservation.setUsed(true);
        reservationRepository.save(reservation);
        reservationRepository.flush();
        return reservation;
    }
    
}
