package tqs.msev.backend.it;

import io.restassured.RestAssured;
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
import tqs.msev.backend.entity.Station;
import tqs.msev.backend.repository.StationRepository;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StationIT {
    @Container
    public static PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17"))
            .withDatabaseName("msev_test");

    @LocalServerPort
    private int port;

    @Autowired
    private StationRepository stationRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @AfterEach
    void resetDb() {
        stationRepository.deleteAll();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Test
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
    void whenGetStationByInvalidId_thenReturnNotFound() {
        given()
                .when()
                .get("/api/v1/stations/" + UUID.randomUUID().toString())
                .then()
                .assertThat()
                .statusCode(404);
    }

    @Test
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
}
