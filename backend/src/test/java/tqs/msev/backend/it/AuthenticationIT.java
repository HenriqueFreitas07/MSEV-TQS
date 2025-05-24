package tqs.msev.backend.it;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tqs.msev.backend.dto.LoginDTO;
import tqs.msev.backend.dto.SignupDTO;

import static io.restassured.RestAssured.given;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthenticationIT {
    @Container
    public static PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17"))
            .withDatabaseName("msev_test");

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("security.jwt.secret-key", () -> "f9924db12318f6a0f1bcfa6e5d0342b65a51022a48a8246cdaa3b1a45493b6b4");
        registry.add("security.jwt.expiration-time", () -> "360000");
    }

    @Test
    @Requirement("MSEV-19")
    @Order(1)
    void whenSignup_thenReturnNoContent() {
        SignupDTO dto = new SignupDTO("Test", "test@gmail.com", "123");
        JSONObject json = new JSONObject(dto);

        given()
                .contentType(ContentType.JSON)
                .body(json.toString())
                .when()
                .post("/api/v1/signup")
                .then()
                .assertThat()
                .statusCode(204);
    }

    @Test
    @Requirement("MSEV-19")
    @Order(2)
    void whenLoginWithValidCredentials_thenReturnCookie() {
        LoginDTO dto = new LoginDTO("test@gmail.com", "123");
        JSONObject json = new JSONObject(dto);

        given()
                .contentType(ContentType.JSON)
                .body(json.toString())
                .when()
                .post("/api/v1/login")
                .then()
                .assertThat()
                .statusCode(204)
                .cookie("accessToken");
    }

    @Test
    @Requirement("MSEV-19")
    @Order(3)
    void whenLoginWithInvalidCredentials_thenReturnUnauthorized() {
        LoginDTO dto = new LoginDTO("test@gmail.com", "1234");
        JSONObject json = new JSONObject(dto);

        given()
                .contentType(ContentType.JSON)
                .body(json.toString())
                .when()
                .post("/api/v1/login")
                .then()
                .assertThat()
                .statusCode(401);
    }
}
