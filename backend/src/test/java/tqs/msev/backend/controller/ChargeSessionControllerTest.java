package tqs.msev.backend.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.msev.backend.entity.ChargeSession;
import tqs.msev.backend.exception.GlobalExceptionHandler;
import tqs.msev.backend.configuration.TestSecurityConfig;
import tqs.msev.backend.service.ChargerService;
import tqs.msev.backend.service.JwtService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChargeSessionController.class)
@Import({GlobalExceptionHandler.class, TestSecurityConfig.class})
class ChargeSessionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChargerService chargerService;

    @MockitoBean
    private JwtService jwtService;

    @WithUserDetails("test")
    @Test
    void whenGetChargeSessions_thenReturnChargeSessions() throws Exception {
        ChargeSession session1 = ChargeSession.builder()
                .startTimestamp(LocalDateTime.now())
                .build();

        ChargeSession session2 = ChargeSession.builder()
                .startTimestamp(LocalDateTime.now())
                .endTimestamp((LocalDateTime.now().plusSeconds(30)))
                .build();

        when(chargerService.getChargeSessions(Mockito.any(), Mockito.anyBoolean())).thenReturn(List.of(session1, session2));

        mockMvc.perform(get("/api/v1/charge-sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
