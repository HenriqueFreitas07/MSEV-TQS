package tqs.msev.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tqs.msev.backend.entity.Reservation;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    List<Reservation> findByChargerId(UUID chargerId);
    List<Reservation> findByUserId(UUID userId);
    Reservation findByUserIdAndStartTimestampBeforeAndEndTimestampAfter(UUID userId, LocalDateTime startTimestampBefore, LocalDateTime endTimestampAfter);
}