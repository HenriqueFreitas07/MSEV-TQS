package tqs.msev.backend.repository;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import tqs.msev.backend.entity.Station;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class StationRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StationRepository stationRepository;

    @Test
    @Requirement("MSEV-16")
    void whenValidId_thenReturnStation() {
        Station station1 = new Station();
        station1.setLatitude(40);
        station1.setLongitude(-0.5);
        station1.setName("Station 1");
        station1.setAddress("NY Street, 1");

        station1 = entityManager.persistAndFlush(station1);

        Station station = stationRepository.findById(station1.getId()).orElseThrow();

        assertThat(station.getId()).isEqualTo(station1.getId());
    }

    @Test
    @Requirement("MSEV-16")
    void whenInvalidId_thenReturnNull() {
        Station station = stationRepository.findById(UUID.randomUUID()).orElse(null);

        assertThat(station).isNull();
    }

    @Test
    @Requirement("MSEV-16")
    void givenSetOfStations_whenFindAll_thenReturnAllStations() {
        Station station1 = new Station();
        station1.setLatitude(40);
        station1.setLongitude(-0.5);
        station1.setName("Station 1");
        station1.setAddress("NY Street, 1");

        Station station2 = new Station();
        station2.setLatitude(41);
        station2.setLongitude(-0.5);
        station2.setName("Station 2");
        station2.setAddress("Idk Street, 2");

        entityManager.persist(station1);
        entityManager.persist(station2);
        entityManager.flush();

        List<Station> stations = stationRepository.findAll();

        assertThat(stations).hasSize(2).extracting(Station::getName).contains(station1.getName(), station2.getName());
    }
}
