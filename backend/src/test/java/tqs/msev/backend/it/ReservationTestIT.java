package tqs.msev.backend.it;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import java.time.Duration;

import org.springframework.boot.test.context.SpringBootTest;
import tqs.msev.backend.entity.Charger;
import tqs.msev.backend.repository.ChargerRepository;
import tqs.msev.backend.repository.StationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import tqs.msev.backend.entity.Station;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import tqs.msev.backend.repository.UserRepository;
import tqs.msev.backend.repository.ReservationRepository;
import tqs.msev.backend.entity.Reservation;
import tqs.msev.backend.entity.User;


@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ReservationTestIT {
    
    @Autowired
    private ChargerRepository chargerRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

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
    void resetDb() {
        reservationRepository.deleteAll();
        userRepository.deleteAll();
        chargerRepository.deleteAll();
        stationRepository.deleteAll();
    }

    @Test
    @Requirement("MSEV-19")
    void whenUserExists_thenReturnReservations() throws Exception {
        Station station = new Station();
        station.setName("Test Station");
        station.setLongitude(-74.0060);
        station.setLatitude(40.7128);
        station.setAddress("Idk St.");
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
        User user = User.builder()
                .email("test@gmail.com")
                .password("password")
                .name("test")
                .isOperator(false)
                .build();
        userRepository.save(user);
        userRepository.flush();
        Date now = new Date();
        Date nowPlusOneHour = new Date(now.getTime() + 3600000);
        Reservation reservation = Reservation.builder()
                .charger(charger)
                .user(user)
                .startTimestamp(nowPlusOneHour)
                .endTimestamp(nowPlusOneHour)
                .build();
        reservationRepository.save(reservation);
        reservationRepository.flush();
        mockMvc.perform(get("/api/v1/reservations/user/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reservation.getId().toString()))
                .andExpect(jsonPath("$[0].user.id").value(user.getId().toString()))
                .andExpect(jsonPath("$[0].charger.id").value(charger.getId().toString()));
    }

    

    @Test
    @Requirement("MSEV-19")
    void whenReservationUnexistent_thenReturnNotFound() throws Exception {
        UUID reservationId = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/reservations/" + reservationId+"/cancel"))
                .andExpect(status().isNotFound());
    }

}