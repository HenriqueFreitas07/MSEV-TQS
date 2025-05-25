package tqs.msev.backend.controller;

import static org.mockito.Mockito.when;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tqs.msev.backend.service.ChargerService;
import tqs.msev.backend.entity.Charger;
import tqs.msev.backend.exception.GlobalExceptionHandler;
import java.util.UUID;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.containsString;
import tqs.msev.backend.entity.Reservation;
import tqs.msev.backend.service.JwtService;
import tqs.msev.backend.service.ReservationService;

@WebMvcTest(ChargerController.class)
@Import(GlobalExceptionHandler.class) 
class ChargerControllerTest {
    
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ChargerService chargerService;


    @MockitoBean
    ReservationService reservationService;

    @MockitoBean
    private JwtService jwtService;


    @Autowired
    ChargerController chargerController;


    @Test
    @WithMockUser(username = "test")
    void whenStationExists_thenReturnChargers() throws Exception {

        UUID stationId = UUID.randomUUID();
        List<Charger> mockChargers = List.of(new Charger(), new Charger());
        
        when(chargerService.getChargersByStation(stationId)).thenReturn(mockChargers);
        
        mockMvc.perform(
            get("/api/v1/chargers/station/{stationId}", stationId)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0]").isNotEmpty())
        .andExpect(jsonPath("$[1]").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "test")
    void whenChargerExists_thenReturnCharger()  throws Exception {
        UUID chargerId = UUID.randomUUID();
        Charger mockCharger = new Charger();
        mockCharger.setStatus(Charger.ChargerStatus.AVAILABLE);
        
        when(chargerService.getChargerById(chargerId)).thenReturn(mockCharger);
        
        mockMvc.perform(
            get("/api/v1/chargers/{chargerId}", chargerId)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isNotEmpty())
        .andExpect(jsonPath("$").value(mockCharger))
        .andExpect(jsonPath("$.status").value(Charger.ChargerStatus.AVAILABLE.toString()));       
    }

    @Test
    @WithMockUser(username = "test")
    void whenChargerDoesNotExist_thenThrowException() throws Exception {
        UUID chargerId = UUID.randomUUID();
        
        when(chargerService.getChargerById(chargerId)).thenThrow(new NoSuchElementException("Charger not found"));
        
        mockMvc.perform(
            get("/api/v1/chargers/{chargerId}", chargerId)
        )
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("Charger not found")));
    }

    @Test
    @WithMockUser(username = "test")
    void whenThereAreNoCloseReservations__thenReturnEmptyList() throws Exception {
        UUID chargerId = UUID.randomUUID();
        List<Reservation> mockReservations = List.of();
        
        when(reservationService.getFutureReservationsOnCharger(chargerId)).thenReturn(mockReservations);
        
        mockMvc.perform(
            get("/api/v1/chargers/{chargerId}/reservations", chargerId)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(username = "test")
    void whenThereAreCloseReservations__thenReturnList() throws Exception {
        UUID chargerId = UUID.randomUUID();
        List<Reservation> mockReservations = List.of(new Reservation(), new Reservation());
        
        when(reservationService.getFutureReservationsOnCharger(chargerId)).thenReturn(mockReservations);
        
        mockMvc.perform(
            get("/api/v1/chargers/{chargerId}/reservations", chargerId)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0]").isNotEmpty())
        .andExpect(jsonPath("$[1]").isNotEmpty());
    }
}