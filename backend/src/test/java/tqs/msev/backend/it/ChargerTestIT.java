package tqs.msev.backend.it;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import tqs.msev.backend.entity.Charger;
import tqs.msev.backend.repository.ChargerRepository;
import tqs.msev.backend.repository.StationRepository;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;

import java.util.UUID;
import tqs.msev.backend.entity.Station;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class ChargerTestIT {
    
    @Autowired
    private ChargerRepository chargerRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private MockMvc mockMvc;

    
    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
        .withDatabaseName("testdb")
        .withUsername("user")
        .withPassword("password");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", ()->"create-drop");
    }
    
    @AfterEach
    public void resetDb() {
        chargerRepository.deleteAll();
        stationRepository.deleteAll();
    }

    @Test
    public void whenStationExists_thenReturnChargers() throws Exception {
        Station station = new Station();
        station.setName("Test Station");
        station.setLongitude(-74.0060);
        station.setLatitude(40.7128);
        station.setStatus(Station.StationStatus.ENABLED);

        station = stationRepository.save(station);
        stationRepository.flush();

        Charger charger = Charger.builder()
                .station(station)
                .connectorType("Type 2")
                .price(0.5)
                .chargingSpeed(22)
                .status(Charger.ChargerStatus.AVAILABLE)
                .build();
        Charger charger2 = Charger.builder()
                .station(station)
                .connectorType("CCS")
                .price(0.7)
                .chargingSpeed(50)
                .status(Charger.ChargerStatus.AVAILABLE)
                .build();

        chargerRepository.save(charger);
        chargerRepository.save(charger2);
        chargerRepository.flush();
        UUID stationId = station.getId();
        mockMvc.perform(get("/api/v1/chargers/station/{stationId}", stationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].connectorType").value("Type 2"))
                .andExpect(jsonPath("$[1].connectorType").value("CCS"));
    }

    @Test 
    public void whenChargerExists_thenReturnCharger() throws Exception {
        Station station = new Station();
        station.setName("Test Station");
        station.setLongitude(-74.0060);
        station.setLatitude(40.7128);
        station.setStatus(Station.StationStatus.ENABLED);

        station = stationRepository.save(station);
        stationRepository.flush();

        Charger charger = Charger.builder()
                .station(station)
                .connectorType("Type 2")
                .price(0.5)
                .chargingSpeed(22)
                .status(Charger.ChargerStatus.AVAILABLE)
                .build();

        charger = chargerRepository.save(charger);
        chargerRepository.flush();
        UUID chargerId = charger.getId();
        mockMvc.perform(get("/api/v1/chargers/{chargerId}", chargerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connectorType").value("Type 2"));
    }

    @Test
    public void whenChargerDoesNotExist_thenReturnNotFound() throws Exception {
        UUID chargerId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/chargers/{chargerId}", chargerId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void whenStationDoesNotExist_thenReturnEmptyList() throws Exception {
        UUID stationId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/chargers/station/{stationId}", stationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void whenChargerStatusExists_thenReturnChargerStatus() throws Exception {
        Station station = new Station();
        station.setName("Test Station");
        station.setLongitude(-74.0060);
        station.setLatitude(40.7128);
        station.setStatus(Station.StationStatus.ENABLED);

        station = stationRepository.save(station);
        stationRepository.flush();

        Charger charger = Charger.builder()
                .station(station)
                .connectorType("Type 2")
                .price(0.5)
                .chargingSpeed(22)
                .status(Charger.ChargerStatus.AVAILABLE)
                .build();

        charger = chargerRepository.save(charger);
        chargerRepository.flush();
        UUID chargerId = charger.getId();
        mockMvc.perform(get("/api/v1/chargers/{chargerId}/status", chargerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("AVAILABLE"));
    }


}
