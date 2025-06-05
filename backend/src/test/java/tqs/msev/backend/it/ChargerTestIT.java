package tqs.msev.backend.it;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;

import tqs.msev.backend.configuration.TestDatabaseConfig;
import tqs.msev.backend.entity.Charger;
import tqs.msev.backend.repository.ChargerRepository;
import tqs.msev.backend.repository.StationRepository;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;
import tqs.msev.backend.entity.Station;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import tqs.msev.backend.repository.UserRepository;
import tqs.msev.backend.repository.ReservationRepository;
import tqs.msev.backend.entity.Reservation;
import tqs.msev.backend.entity.User;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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

    @BeforeAll
    void beforeAll() {
        User operator = User.builder()
                .email("test_operator")
                .name("test_operator")
                .password("test_operator")
                .isOperator(true)
                .build();
        userRepository.saveAndFlush(operator);

        User user = User.builder()
                .email("test")
                .name("test")
                .password("test_user")
                .isOperator(false)
                .build();
        userRepository.saveAndFlush(user);
    }

    @AfterAll
    void afterAll() {
        userRepository.deleteAll();
    }

    @AfterEach
    void resetDb() {
        reservationRepository.deleteAll();
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
    @Requirement("MSEV-19")
    @WithUserDetails("test_operator")
    void whenUserIsOperator_thenDisableCharger() throws Exception {
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
        mockMvc.perform(patch("/api/v1/chargers/{chargerId}/disable", chargerId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/chargers/{chargerId}", chargerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TEMPORARILY_DISABLED"));
    }
    @Test
    @Requirement("MSEV-19")
    @WithUserDetails("test")
    void whenUserNotOperator_thenNotAllowDisableCharger() throws Exception {
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
        mockMvc.perform(patch("/api/v1/chargers/{chargerId}/disable", chargerId))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/chargers/{chargerId}", chargerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
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
        LocalDateTime nowPlusOneHour = LocalDateTime.now().plusHours(1);
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

        Gson gson = new Gson();

        mockMvc.perform(post("/api/v1/chargers").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(gson.toJson(charger)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.connectorType").value("Type 2"))
            .andExpect(jsonPath("$.price").value(0.5))
            .andExpect(jsonPath("$.chargingSpeed").value(22))
            .andExpect(jsonPath("$.status").value("AVAILABLE"))
            .andExpect(jsonPath("$.station.id").value(station.getId().toString()));
    }

    @Test
    @Requirement("MSEV-13")
    @WithUserDetails("test_operator")
    void whenCreateChargerWithInvalidData_thenReturnBadRequest() throws Exception {
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

        Gson gson = new Gson();

        mockMvc.perform(post("/api/v1/chargers").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(gson.toJson(charger)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Requirement("MSEV-25")
    @WithUserDetails("test_operator")
    void whenUpdateChargerPrice_thenReturnUpdatedCharger() throws Exception {
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
        
        mockMvc.perform(put("/api/v1/chargers/{chargerId}/update", chargerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("0.7")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(chargerId.toString()))
            .andExpect(jsonPath("$.price").value(0.7));
    }

    @Test
    @Requirement("MSEV-25")
    @WithUserDetails("test_operator")
    void whenUpdateChargerPriceWithInvalidData_thenReturnBadRequest() throws Exception {
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
        
        mockMvc.perform(put("/api/v1/chargers/{chargerId}/update", chargerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("-1.0")
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }
}
