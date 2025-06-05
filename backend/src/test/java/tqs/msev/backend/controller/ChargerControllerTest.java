package tqs.msev.backend.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tqs.msev.backend.configuration.TestSecurityConfig;
import tqs.msev.backend.service.ChargerService;
import tqs.msev.backend.entity.Charger;
import tqs.msev.backend.exception.GlobalExceptionHandler;
import java.util.UUID;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.containsString;
import tqs.msev.backend.entity.Reservation;
import tqs.msev.backend.service.JwtService;
import tqs.msev.backend.service.ReservationService;

@WebMvcTest(ChargerController.class)
@Import({GlobalExceptionHandler.class, TestSecurityConfig.class})
class ChargerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChargerService chargerService;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private JwtService jwtService;

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

    @Test
    @WithUserDetails("test")
    void whenUnlockOutOfOrderCharger_thenReturnBadRequest() throws Exception {
        doThrow(new IllegalStateException("The charger is out of order")).when(chargerService).unlockCharger(Mockito.any(), Mockito.any());

        mockMvc.perform(patch("/api/v1/chargers/{chargerId}/unlock", UUID.randomUUID()).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails("test")
    void whenUnlockCharger_thenReturnOk() throws Exception {
        mockMvc.perform(patch("/api/v1/chargers/{chargerId}/unlock", UUID.randomUUID()).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("test")
    void whenLockInvalidCharger_thenReturnBadRequest() throws Exception {
        doThrow(new NoSuchElementException("The charger does not exist")).when(chargerService).lockCharger(Mockito.any(), Mockito.any());

        mockMvc.perform(patch("/api/v1/chargers/{chargerId}/lock", UUID.randomUUID()).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("test")
    void whenLockValidCharger_thenReturnOk() throws Exception {
        mockMvc.perform(patch("/api/v1/chargers/{chargerId}/lock", UUID.randomUUID()).with(csrf()))
                .andExpect(status().isOk());
    }

    
    @Test
    @WithUserDetails("test_operator")
    @Requirement("MSEV-13")
    void whenCreateInvalidCharger_thenReturnBadRequest() throws Exception {
        Charger invalidCharger = new Charger();
        invalidCharger.setConnectorType("INVALID_TYPE");
        invalidCharger.setPrice(-1.0);
        invalidCharger.setChargingSpeed(-10);

        mockMvc.perform(
            post("/api/v1/chargers")
                .contentType("application/json")
                .content("{\"connectorType\":\"INVALID_TYPE\", \"price\":-1.0, \"chargingSpeed\":-10}")
                .with(csrf())
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails("test_operator")
    @Requirement("MSEV-25")
    void whenUpdateChargerPrice_thenReturnOk() throws Exception {
        UUID chargerId = UUID.randomUUID();

        Charger updatedCharger = new Charger();
        updatedCharger.setId(chargerId);
        updatedCharger.setPrice(0.7);
        
        when(chargerService.updateChargerPrice(chargerId, 0.7)).thenReturn(updatedCharger);
       
        
        mockMvc.perform(
            patch("/api/v1/chargers/{chargerId}/update", chargerId)
                .contentType("application/json")
                .content("0.7")
                .with(csrf()


        )        )        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(chargerId.toString()))
        .andExpect(jsonPath("$.price").value(0.7));
    }

    



}