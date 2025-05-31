package tqs.msev.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tqs.msev.backend.entity.ChargeSession;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ChargeSessionRepository extends JpaRepository<ChargeSession, UUID> {
    ChargeSession findByChargerIdAndEndTimestamp(UUID chargerId, LocalDateTime endTimestamp);
}
