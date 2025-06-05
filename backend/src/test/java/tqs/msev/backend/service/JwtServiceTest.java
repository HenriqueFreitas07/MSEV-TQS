package tqs.msev.backend.service;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tqs.msev.backend.entity.User;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {
    private static final String EXPIRED_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0ZUBnbWFpbC5jb20iLCJpYXQiOjE3NDgwOTA5NTcsImV4cCI6MTc0ODA5MTMxN30.vHMfwIRJ77F5I_BkjSV8cxYRT9q6wScCVmLEnxWXvA8";

    private final JwtService service = new JwtService();

    @BeforeEach
    void setup() throws Exception {
        Field secretKey = JwtService.class.getDeclaredField("secretKey");
        secretKey.setAccessible(true);
        secretKey.set(service, "f9924db12318f6a0f1bcfa6e5d0342b65a51022a48a8246cdaa3b1a45493b6b4");

        Field expiration = JwtService.class.getDeclaredField("jwtExpiration");
        expiration.setAccessible(true);
        expiration.set(service, 3600000L);
    }

    @Test
    void whenGenerateToken_thenReturnValidToken() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@gmail.com")
                .password("123")
                .build();

        String token = service.generateToken(user);

        assertThat(service.extractUsername(token)).isEqualTo("test@gmail.com");
    }

    @Test
    void whenValidateAgainstInvalidUser_thenReturnInvalidToken() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@gmail.com")
                .password("123")
                .build();

        String token = service.generateToken(user);

        user.setEmail("teste@gmail.com");

        assertThat(service.isTokenValid(token, user)).isFalse();
    }

    @Test
    void whenVerifyForExpiredToken_thenReturnInvalid() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("teste@gmail.com")
                .password("123")
                .build();

        assertThatThrownBy(() -> service.isTokenValid(EXPIRED_TOKEN, user)).isInstanceOf(ExpiredJwtException.class);
    }
}
