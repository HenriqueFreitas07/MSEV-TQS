package tqs.msev.backend.it;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import tqs.msev.backend.configuration.TestDatabaseConfig;
import tqs.msev.backend.entity.Charger;
import tqs.msev.backend.repository.ChargerRepository;
import tqs.msev.backend.repository.StationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Date;
import java.util.UUID;
import tqs.msev.backend.entity.Station;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import tqs.msev.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import tqs.msev.backend.repository.ReservationRepository;
import tqs.msev.backend.entity.Reservation;
import tqs.msev.backend.entity.User;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestDatabaseConfig.class)
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

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.hibernate.ddl-auto", ()->"create-drop");
        registry.add("security.jwt.secret-key", () -> "f9924db12318f6a0f1bcfa6e5d0342b65a51022a48a8246cdaa3b1a45493b6b4");
        registry.add("security.jwt.expiration-time", () -> "360000");
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
    @WithMockUser(username = "test")
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
        mockMvc.perform(get("/api/v1/reservations" + "?userId=" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reservation.getId().toString()))
                .andExpect(jsonPath("$[0].user.id").value(user.getId().toString()))
                .andExpect(jsonPath("$[0].charger.id").value(charger.getId().toString()));
    }

    @Test
    @Requirement("MSEV-19")
    @WithMockUser(username = "test")
    void whenReservationUnexistent_thenReturnNotFound() throws Exception {
        UUID reservationId = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/reservations/" + reservationId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Requirement("MSEV-19")
    @WithMockUser(username = "test")
    void whenReservationExists_thenReturnReservation() throws Exception {
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
        mockMvc.perform(get("/api/v1/reservations/" + reservation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservation.getId().toString()))
                .andExpect(jsonPath("$.user.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.charger.id").value(charger.getId().toString()));
    }

    @Test
    @Requirement("MSEV-19")
    @WithMockUser(username = "test")
    void whenReservationValidAndUnused_thenMarkAsUsed() throws Exception {
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
                .startTimestamp(now)
                .endTimestamp(nowPlusOneHour)
                .build();
        reservationRepository.save(reservation);
        reservationRepository.flush();
        mockMvc.perform(put("/api/v1/reservations/" + reservation.getId() + "/used"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservation.getId().toString()))
                .andExpect(jsonPath("$.user.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.charger.id").value(charger.getId().toString()))
                .andExpect(jsonPath("$.used").value(true));
    }

    @Test
    @Requirement("MSEV-19")
    @WithMockUser(username = "test")
    void whenReservationIsValid_thenCreateReservation() throws Exception {
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
        Date inThirtyMinutes = new Date(System.currentTimeMillis() + 1800000);
        Date nowPlusOneHour = new Date(inThirtyMinutes.getTime() + 3600000);
        Reservation reservation = Reservation.builder()
                .charger(charger)
                .user(user)
                .startTimestamp(inThirtyMinutes)
                .endTimestamp(nowPlusOneHour)
                .build();
        mockMvc.perform(post("/api/v1/reservations")
                .contentType("application/json")
                .content(new ObjectMapper().writeValueAsString(reservation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty());

    }

    @Test
    @Requirement("MSEV-19")
    @WithMockUser(username = "test")
    void whenChargerEXists_thenReturnReservations() throws Exception{
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
        Date inThirtyMinutes = new Date(System.currentTimeMillis() + 1800000);
        Date nowPlusOneHour = new Date(inThirtyMinutes.getTime() + 3600000);
        Reservation reservation = Reservation.builder()
                .charger(charger)
                .user(user)
                .startTimestamp(inThirtyMinutes)
                .endTimestamp(nowPlusOneHour)
                .build();
        reservationRepository.save(reservation);
        reservationRepository.flush();
        mockMvc.perform(get("/api/v1/reservations" + "?chargerId=" + charger.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reservation.getId().toString()))
                .andExpect(jsonPath("$[0].user.id").value(user.getId().toString()))
                .andExpect(jsonPath("$[0].charger.id").value(charger.getId().toString()));
    }


}