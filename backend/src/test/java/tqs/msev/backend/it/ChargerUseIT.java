package tqs.msev.backend.it;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tqs.msev.backend.entity.*;
import tqs.msev.backend.repository.*;
import tqs.msev.backend.service.JwtService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChargerUseIT {
    @Container
    public static PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17"))
            .withDatabaseName("msev_test");

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

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        User user = userRepository.findUserByEmail("test@gmail.com").orElse(null);

        if (user == null) {
            user = User.builder()
                    .email("test@gmail.com")
                    .name("Test")
                    .password("test")
                    .isOperator(false)
                    .build();

            user = userRepository.save(user);
        }

        String jwtToken = jwtService.generateToken(user);

        RestAssured.requestSpecification = new RequestSpecBuilder()
                .addCookie("accessToken", jwtToken)
                .build();
    }

    @AfterEach
    void resetDb() {
        reservationRepository.deleteAll();
        chargeSessionRepository.deleteAll();
        stationRepository.deleteAll();

        Optional<User> user = userRepository.findUserByEmail("test2@gmail.com");

        user.ifPresent(user2 -> userRepository.delete(user2));
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
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
                .startTimestamp(new Date())
                .endTimestamp(new Date(new Date().getTime() + 3600000))
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
}
