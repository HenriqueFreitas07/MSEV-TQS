package tqs.msev.backend.ServiceTests;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tqs.msev.backend.repository.ChargerRepository;
import tqs.msev.backend.service.ChargerService;
import tqs.msev.backend.entity.Charger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import java.util.UUID;
import java.util.Optional;
import java.util.List;  

@ExtendWith(MockitoExtension.class)
public class ChargerServiceTest {

    @Mock
    ChargerRepository chargerRepository;

    @InjectMocks
    private ChargerService chargerService;


    @Test
    public void whenChargerExists_thenReturnCharger() {
        UUID chargerId = UUID.randomUUID();
        Charger mockCharger = new Charger();
        
        when(chargerRepository.findById(chargerId)).thenReturn(Optional.of(mockCharger));
        
        Charger charger = chargerService.getChargerById(chargerId);
        
        assertEquals(mockCharger, charger);
    }

    @Test
    public void whenChargerDoesNotExist_thenThrowException() {
        UUID chargerId = UUID.randomUUID();
        
        when(chargerRepository.findById(chargerId)).thenReturn(Optional.empty());
        
        try {
            chargerService.getChargerById(chargerId);
        } catch (RuntimeException e) {
            assertEquals("Charger not found", e.getMessage());
        }
    }

    @Test
    public void whenStationExists_thenReturnChargers() {
        UUID stationId = UUID.randomUUID();
        List<Charger> mockChargers = List.of(new Charger(), new Charger());
        
        when(chargerRepository.findByStationId(stationId)).thenReturn(mockChargers);
        
        List<Charger> chargers = chargerService.getChargersByStation(stationId);
        
        assertEquals(mockChargers, chargers);
    }

    @Test
    public void whenStationDoesNotExist_thenReturnEmptyList() {
        UUID stationId = UUID.randomUUID();
        
        when(chargerRepository.findByStationId(stationId)).thenReturn(List.of());
        
        List<Charger> chargers = chargerService.getChargersByStation(stationId);
        
        assertEquals(0, chargers.size());
    }
    
}
