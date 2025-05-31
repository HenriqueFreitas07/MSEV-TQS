package tqs.msev.backend.it;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import tqs.msev.backend.configuration.TestDatabaseConfig;
import tqs.msev.backend.configuration.TestSecurityConfig;
import tqs.msev.backend.entity.Station;
import tqs.msev.backend.entity.User;
import tqs.msev.backend.exception.GlobalExceptionHandler;
import tqs.msev.backend.repository.StationRepository;
import tqs.msev.backend.repository.UserRepository;
import tqs.msev.backend.service.JwtService;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import({GlobalExceptionHandler.class, TestSecurityConfig.class, TestDatabaseConfig.class})
class StationIT {
    @LocalServerPort
    private int port;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;

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
        stationRepository.deleteAll();
        userRepository.deleteAll();
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
    @WithUserDetails("test_operator")
    void whenCreateStation_thenReturnCreatedStation() {
        Station station = new Station();
        station.setName("New Station");
        station.setAddress("New Address");
        station.setLatitude(40.7128);
        station.setLongitude(-74.0060);

        given()
                .contentType("application/json")
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
    @WithUserDetails("test_operator")
    void whenCreateStationWithInvalidData_thenReturnBadRequest() {
        Station station = new Station();
        station.setName("");
        station.setAddress("New Address");
        station.setLatitude(200.7128);
        station.setLongitude(-74.0060);
        given()
                .contentType("application/json")
                .body(station)
                .when()
                .post("/api/v1/stations")
                .then()
                .assertThat()
                .statusCode(400);
    }
}
