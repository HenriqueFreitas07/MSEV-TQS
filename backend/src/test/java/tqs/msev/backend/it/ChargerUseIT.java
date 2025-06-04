package tqs.msev.backend.it;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import tqs.msev.backend.configuration.TestDatabaseConfig;
import tqs.msev.backend.entity.*;
import tqs.msev.backend.repository.*;
import tqs.msev.backend.service.JwtService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestDatabaseConfig.class)
class ChargerUseIT {
    @LocalServerPort
    private int port;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReservationRepository reservationRepository;
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
                .name("Test")
                .password("test")
                .isOperator(false)
                .build();

        user = userRepository.saveAndFlush(user);
        

        String jwtToken = jwtService.generateToken(user);

        RestAssured.requestSpecification = new RequestSpecBuilder()
                .addCookie("accessToken", jwtToken)
                .build();
    }

    @AfterEach
    void resetDb() {
        reservationRepository.deleteAll();
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
        registry.add("geocoding.apikey", () -> "369f40db67394240aa5439ecf07af5b1");
    }

    @Test
    @Requirement("MSEV-20")
    void whenUnlockInUseChargerWithValidReservation_thenUnlock() {
        Station station1 = new Station();
        station1.setLatitude(40);
        station1.setLongitude(-0.5);
        station1.setName("Station 1");
        station1.setAddress("NY Street, 1");

        station1 = stationRepository.saveAndFlush(station1);

        Charger charger = Charger.builder()
                .station(station1)
                .price(30)
                .chargingSpeed(10)
                .connectorType("Type 2")
                .status(Charger.ChargerStatus.IN_USE)
                .build();

        charger = chargerRepository.saveAndFlush(charger);

        User user2 = User.builder()
                .name("Teste")
                .email("test2@gmail.com")
                .password("123")
                .isOperator(false)
                .build();

        user2 = userRepository.saveAndFlush(user2);

        ChargeSession session = ChargeSession.builder()
                .charger(charger)
                .user(user2)
                .startTimestamp(LocalDateTime.now())
                .build();

        chargeSessionRepository.saveAndFlush(session);

        Reservation reservation = Reservation.builder()
                .user(userRepository.findUserByEmail("test@gmail.com").get())
                .charger(charger)
                .startTimestamp(LocalDateTime.now())
                .endTimestamp(LocalDateTime.now().plusHours(1))
                .build();

        reservationRepository.saveAndFlush(reservation);

        given()
                .when()
                .patch("/api/v1/chargers/{chargerId}/unlock", charger.getId())
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    @Requirement("MSEV-20")
    void whenUnlockChargerOfAnotherUserWithoutReservation_thenDontUnlock() {
        Station station1 = new Station();
        station1.setLatitude(40);
        station1.setLongitude(-0.5);
        station1.setName("Station 1");
        station1.setAddress("NY Street, 1");

        station1 = stationRepository.saveAndFlush(station1);

        Charger charger = Charger.builder()
                .station(station1)
                .price(30)
                .chargingSpeed(10)
                .connectorType("Type 2")
                .status(Charger.ChargerStatus.IN_USE)
                .build();

        charger = chargerRepository.saveAndFlush(charger);

        User user2 = User.builder()
                .name("Teste")
                .email("test2@gmail.com")
                .password("123")
                .isOperator(false)
                .build();

        user2 = userRepository.saveAndFlush(user2);

        ChargeSession session = ChargeSession.builder()
                .charger(charger)
                .user(user2)
                .startTimestamp(LocalDateTime.now())
                .build();

        chargeSessionRepository.saveAndFlush(session);

        given()
                .when()
                .patch("/api/v1/chargers/{chargerId}/unlock", charger.getId())
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", containsString("in use"));
    }

    @Test
    @Requirement("MSEV-20")
    void whenLockMyCharger_thenLock() {
        Station station1 = new Station();
        station1.setLatitude(40);
        station1.setLongitude(-0.5);
        station1.setName("Station 1");
        station1.setAddress("NY Street, 1");

        station1 = stationRepository.saveAndFlush(station1);

        Charger charger = Charger.builder()
                .station(station1)
                .price(30)
                .chargingSpeed(10)
                .connectorType("Type 2")
                .status(Charger.ChargerStatus.IN_USE)
                .build();

        charger = chargerRepository.saveAndFlush(charger);

        User user = userRepository.findUserByEmail("test@gmail.com").get();

        ChargeSession session = ChargeSession.builder()
                .charger(charger)
                .user(user)
                .startTimestamp(LocalDateTime.now().minus(10, ChronoUnit.MINUTES))
                .build();

        chargeSessionRepository.saveAndFlush(session);

        given()
                .when()
                .patch("/api/v1/chargers/{chargerId}/lock", charger.getId())
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    @Requirement("MSEV-20")
    void whenUnlockChargerOutOfOrder_thenDontUnlock() {
        Station station1 = new Station();
        station1.setLatitude(40);
        station1.setLongitude(-0.5);
        station1.setName("Station 1");
        station1.setAddress("NY Street, 1");

        station1 = stationRepository.saveAndFlush(station1);

        Charger charger = Charger.builder()
                .station(station1)
                .price(30)
                .chargingSpeed(10)
                .connectorType("Type 2")
                .status(Charger.ChargerStatus.OUT_OF_ORDER)
                .build();

        charger = chargerRepository.saveAndFlush(charger);

        given()
                .when()
                .patch("/api/v1/chargers/{chargerId}/unlock", charger.getId())
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", containsString("out of order"));
    }

    @Test
    @Requirement("MSEV-20")
    void whenUnlockAvailableCharger_thenUnlock() {
        Station station1 = new Station();
        station1.setLatitude(40);
        station1.setLongitude(-0.5);
        station1.setName("Station 1");
        station1.setAddress("NY Street, 1");

        station1 = stationRepository.saveAndFlush(station1);

        Charger charger = Charger.builder()
                .station(station1)
                .price(30)
                .chargingSpeed(10)
                .connectorType("Type 2")
                .status(Charger.ChargerStatus.AVAILABLE)
                .build();

        charger = chargerRepository.saveAndFlush(charger);

        given()
                .when()
                .patch("/api/v1/chargers/{chargerId}/unlock", charger.getId())
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    @Requirement("MSEV-20")
    void whenLockChargerOfAnotherUser_thenDontLock() {
        Station station1 = new Station();
        station1.setLatitude(40);
        station1.setLongitude(-0.5);
        station1.setName("Station 1");
        station1.setAddress("NY Street, 1");

        station1 = stationRepository.saveAndFlush(station1);

        Charger charger = Charger.builder()
                .station(station1)
                .price(30)
                .chargingSpeed(10)
                .connectorType("Type 2")
                .status(Charger.ChargerStatus.IN_USE)
                .build();

        charger = chargerRepository.saveAndFlush(charger);

        User user2 = User.builder()
                .name("Teste")
                .email("test2@gmail.com")
                .password("123")
                .isOperator(false)
                .build();

        user2 = userRepository.saveAndFlush(user2);

        ChargeSession session = ChargeSession.builder()
                .charger(charger)
                .user(user2)
                .startTimestamp(LocalDateTime.now())
                .build();

        chargeSessionRepository.saveAndFlush(session);

        given()
                .when()
                .patch("/api/v1/chargers/{chargerId}/lock", charger.getId())
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", containsString("another user"));
    }

    @Test
    @Requirement("MSEV-25")
    @WithUserDetails("test_operator")
    void whenGetChargerStats_thenReturnChargeSessions() {
        Station station1 = new Station();
        station1.setLatitude(40);
        station1.setLongitude(-0.5);
        station1.setName("Station 1");
        station1.setAddress("NY Street, 1");

        station1 = stationRepository.saveAndFlush(station1);

        Charger charger = Charger.builder()
                .station(station1)
                .price(30)
                .chargingSpeed(10)
                .connectorType("Type 2")
                .status(Charger.ChargerStatus.IN_USE)
                .build();

        charger = chargerRepository.saveAndFlush(charger);
         User user2 = User.builder()
                .name("Teste")
                .email("test2@gmail.com")
                .password("123")
                .isOperator(false)
                .build();

        user2 = userRepository.saveAndFlush(user2);
        ChargeSession session = ChargeSession.builder()
                .charger(charger)
                .user(user2)
                .startTimestamp(LocalDateTime.now())
                .endTimestamp(LocalDateTime.now().plusHours(1))
                .build();
        chargeSessionRepository.saveAndFlush(session);
        given()
                .when()
                .get("/api/v1/charge-sessions/stats/{chargerId}", charger.getId())
                .then()
                .assertThat()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].charger.id", equalTo(charger.getId().toString()))
                .body("[0].user.id", equalTo(user2.getId().toString()))
                .body("[0].startTimestamp", notNullValue())
                .body("[0].endTimestamp", notNullValue());
    }

}
