package tqs.msev.backend.controller;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tqs.msev.backend.service.ChargerService;
import tqs.msev.backend.exception.GlobalExceptionHandler;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import tqs.msev.backend.service.ReservationService;
import tqs.msev.backend.entity.Reservation;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;



@WebMvcTest(ReservationController.class)
@Import(GlobalExceptionHandler.class) 
class ReservationControllerTest {
    
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ChargerService chargerService;

    @MockitoBean
    ReservationService reservationService;

    @Autowired
    ReservationController reservationController;

    @Test
    @Requirement("MSEV-19")
    void whenGetUserReservations_thenReturnReservations() throws Exception {
        UUID userId = UUID.randomUUID();
        List<Reservation> mockReservations = List.of(new Reservation(), new Reservation());
        
        when(reservationService.getUserReservations(userId)).thenReturn(mockReservations);
        
        mockMvc.perform(
            get("/api/v1/reservations/user/{userId}", userId)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0]").isNotEmpty())
        .andExpect(jsonPath("$[1]").isNotEmpty());
    }

    @Test
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
    void whenReservationValid_thenCreateReservation() throws Exception {
        UUID reservationId = UUID.randomUUID();
        Reservation mockReservation = new Reservation();
        
        when(reservationService.createReservation(mockReservation)).thenReturn(mockReservation);
        
        mockMvc.perform(
            get("/api/v1/reservations", reservationId)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @Requirement("MSEV-19")
    void whenReservationNonExistent_thenThrowException() throws Exception {
        UUID reservationId = UUID.randomUUID();
        
        when(reservationService.getReservationById(reservationId)).thenThrow(new NoSuchElementException("Reservation not found"));
        
        mockMvc.perform(
            get("/api/v1/reservations/{reservationId}", reservationId)
        )
        .andExpect(status().isNotFound());
    }

    @Test
    @Requirement("MSEV-19")
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
    void whenReservationExistsAndValid_thenCancelReservation() throws Exception {
        UUID reservationId = UUID.randomUUID();
        Reservation mockReservation = new Reservation();
        
        when(reservationService.cancelReservation(reservationId)).thenReturn(mockReservation);
        
        mockMvc.perform(
            get("/api/v1/reservations/{reservationId}/cancel", reservationId)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @Requirement("MSEV-19")
    void whenReservationNonExistent_thenThrowExceptionOnCancel() throws Exception {
        UUID reservationId = UUID.randomUUID();
        
        when(reservationService.cancelReservation(reservationId)).thenThrow(new NoSuchElementException("Reservation not found"));
        
        mockMvc.perform(
            get("/api/v1/reservations/{reservationId}/cancel", reservationId)
        )
        .andExpect(status().isNotFound());
    }

    @Test
    @Requirement("MSEV-19")
    void whenReservationExists_thenReturnReservation() throws Exception {
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
    void whenReservationExistsAndValid_thenMarkAsUsed() throws Exception {
        UUID reservationId = UUID.randomUUID();
        Reservation mockReservation = new Reservation();
        
        when(reservationService.markReservationAsUsed(reservationId)).thenReturn(mockReservation);
        
        mockMvc.perform(
            get("/api/v1/reservations/{reservationId}/used", reservationId)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isNotEmpty());
    }


   
 
}