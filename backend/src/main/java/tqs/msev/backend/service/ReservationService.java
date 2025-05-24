package tqs.msev.backend.service;

import org.springframework.stereotype.Service;
import tqs.msev.backend.entity.Reservation;
import tqs.msev.backend.repository.ReservationRepository;
import java.util.List;
import java.util.Date;
import java.util.UUID;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation createReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
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
    
}
