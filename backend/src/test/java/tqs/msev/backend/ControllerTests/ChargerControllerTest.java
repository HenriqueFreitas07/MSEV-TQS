package tqs.msev.backend.ControllerTests;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import tqs.msev.backend.controller.ChargerController;
import tqs.msev.backend.service.ChargerService;
import tqs.msev.backend.entity.Charger;
import tqs.msev.backend.exception.GlobalExceptionHandler;
import java.util.UUID;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.containsString;


@WebMvcTest(ChargerController.class)
@Import(GlobalExceptionHandler.class) 
public class ChargerControllerTest {
    
    @Autowired
    MockMvc mockMvc;

    @MockBean
    ChargerService chargerService;


    @Autowired
    ChargerController chargerController;

    @Test
    public void whenStationExists_thenReturnChargers() throws Exception {

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
    public void whenChargerExists_thenReturnCharger()  throws Exception {
        UUID chargerId = UUID.randomUUID();
        Charger mockCharger = new Charger();
        
        when(chargerService.getChargerById(chargerId)).thenReturn(mockCharger);
        
        mockMvc.perform(
            get("/api/v1/chargers/{chargerId}", chargerId)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isNotEmpty())
        .andExpect(jsonPath("$").value(mockCharger));        
    }

    @Test
    public void whenChargerDoesNotExist_thenThrowException() throws Exception {
        UUID chargerId = UUID.randomUUID();
        
        when(chargerService.getChargerById(chargerId)).thenThrow(new NoSuchElementException("Charger not found"));
        
        mockMvc.perform(
            get("/api/v1/chargers/{chargerId}", chargerId)
        )
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("Charger not found")));
    }

}