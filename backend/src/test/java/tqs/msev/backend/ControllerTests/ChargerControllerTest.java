package tqs.msev.backend.ControllerTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.List;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import tqs.msev.backend.controller.ChargerController;
import tqs.msev.backend.service.ChargerService;
import tqs.msev.backend.entity.Charger;
import java.util.UUID;


public class ChargerControllerTest {
    
    @Mock
    ChargerService chargerService;

    ChargerController chargerController = new ChargerController(chargerService);

    @BeforeEach
    public void setUp() {
        chargerService = mock(ChargerService.class);
        chargerController = new ChargerController(chargerService);
    }

    @Test
    public void whenStationExists_thenReturnChargers() {
        UUID stationId = UUID.randomUUID();
        List<Charger> mockChargers = List.of(new Charger(), new Charger());
        
        when(chargerService.getChargersByStation(stationId)).thenReturn(mockChargers);
        
        List<Charger> chargers = chargerController.getChargersByStation(stationId);
        
        assertEquals(mockChargers, chargers);
    }

    @Test
    public void whenChargerExists_thenReturnCharger() {
        UUID chargerId = UUID.randomUUID();
        Charger mockCharger = new Charger();
        
        when(chargerService.getChargerById(chargerId)).thenReturn(mockCharger);
        
        Charger charger = chargerController.getChargerById(chargerId);
        
        assertEquals(mockCharger, charger);
    }

    @Test
    public void whenChargerDoesNotExist_thenThrowException() {
        UUID chargerId = UUID.randomUUID();
        
        when(chargerService.getChargerById(chargerId)).thenThrow(new RuntimeException("Charger not found"));
        
        try {
            chargerController.getChargerById(chargerId);
        } catch (RuntimeException e) {
            assertEquals("Charger not found", e.getMessage());
        }
    }

    @Test
    public void whenStationDoesNotExist_thenReturnEmptyList() {
        UUID stationId = UUID.randomUUID();
        List<Charger> mockChargers = List.of();
        
        when(chargerService.getChargersByStation(stationId)).thenReturn(mockChargers);
        
        List<Charger> chargers = chargerController.getChargersByStation(stationId);
        
        assertEquals(mockChargers, chargers);
    }

  

}