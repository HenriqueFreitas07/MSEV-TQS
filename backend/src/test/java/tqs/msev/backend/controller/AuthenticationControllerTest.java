package tqs.msev.backend.controller;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.msev.backend.configuration.ApplicationConfig;
import tqs.msev.backend.configuration.JwtAuthFilter;
import tqs.msev.backend.configuration.SecurityConfig;
import tqs.msev.backend.dto.LoginDTO;
import tqs.msev.backend.entity.User;
import tqs.msev.backend.repository.UserRepository;
import tqs.msev.backend.service.AuthService;
import tqs.msev.backend.service.JwtService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@Import({SecurityConfig.class, ApplicationConfig.class, JwtAuthFilter.class})
class AuthenticationControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AuthService service;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private static final String MOCK_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0ZUBnbWFpbC5jb20iLCJpYXQiOjE3NDgwOTA5NTcsImV4cCI6MTc0ODA5MTMxN30.vHMfwIRJ77F5I_BkjSV8cxYRT9q6wScCVmLEnxWXvA8";

    @Test
    void whenLoginWithValidCredentials_thenReturnTokenCookie() throws Exception {
        User user = User.builder()
                .name("Test")
                .email("test@gmail.com")
                .password("123")
                .build();

        when(service.authenticate(Mockito.any())).thenReturn(user);
        when(jwtService.generateToken(Mockito.any())).thenReturn(MOCK_TOKEN);
        when(jwtService.getExpirationTime()).thenReturn(360000L);

        LoginDTO dto = new LoginDTO("test@gmail.com", "123");
        JSONObject json = new JSONObject(dto);

        mvc.perform(post("/api/v1/login").contentType(MediaType.APPLICATION_JSON).content(json.toString()))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().maxAge("accessToken", 360000 / 1000))
                .andExpect(cookie().value("accessToken", MOCK_TOKEN));

        verify(jwtService, times(1)).getExpirationTime();
        verify(jwtService, times(1)).generateToken(Mockito.any());
        verify(service, times(1)).authenticate(Mockito.any());
    }

    @Test
    void whenLoginWithInvalidCredentials_thenReturnUnauthorized() throws Exception {
        when(service.authenticate(Mockito.any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        LoginDTO dto = new LoginDTO("aa@gmail.com", "1234");
        JSONObject json = new JSONObject(dto);

        mvc.perform(post("/api/v1/login").contentType(MediaType.APPLICATION_JSON).content(json.toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@gmail.com")
    void whenLogout_thenReturnExpiredCookie() throws Exception {
        mvc.perform(post("/api/v1/logout"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().maxAge("accessToken", 0));
    }
}
