package tqs.msev.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tqs.msev.backend.entity.Charger;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChargerRepository extends JpaRepository<Charger, UUID> {
    List<Charger> findByStationId(UUID stationId);
}