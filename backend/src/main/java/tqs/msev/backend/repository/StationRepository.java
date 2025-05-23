package tqs.msev.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tqs.msev.backend.entity.Station;

import java.util.UUID;

@Repository
public interface StationRepository extends JpaRepository<Station, UUID> {
}
