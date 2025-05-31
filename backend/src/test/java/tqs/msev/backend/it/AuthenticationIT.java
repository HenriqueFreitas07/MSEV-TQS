package tqs.msev.backend.it;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import tqs.msev.backend.configuration.TestDatabaseConfig;
import tqs.msev.backend.dto.LoginDTO;
import tqs.msev.backend.dto.SignupDTO;
import tqs.msev.backend.repository.UserRepository;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import(TestDatabaseConfig.class)
class AuthenticationIT {
    @LocalServerPort
    private int port;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @AfterEach
    void reset() {
        RestAssured.reset();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
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

        userRepository.deleteAll();
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
