package tqs.msev.backend.controller;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tqs.msev.backend.configuration.TestSecurityConfig;
import tqs.msev.backend.entity.Charger;
import tqs.msev.backend.entity.User;
import tqs.msev.backend.exception.GlobalExceptionHandler;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import tqs.msev.backend.service.JwtService;
import tqs.msev.backend.service.ReservationService;
import tqs.msev.backend.entity.Reservation;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@WebMvcTest(ReservationController.class)
@Import({GlobalExceptionHandler.class, TestSecurityConfig.class})
class ReservationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "test")
    @Requirement("MSEV-19")
    void whenGetUserReservations_thenReturnReservations() throws Exception {
        UUID userId = UUID.randomUUID();
        List<Reservation> mockReservations = List.of(new Reservation(), new Reservation());
        
        when(reservationService.getUserReservations(userId)).thenReturn(mockReservations);
        
        mockMvc.perform(
            get("/api/v1/reservations")
                .param("userId", userId.toString())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0]").isNotEmpty())
        .andExpect(jsonPath("$[1]").isNotEmpty());

        verify(reservationService, times(1)).getUserReservations(userId);
    }

    @Test
    @WithMockUser(username = "test")
    @Requirement("MSEV-19")
    void whenReservationValid__thenReturnReservation() throws Exception {
        UUID reservationId = UUID.randomUUID();
        Reservation mockReservation = new Reservation();
        
        when(reservationService.getReservationById(reservationId)).thenReturn(mockReservation);
        
        mockMvc.perform(
            get("/api/v1/reservations/{reservationId}", reservationId)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @Requirement("MSEV-19")
    @WithMockUser(username = "test")
    void whenReservationValid_thenCreateReservation() throws Exception {
        Reservation mockReservation = new Reservation();
        Charger charger = new Charger();
        charger.setId(UUID.randomUUID());
        User user = new User();
        user.setId(UUID.randomUUID());
        mockReservation.setCharger(charger);
        mockReservation.setUser(user);
        mockReservation.setStartTimestamp(LocalDateTime.now());
        mockReservation.setEndTimestamp(LocalDateTime.now().plusHours(5));

        JSONObject json = new JSONObject(mockReservation);

        when(reservationService.createReservation(mockReservation)).thenReturn(mockReservation);
        
        mockMvc.perform(
            post("/api/v1/reservations")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json.toString())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @Requirement("MSEV-19")
    @WithMockUser(username = "test")
    void whenReservationNonExistent_thenThrowException() throws Exception {
        UUID reservationId = UUID.randomUUID();
        
        when(reservationService.getReservationById(reservationId)).thenThrow(new NoSuchElementException("Reservation not found"));
        
        mockMvc.perform(
            get("/api/v1/reservations/{reservationId}", reservationId)
        ).andExpect(status().isNotFound());

        verify(reservationService, times(1)).getReservationById(reservationId);
    }

    @Test
    @Requirement("MSEV-19")
    @WithMockUser(username = "test")
    void whenReservationInPast_thenThrowException() throws Exception {
        UUID reservationId = UUID.randomUUID();
        Reservation mockReservation = new Reservation();
        
        when(reservationService.createReservation(mockReservation)).thenThrow(new IllegalArgumentException("Reservation cannot be in the past"));
        
        mockMvc.perform(
            get("/api/v1/reservations", reservationId)
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    @Requirement("MSEV-19")
    @WithMockUser(username = "test")
    void whenReservationExistsAndValid_thenCancelReservation() throws Exception {
        UUID reservationId = UUID.randomUUID();
        Reservation mockReservation = new Reservation();
        
        when(reservationService.cancelReservation(reservationId)).thenReturn(mockReservation);
        
        mockMvc.perform(
            delete("/api/v1/reservations/{reservationId}", reservationId).with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @Requirement("MSEV-19")
    @WithMockUser(username = "test")
    void whenReservationNonExistent_thenThrowExceptionOnCancel() throws Exception {
        UUID reservationId = UUID.randomUUID();
        
        when(reservationService.cancelReservation(reservationId)).thenThrow(new NoSuchElementException("Reservation not found"));
        
        mockMvc.perform(
            delete("/api/v1/reservations/{reservationId}", reservationId).with(csrf())
        )
        .andExpect(status().isNotFound());
    }

    @Test
    @Requirement("MSEV-19")
    @WithMockUser(username = "test")
    void whenReservationExistsAndValid_thenMarkAsUsed() throws Exception {
        UUID reservationId = UUID.randomUUID();
        Reservation mockReservation = new Reservation();
        
        when(reservationService.markReservationAsUsed(reservationId)).thenReturn(mockReservation);
        
        mockMvc.perform(
            put("/api/v1/reservations/{reservationId}/used", reservationId).with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isNotEmpty());
    }
}