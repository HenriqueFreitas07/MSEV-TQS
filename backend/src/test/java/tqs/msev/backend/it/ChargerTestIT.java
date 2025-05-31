package tqs.msev.backend.it;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static io.restassured.RestAssured.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;
import tqs.msev.backend.repository.UserRepository;
import tqs.msev.backend.repository.ReservationRepository;
import tqs.msev.backend.entity.Reservation;
import tqs.msev.backend.entity.User;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestDatabaseConfig.class)
class ChargerTestIT {
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
    @Requirement("MSEV-18")
    @WithMockUser(username = "test")
    void whenStationExists_thenReturnChargers() throws Exception {
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
    @Requirement("MSEV-18")
    @WithMockUser(username = "test")
    void whenChargerExists_thenReturnCharger() throws Exception {
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
        UUID chargerId = charger.getId();
        mockMvc.perform(get("/api/v1/chargers/{chargerId}", chargerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connectorType").value("Type 2"));
    }

    @Test
    @Requirement("MSEV-18")
    @WithMockUser(username = "test")
    void whenChargerDoesNotExist_thenReturnNotFound() throws Exception {
        UUID chargerId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/chargers/{chargerId}", chargerId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Requirement("MSEV-18")
    @WithMockUser(username = "test")
    void whenStationDoesNotExist_thenReturnEmptyList() throws Exception {
        UUID stationId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/chargers/station/{stationId}", stationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Requirement("MSEV-17")
    @WithMockUser(username = "test")
    void whenThereAreCloseReservations__thenReturnList() throws Exception {
        Station station = new Station();
        station.setName("Test Station");
        station.setLongitude(-74.0060);
        station.setLatitude(40.7128);
        station.setStatus(Station.StationStatus.ENABLED);
        station.setAddress("Idk St.");
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
        mockMvc.perform(get("/api/v1/chargers/{chargerId}/reservations", charger.getId()))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Requirement("MSEV-17")
    @WithMockUser(username = "test")
    void whenThereAreNoCloseReservations__thenReturnEmptyList() throws Exception {
        Station station = new Station();
        station.setName("Test Station");
        station.setLongitude(-74.0060);
        station.setLatitude(40.7128);
        station.setStatus(Station.StationStatus.ENABLED);
        station.setAddress("Idk St.");
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
        mockMvc.perform(get("/api/v1/chargers/{chargerId}/reservations", charger.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

        @Test
        @Requirement("MSEV-13")
        @WithUserDetails("test_operator")
        void whenCreateCharger_thenReturnCreatedCharger() throws Exception {
                User operator = User.builder()
                .email("test_operator")
                .name("test_operator")
                .password("test_operator")
                .isOperator(true)
                .build();
                userRepository.saveAndFlush(operator);
                Station station = new Station();
                station.setName("New Station");
                station.setAddress("New Address");
                station.setLatitude(40.7128);
                station.setLongitude(-74.0060);
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
        
                given()
                        .contentType("application/json")
                        .body(charger)
                        .when()
                        .post("/api/v1/chargers")
                        .then()
                        .assertThat()
                        .statusCode(201)
                        .body("connectorType", is("Type 2"))
                        .body("price", is(0.5f))
                        .body("chargingSpeed", is(22))
                        .body("status", is("AVAILABLE"))
                        .body("station.id", is(station.getId().toString()))
                        .body("station.name", is("New Station"))
                        .body("station.address", is("New Address"))
                        .body("station.latitude", is(40.7128f))
                        .body("station.longitude", is(-74.0060f));
        }

        @Test
        @Requirement("MSEV-13")
        @WithUserDetails("test_operator")
        void whenCreateChargerWithInvalidData_thenReturnBadRequest() throws Exception {
                User operator = User.builder()
                .email("test_operator")
                .name("test_operator")
                .password("test_operator")
                .isOperator(true)
                .build();
                userRepository.saveAndFlush(operator);
                Station station = new Station();
                station.setName("New Station");
                station.setAddress("New Address");
                station.setLatitude(40.7128);
                station.setLongitude(-74.0060);
                station.setStatus(Station.StationStatus.ENABLED);
                station = stationRepository.save(station);
                stationRepository.flush();
        
                Charger charger = Charger.builder()
                        .station(station)
                        .connectorType("") 
                        .price(-1.0)
                        .chargingSpeed(22)
                        .status(Charger.ChargerStatus.AVAILABLE)
                        .build();
        
                given()
                        .contentType("application/json")
                        .body(charger)
                        .when()
                        .post("/api/v1/chargers")
                        .then()
                        .assertThat()
                        .statusCode(400);
        }
                
}
