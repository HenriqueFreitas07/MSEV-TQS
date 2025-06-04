package tqs.msev.backend.service;

import org.springframework.stereotype.Service;
import tqs.msev.backend.entity.ChargeSession;
import tqs.msev.backend.entity.Charger;
import tqs.msev.backend.entity.Reservation;
import tqs.msev.backend.repository.ChargeSessionRepository;
import tqs.msev.backend.repository.ChargerRepository;
import tqs.msev.backend.repository.ReservationRepository;
import tqs.msev.backend.repository.UserRepository;
import tqs.msev.backend.repository.StationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ChargerService {
    private final ChargerRepository chargerRepository;
    private final ReservationRepository reservationRepository;
    private final ChargeSessionRepository chargeSessionRepository;
    private final UserRepository userRepository;
    private final StationRepository stationRepository;

    public ChargerService(ChargerRepository chargerRepository, ReservationRepository reservationRepository, ChargeSessionRepository chargeSessionRepository, UserRepository userRepository, StationRepository stationRepository) {
        this.chargerRepository = chargerRepository;
        this.reservationRepository = reservationRepository;
        this.chargeSessionRepository = chargeSessionRepository;
        this.userRepository = userRepository;
        this.stationRepository = stationRepository;
    }

    public List<Charger> getChargersByStation(UUID stationId) {
        return chargerRepository.findByStationId(stationId);
    }

    public Charger getChargerById(UUID chargerId) {
        return chargerRepository.findById(chargerId)
                .orElseThrow(() -> new NoSuchElementException("Charger not found"));
    }

    public void unlockCharger(UUID chargerId, UUID userId) {
        Charger charger = chargerRepository.findById(chargerId)
                .orElseThrow(() -> new NoSuchElementException("Invalid charger id"));

        if (charger.getStatus() == Charger.ChargerStatus.OUT_OF_ORDER) {
            throw new IllegalStateException("Charger is out of order");
        }

        if (charger.getStatus() == Charger.ChargerStatus.TEMPORARILY_DISABLED) {
            throw new IllegalStateException("Charger is temporarily disabled");
        }

        if (charger.getStatus() == Charger.ChargerStatus.IN_USE) {
            // If the charger is in use, let's check if the user has a valid reservation for this charger...
            LocalDateTime now = LocalDateTime.now();
            Reservation reservation = reservationRepository
                    .findByUserIdAndStartTimestampBeforeAndEndTimestampAfter(userId, now, now);

            if (reservation == null) {
                throw new IllegalStateException("Charger is in use");
            }

            ChargeSession oldSession = chargeSessionRepository.findByChargerIdAndEndTimestamp(chargerId, null);
            oldSession.setEndTimestamp(LocalDateTime.now());

            chargeSessionRepository.save(oldSession);
        }

        ChargeSession newSession = ChargeSession.builder()
                .user(userRepository.getReferenceById(userId))
                .charger(charger)
                .startTimestamp(LocalDateTime.now())
                .build();

        chargeSessionRepository.save(newSession);

        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = reservationRepository
                .findByUserIdAndStartTimestampBeforeAndEndTimestampAfter(userId, now, now);

        if (reservation != null) {
            reservation.setUsed(true);
            reservationRepository.save(reservation);
        }

        charger.setStatus(Charger.ChargerStatus.IN_USE);
        chargerRepository.save(charger);
    }

    public void lockCharger(UUID chargerId, UUID userId) {
        ChargeSession session = chargeSessionRepository.findByChargerIdAndEndTimestamp(chargerId, null);

        if (session == null)
            throw new NoSuchElementException("The charger is already available");

        if (!session.getUser().getId().equals(userId))
            throw new IllegalStateException("You cannot lock a charger that is already being used by another user");

        session.setEndTimestamp(LocalDateTime.now());
        chargeSessionRepository.save(session);

        Charger charger = session.getCharger();
        charger.setStatus(Charger.ChargerStatus.AVAILABLE);
        chargerRepository.save(charger);
    }

    public Charger createCharger(Charger charger) {
        if (charger.getStation() == null || stationRepository.findById(charger.getStation().getId()).isEmpty()) {
            throw new IllegalArgumentException("Charger must be associated with a valid station");
        }
        if (charger.getStatus() == null) {
            charger.setStatus(Charger.ChargerStatus.AVAILABLE);
        }

        return chargerRepository.save(charger);
    }

    public List<ChargeSession> getChargeSessions(UUID userId, boolean activeOnly) {
        List<ChargeSession> sessions = chargeSessionRepository.findAllByUserId(userId);

        if (activeOnly) {
            return sessions.stream().filter(s -> s.getEndTimestamp() == null).toList();
        }

        return sessions;
    }
}