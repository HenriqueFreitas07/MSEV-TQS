package tqs.msev.backend.it;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import tqs.msev.backend.configuration.TestDatabaseConfig;
import tqs.msev.backend.entity.ChargeSession;
import tqs.msev.backend.entity.Charger;
import tqs.msev.backend.entity.Station;
import tqs.msev.backend.entity.User;
import tqs.msev.backend.repository.ChargeSessionRepository;
import tqs.msev.backend.repository.ChargerRepository;
import tqs.msev.backend.repository.StationRepository;
import tqs.msev.backend.repository.UserRepository;
import tqs.msev.backend.service.JwtService;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestDatabaseConfig.class)
class StationIT {
    @LocalServerPort
    private int port;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;

    private RequestSpecification defaultSpec;

    private RequestSpecification operatorSpec;

    @Autowired
    private ChargerRepository chargerRepository;

    @Autowired
    private ChargeSessionRepository chargeSessionRepository;
    

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

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        User user = User.builder()
                .email("test@gmail.com")
                .name("test")
                .password("test")
                .isOperator(false)
                .build();

        user = userRepository.saveAndFlush(user);

        String jwtToken = jwtService.generateToken(user);

        User operator = User.builder()
                .email("test_operator@gmail.com")
                .name("test_operator")
                .password("test_operator")
                .isOperator(true)
                .build();

        defaultSpec = new RequestSpecBuilder()
                .addCookie("accessToken", jwtToken)
                .build();

        user = userRepository.saveAndFlush(operator);
        jwtToken = jwtService.generateToken(user);

        operatorSpec = new RequestSpecBuilder()
                .addCookie("accessToken", jwtToken)
                .build();
    }

    @AfterEach
    void resetDb() {
        chargeSessionRepository.deleteAll();
        chargerRepository.deleteAll();
        stationRepository.deleteAll();
        RestAssured.reset();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("security.jwt.secret-key", () -> "f9924db12318f6a0f1bcfa6e5d0342b65a51022a48a8246cdaa3b1a45493b6b4");
        registry.add("security.jwt.expiration-time", () -> "360000");
    }

    @Test
    @Requirement("MSEV-16")
    void whenGetStations_thenReturnStations() {
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

        stationRepository.saveAllAndFlush(List.of(station1, station2));

        given()
                .spec(defaultSpec)
                .when()
                .get("/api/v1/stations")
                .then()
                .assertThat()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("name", hasItems("Station 1", "Station 2"));
    }

    @Test
    @Requirement("MSEV-16")
    void whenGetStationByValidId_thenReturnStation() {
        Station station1 = new Station();
        station1.setLatitude(40);
        station1.setLongitude(-0.5);
        station1.setName("Station 1");
        station1.setAddress("NY Street, 1");

        station1 = stationRepository.saveAndFlush(station1);

        given()
                .spec(defaultSpec)
                .when()
                .get("/api/v1/stations/" + station1.getId().toString())
                .then()
                .assertThat()
                .statusCode(200)
                .body("name", is("Station 1"));
    }

    @Test
    @Requirement("MSEV-16")
    void whenGetStationByInvalidId_thenReturnNotFound() {
        given()
                .spec(defaultSpec)
                .when()
                .get("/api/v1/stations/" + UUID.randomUUID())
                .then()
                .assertThat()
                .statusCode(404);
    }

    @Test
    @Requirement("MSEV-16")
    void whenSearchStationByValidName_thenReturnStations() {
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

        stationRepository.saveAllAndFlush(List.of(station1, station2));

        given()
                .spec(defaultSpec)
                .queryParam("name", "Station 1")
                .when()
                .get("/api/v1/stations/search-by-name")
                .then()
                .assertThat()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("name", hasItems("Station 1"));
    }

    @Test
    @Requirement("MSEV-16")
    void whenSearchStationByAddress_thenReturnStations() {
        Station station1 = new Station();
        station1.setLatitude(40);
        station1.setLongitude(-0.5);
        station1.setName("Station 1");
        station1.setAddress("NY Street, 1");

        Station station2 = new Station();
        station2.setLatitude(41);
        station2.setLongitude(-8);
        station2.setName("Station 2");
        station2.setAddress("Idk Street, 2");

        stationRepository.saveAllAndFlush(List.of(station1, station2));

        given()
                .spec(defaultSpec)
                .queryParam("address", "Aveiro")
                .when()
                .get("/api/v1/stations/search-by-address")
                .then()
                .assertThat()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("[0].name", is("Station 2"))
                .body("[1].name", is("Station 1"));
    }

    @Test
    @Requirement("MSEV-16")
    void whenSearchStationByInvalidAddress_thenReturnEmptyList() {
        Station station1 = new Station();
        station1.setLatitude(40);
        station1.setLongitude(-0.5);
        station1.setName("Station 1");
        station1.setAddress("NY Street, 1");

        Station station2 = new Station();
        station2.setLatitude(41);
        station2.setLongitude(-8);
        station2.setName("Station 2");
        station2.setAddress("Idk Street, 2");

        stationRepository.saveAllAndFlush(List.of(station1, station2));

        given()
                .spec(defaultSpec)
                .queryParam("address", "gdais dhasdiasd iasd sa ias")
                .when()
                .get("/api/v1/stations/search-by-address")
                .then()
                .assertThat()
                .statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    @Requirement("MSEV-13")
    void whenCreateStation_thenReturnCreatedStation() {
        Station station = new Station();
        station.setName("New Station");
        station.setAddress("New Address");
        station.setLatitude(40.7128);
        station.setLongitude(-74.0060);

        given()
                .spec(operatorSpec)
                .contentType(ContentType.JSON)
                .body(station)
                .when()
                .post("/api/v1/stations")
                .then()
                .assertThat()
                .statusCode(201)
                .body("name", is("New Station"))
                .body("address", is("New Address"))
                .body("latitude", is(40.7128f))
                .body("longitude", is(-74.0060f));
    }

    @Test
    @Requirement("MSEV-13")
    void whenCreateStationWithInvalidData_thenReturnBadRequest() {
        Station station = new Station();
        station.setName("");
        station.setAddress("New Address");
        station.setLatitude(200.7128);
        station.setLongitude(-74.0060);

        given()
                .spec(operatorSpec)
                .contentType(ContentType.JSON)
                .body(station)
                .when()
                .post("/api/v1/stations")
                .then()
                .assertThat()
                .statusCode(400);
    }
    @Test
    @Requirement("MSEV-19")
    void whenOperatorDisabled_thenReturnOk() {
        Station station = new Station();
        station.setName("Test");
        station.setAddress("New Address");
        station.setLatitude(20.7128);
        station.setLongitude(-74.0060);

        Charger charger = new Charger();
        charger.setStation(station);
        charger.setConnectorType("Type 2");
        charger.setStatus(Charger.ChargerStatus.AVAILABLE);
        station.setChargers(List.of(charger));
        station.setStatus(Station.StationStatus.ENABLED);

        Station s =stationRepository.saveAndFlush(station);

        given()
                .spec(operatorSpec)
                .contentType(ContentType.JSON)
                .body(s)
                .when()
                .patch("/api/v1/stations/{id}/disable", s.getId())
                .then()
                .assertThat()
                .statusCode(200);

        Station newS=stationRepository.findById(s.getId()).orElseThrow();
        assertThat(newS.getStatus()).isEqualTo(Station.StationStatus.DISABLED);
    }

    @Test
    @Requirement("MSEV-19")
    void whenUserTriesDisabledStation_thenReturnForbidden() {
        Station station = new Station();
        station.setName("Test");
        station.setAddress("New Address");
        station.setLatitude(20.7128);
        station.setLongitude(-74.0060);
        station.setStatus(Station.StationStatus.ENABLED);
        Charger charger = new Charger();

        charger.setStation(station);
        charger.setConnectorType("Type 2");
        charger.setStatus(Charger.ChargerStatus.AVAILABLE);
        station.setChargers(List.of(charger));

        Station s =stationRepository.saveAndFlush(station);

        given()
                .spec(defaultSpec)
                .contentType(ContentType.JSON)
                .body(s)
                .when()
                .patch("/api/v1/stations/{id}/disable", s.getId())
                .then()
                .assertThat()
                .statusCode(403);

    Station newS=stationRepository.findById(s.getId()).orElseThrow();
    assertThat(newS.getStatus()).isEqualTo(Station.StationStatus.ENABLED);

    }

    @Test
    @Requirement("MSEV-19")
    void whenOperatorEnable_thenReturnOk() {
        Station station = new Station();
        station.setName("Test");
        station.setAddress("New Address");
        station.setLatitude(20.7128);
        station.setLongitude(-74.0060);
        Charger charger = new Charger();
        charger.setStation(station);
        charger.setConnectorType("Type 2");
        charger.setStatus(Charger.ChargerStatus.TEMPORARILY_DISABLED);
        station.setChargers(List.of(charger));
        station.setStatus(Station.StationStatus.DISABLED);

        Station s =stationRepository.saveAndFlush(station);

        given()
                .spec(operatorSpec)
                .contentType(ContentType.JSON)
                .body(s)
                .when()
                .patch("/api/v1/stations/{id}/enable", s.getId())
                .then()
                .assertThat()
                .statusCode(200);

        Station newS=stationRepository.findById(s.getId()).orElseThrow();
        assertThat(newS.getStatus()).isEqualTo(Station.StationStatus.ENABLED);
    }

    @Test
    @Requirement("MSEV-19")
    void whenUserTriesEnableStation_thenReturnForbidden() {
        Station station = new Station();
        station.setName("Test");
        station.setAddress("New Address");
        station.setLatitude(20.7128);
        station.setLongitude(-74.0060);
        station.setStatus(Station.StationStatus.DISABLED);
        Charger charger = new Charger();
        charger.setConnectorType("Type 2");
        charger.setStatus(Charger.ChargerStatus.TEMPORARILY_DISABLED);
        charger.setStation(station);
        station.setChargers(List.of(charger));
        Station s =stationRepository.saveAndFlush(station);

        given()
                .spec(defaultSpec)
                .contentType(ContentType.JSON)
                .body(s)
                .when()
                .patch("/api/v1/stations/{id}/disable", s.getId())
                .then()
                .assertThat()
                .statusCode(403);

        Station newS=stationRepository.findById(s.getId()).orElseThrow();
        assertThat(newS.getStatus()).isEqualTo(Station.StationStatus.DISABLED);
    }

    @Test
    @Requirement("MSEV-25")
    @WithUserDetails("test_operator")
    void whenGetStationStats_thenReturnStats() {
        Station station = new Station();
        station.setName("Test Station");
        station.setAddress("Test Address");
        station.setLatitude(40.7128);
        station.setLongitude(-74.0060);
        station.setStatus(Station.StationStatus.ENABLED);

        station = stationRepository.saveAndFlush(station);

        Charger charger1 = new Charger();
        charger1.setConnectorType("Type 2");
        charger1.setStatus(Charger.ChargerStatus.AVAILABLE);
        charger1.setStation(station);

        chargerRepository.saveAndFlush(charger1);

        Charger charger2 = new Charger();
        charger2.setConnectorType("CCS");
        charger2.setStatus(Charger.ChargerStatus.AVAILABLE);
        charger2.setStation(station);



        chargerRepository.saveAndFlush(charger1);

        station.setChargers(List.of(charger1, charger2));

        stationRepository.saveAndFlush(station);

        User user = new User();
        user.setEmail("teststats@gmail.com");
        user.setName("Test Stats");
        user.setPassword("teststats");
        user.setOperator(false);
        user = userRepository.saveAndFlush(user);

        ChargeSession session1 = new ChargeSession();
        session1.setCharger(charger1);
        session1.setStartTimestamp(LocalDateTime.now().minusHours(2));
        session1.setEndTimestamp(LocalDateTime.now().minusHours(1));
        session1.setUser(user);

        ChargeSession session2 = new ChargeSession();
        session2.setCharger(charger2);
        session2.setStartTimestamp(LocalDateTime.now().minusHours(3));
        session2.setEndTimestamp(null);
        session2.setUser(user);

        chargeSessionRepository.saveAllAndFlush(List.of(session1, session2));

        given()
                .spec(operatorSpec)
                .when()
                .get("/api/v1/stations/stats/{stationId}", station.getId())
                .then()
                .assertThat()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].charger.id", is(charger1.getId().toString()))
                .body("[0].consumption", is(0))
                .body("[0].chargingSpeed", is(0));

    }

}
