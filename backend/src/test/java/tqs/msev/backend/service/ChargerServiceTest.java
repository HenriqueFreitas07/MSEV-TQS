package tqs.msev.backend.service;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tqs.msev.backend.entity.ChargeSession;
import tqs.msev.backend.entity.Reservation;
import tqs.msev.backend.entity.User;
import tqs.msev.backend.repository.*;
import tqs.msev.backend.entity.Charger;

import tqs.msev.backend.entity.Station;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class ChargerServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ChargeSessionRepository chargeSessionRepository;

    @Mock
    private ChargerRepository chargerRepository;

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private ChargerService chargerService;

    @Test
    @Requirement("MSEV-18")
    void whenChargerExists_thenReturnCharger() {
        UUID chargerId = UUID.randomUUID();
        Charger mockCharger = new Charger();
        Charger.ChargerStatus status = Charger.ChargerStatus.AVAILABLE;
        
        when(chargerRepository.findById(chargerId)).thenReturn(Optional.of(mockCharger));
        
        Charger charger = chargerService.getChargerById(chargerId);
        
        assertEquals(mockCharger, charger);
        assertEquals(status, charger.getStatus());
    }

    @Test
    @Requirement("MSEV-18")
    void whenChargerDoesNotExist_thenThrowException() {
        UUID chargerId = UUID.randomUUID();
        
        when(chargerRepository.findById(chargerId)).thenReturn(Optional.empty());
        
        try {
            chargerService.getChargerById(chargerId);
        } catch (NoSuchElementException e) {
            assertEquals("Charger not found", e.getMessage());
        }
    }

    @Test
    @Requirement("MSEV-18")
    void whenStationExists_thenReturnChargers() {
        UUID stationId = UUID.randomUUID();
        List<Charger> mockChargers = List.of(new Charger(), new Charger());
        
        when(chargerRepository.findByStationId(stationId)).thenReturn(mockChargers);
        
        List<Charger> chargers = chargerService.getChargersByStation(stationId);
        
        assertEquals(mockChargers, chargers);
    }

    @Test
    @Requirement("MSEV-18")
    void whenStationDoesNotExist_thenReturnEmptyList() {
        UUID stationId = UUID.randomUUID();
        
        when(chargerRepository.findByStationId(stationId)).thenReturn(List.of());
        
        List<Charger> chargers = chargerService.getChargersByStation(stationId);
        
        assertEquals(0, chargers.size());
    }

    @Test
    @Requirement("MSEV-20")
    void whenUnlockOutOfOrderCharger_thenThrowException() {
        Charger charger = Charger.builder()
                .status(Charger.ChargerStatus.OUT_OF_ORDER)
                .build();

        when(chargerRepository.findById(Mockito.any())).thenReturn(Optional.of(charger));

        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> chargerService.unlockCharger(id, id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("out of order");

        verify(chargerRepository, times(1)).findById(Mockito.any());
    }

    @Test
    @Requirement("MSEV-20")
    void whenUnlockDisabledCharger_thenThrowException() {
        Charger charger = Charger.builder()
                .status(Charger.ChargerStatus.TEMPORARILY_DISABLED)
                .build();

        when(chargerRepository.findById(Mockito.any())).thenReturn(Optional.of(charger));

        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> chargerService.unlockCharger(id, id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("disabled");

        verify(chargerRepository, times(1)).findById(Mockito.any());
    }

    @Test
    @Requirement("MSEV-20")
    void whenUnlockChargerInUseWithoutReservation_thenThrowException() {
        Charger charger = Charger.builder()
                .status(Charger.ChargerStatus.IN_USE)
                .build();

        when(chargerRepository.findById(Mockito.any())).thenReturn(Optional.of(charger));

        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> chargerService.unlockCharger(id, id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("in use");

        verify(chargerRepository, times(1)).findById(Mockito.any());
    }

    @Test
    @Requirement("MSEV-20")
    void whenUnlockChargerInUseWithReservation_thenUnlock() {
        Charger charger = Charger.builder()
                .id(UUID.randomUUID())
                .status(Charger.ChargerStatus.IN_USE)
                .build();

        User oldUser = User.builder()
                .id(UUID.randomUUID())
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        Reservation reservation = Reservation.builder()
                .charger(charger)
                .user(user)
                .startTimestamp(LocalDateTime.now())
                .build();

        ChargeSession oldSession = ChargeSession.builder()
                .user(oldUser)
                .charger(charger)
                .startTimestamp(LocalDateTime.of(2025, 5, 25, 20, 0))
                .build();

        when(chargerRepository.findById(charger.getId())).thenReturn(Optional.of(charger));
        when(reservationRepository
                .findByUserIdAndStartTimestampBeforeAndEndTimestampAfter(eq(user.getId()), Mockito.any(), Mockito.any()))
                .thenReturn(reservation);
        when(chargeSessionRepository.findByChargerIdAndEndTimestamp(charger.getId(), null)).thenReturn(oldSession);

        assertThatCode(() -> chargerService.unlockCharger(charger.getId(), user.getId())).doesNotThrowAnyException();

        verify(chargerRepository, times(1)).findById(charger.getId());
        verify(reservationRepository, times(1)).save(Mockito.any());
    }

    @Test
    @Requirement("MSEV-20")
    void whenUnlockChargerWithReservation_thenUnlock() {
        Charger charger = Charger.builder()
                .id(UUID.randomUUID())
                .status(Charger.ChargerStatus.AVAILABLE)
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        Reservation reservation = Reservation.builder()
                .charger(charger)
                .user(user)
                .startTimestamp(LocalDateTime.now())
                .build();

        when(chargerRepository.findById(charger.getId())).thenReturn(Optional.of(charger));
        when(reservationRepository
                .findByUserIdAndStartTimestampBeforeAndEndTimestampAfter(eq(user.getId()), Mockito.any(), Mockito.any()))
                .thenReturn(reservation);

        assertThatCode(() -> chargerService.unlockCharger(charger.getId(), user.getId())).doesNotThrowAnyException();

        verify(chargerRepository, times(1)).findById(charger.getId());
        verify(reservationRepository, times(1)).save(Mockito.any());
    }

    @Test
    @Requirement("MSEV-20")
    void whenUnlockAvailableCharger_thenUnlock() {
        Charger charger = Charger.builder()
                .id(UUID.randomUUID())
                .status(Charger.ChargerStatus.AVAILABLE)
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        when(chargerRepository.findById(charger.getId())).thenReturn(Optional.of(charger));
        assertThatCode(() -> chargerService.unlockCharger(charger.getId(), user.getId())).doesNotThrowAnyException();
    }

    @Test
    @Requirement("MSEV-20")
    void whenLockLockedCharger_thenThrowException() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> chargerService.lockCharger(id, id))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @Requirement("MSEV-20")
    void whenLockChargerUsedByAnotherUser_thenThrowException() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        ChargeSession session = ChargeSession.builder()
                .user(user)
                .build();

        when(chargeSessionRepository.findByChargerIdAndEndTimestamp(Mockito.any(), Mockito.any())).thenReturn(session);

        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> chargerService.lockCharger(id, id))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @Requirement("MSEV-20")
    void whenLockCharger_thenLockCharger() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        Charger charger = Charger.builder()
                .id(UUID.randomUUID())
                .status(Charger.ChargerStatus.IN_USE)
                .build();

        ChargeSession session = ChargeSession.builder()
                .user(user)
                .charger(charger)
                .build();

        when(chargeSessionRepository.findByChargerIdAndEndTimestamp(Mockito.any(), Mockito.any())).thenReturn(session);

        assertThatCode(() -> chargerService.lockCharger(charger.getId(), user.getId())).doesNotThrowAnyException();

        verify(chargeSessionRepository, times(1)).save(Mockito.any());
        verify(chargerRepository, times(1)).save(Mockito.any());

    }

    @Test
    @Requirement("MSEV-13")
    void whenCreateValidCharger_thenReturnCharger() {
        Station station = Station.builder()
                .id(UUID.randomUUID())
                .build();
        Charger charger = Charger.builder()
                .id(UUID.randomUUID())
                .station(station)
                .status(Charger.ChargerStatus.AVAILABLE)
                .build();
        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(chargerRepository.save(Mockito.any(Charger.class))).thenReturn(charger);

        Charger createdCharger = chargerService.createCharger(charger);

        assertEquals(charger, createdCharger);
        verify(chargerRepository, times(1)).save(charger);
    }

    @Test
    @Requirement("MSEV-13")
    void whenCreateChargerWithNullStatus_thenSetDefaultStatus() {
        Station station = Station.builder()
                .id(UUID.randomUUID())
                .build();
        Charger charger = Charger.builder()
                .id(UUID.randomUUID())
                .station(station)
                .status(null)
                .build();
        Charger expectedCharger = Charger.builder()
                .id(charger.getId())
                .status(Charger.ChargerStatus.AVAILABLE)
                .build();
        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(chargerRepository.save(Mockito.any(Charger.class))).thenReturn(expectedCharger);
        Charger createdCharger = chargerService.createCharger(charger);
        assertEquals(expectedCharger, createdCharger);
        verify(chargerRepository, times(1)).save(charger);
    }

    @Test
    @Requirement("MSEV-13")
    void whenCreateChargerWithNoStation_thenThrowException() {
        Charger charger = Charger.builder()
                .id(UUID.randomUUID())
                .station(null)
                .build();

        assertThatThrownBy(() -> chargerService.createCharger(charger))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Charger must be associated with a valid station");
        
        verify(chargerRepository, never()).save(Mockito.any(Charger.class));
    }

    @Test
    @Requirement("MSEV-20")
    void whenGetAllChargeSessions_thenReturnChargeSessions() {
        UUID id = UUID.randomUUID();

        ChargeSession session1 = ChargeSession.builder()
                .startTimestamp(LocalDateTime.now())
                .build();

        ChargeSession session2 = ChargeSession.builder()
                .startTimestamp(LocalDateTime.now())
                .endTimestamp((LocalDateTime.now().plusSeconds(30)))
                .build();

        when(chargeSessionRepository.findAllByUserId(id)).thenReturn(List.of(session1, session2));

        List<ChargeSession> sessions = chargerService.getChargeSessions(id, false);

        assertThat(sessions).hasSize(2);

        verify(chargeSessionRepository, times(1)).findAllByUserId(id);
    }

    @Test
    @Requirement("MSEV-20")
    void whenGetActiveChargeSessions_thenReturnActiveChargeSessions() {
        UUID id = UUID.randomUUID();

        ChargeSession session1 = ChargeSession.builder()
                .startTimestamp(LocalDateTime.now())
                .build();

        ChargeSession session2 = ChargeSession.builder()
                .startTimestamp(LocalDateTime.now())
                .endTimestamp((LocalDateTime.now().plusSeconds(30)))
                .build();

        when(chargeSessionRepository.findAllByUserId(id)).thenReturn(List.of(session1, session2));

        List<ChargeSession> sessions = chargerService.getChargeSessions(id, true);

        assertThat(sessions).hasSize(1);

        verify(chargeSessionRepository, times(1)).findAllByUserId(id);
    }

    @Test
    @Requirement("MSEV-25")
    void whenUpdateChargerPrice_thenReturnUpdatedCharger() {
        UUID chargerId = UUID.randomUUID();
        double newPrice = 0.7;

        Charger mockCharger = new Charger();
        mockCharger.setId(chargerId);
        mockCharger.setPrice(newPrice);

        when(chargerRepository.findById(chargerId)).thenReturn(Optional.of(mockCharger));
        when(chargerRepository.save(Mockito.any(Charger.class))).thenReturn(mockCharger);

        Charger updatedCharger = chargerService.updateChargerPrice(chargerId, newPrice);

        assertEquals(mockCharger, updatedCharger);
        assertEquals(newPrice, updatedCharger.getPrice());
    }
    
    @Test
    @Requirement("MSEV-25")
    void whenUpdateChargerWithInvalidPrice_thenThrowException() {
        UUID chargerId = UUID.randomUUID();
        double invalidPrice = -1.0;

        Charger mockCharger = new Charger();
        mockCharger.setId(chargerId);
        
        when(chargerRepository.findById(chargerId)).thenReturn(Optional.of(mockCharger));

        assertThatThrownBy(() -> chargerService.updateChargerPrice(chargerId, invalidPrice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Price must be non-negative");

        verify(chargerRepository, never()).save(Mockito.any(Charger.class));
    }

    @Test
    @Requirement("MSEV-25")
    void whenUpdateChargerThatDoesNotExist_thenThrowException() {
        UUID chargerId = UUID.randomUUID();
        double newPrice = 0.7;
        when(chargerRepository.findById(chargerId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> chargerService.updateChargerPrice(chargerId, newPrice))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Charger not found");
        verify(chargerRepository, never()).save(Mockito.any(Charger.class));
    }

    @Test
    @Requirement("MSEV-25")
    void whenGetChargeSessionsByCharger_thenReturnChargeSessions() {
        UUID chargerId = UUID.randomUUID();
        ChargeSession session1 = ChargeSession.builder()
                .id(UUID.randomUUID())
                .charger(Charger.builder().id(chargerId).build())
                .startTimestamp(LocalDateTime.now())
                .endTimestamp(LocalDateTime.now().plusMinutes(30))      
                .build();
        ChargeSession session2 = ChargeSession.builder()
                .id(UUID.randomUUID())               
                .charger(Charger.builder().id(chargerId).build())
                .startTimestamp(LocalDateTime.now().minusMinutes(60))
                .endTimestamp(null)
                .build();
        when(chargeSessionRepository.findAllByChargerId(chargerId)).thenReturn(List.of(session1, session2));
        List<ChargeSession> sessions = chargerService.getChargeSessionsByCharger(chargerId);
        assertThat(sessions).hasSize(1)
                .contains(session1);
        verify(chargeSessionRepository, times(1)).findAllByChargerId(chargerId);
    }

    @Test
    @Requirement("MSEV-25")
    void whenGetStationStats_thenReturnStats() {
        UUID stationId = UUID.randomUUID();
        Charger charger1 = Charger.builder().id(UUID.randomUUID()).station(Station.builder().id(stationId).build()).build();
        Charger charger2 = Charger.builder().id(UUID.randomUUID()).station(Station.builder().id(stationId).build()).build();
        
        ChargeSession session1 = ChargeSession.builder().charger(charger1).startTimestamp(LocalDateTime.now().minusHours(1)).endTimestamp(LocalDateTime.now()).build();
        ChargeSession session2 = ChargeSession.builder().charger(charger2).startTimestamp(LocalDateTime.now().minusHours(2)).endTimestamp(LocalDateTime.now().minusHours(1)).build();
        
        when(chargerRepository.findByStationId(stationId)).thenReturn(List.of(charger1, charger2));
        when(chargeSessionRepository.findAllByChargerId(charger1.getId())).thenReturn(List.of(session1));
        when(chargeSessionRepository.findAllByChargerId(charger2.getId())).thenReturn(List.of(session2));
        
        List<ChargeSession> stats = chargerService.getStationStats(stationId);
        
        assertThat(stats).hasSize(2)
                .contains(session1, session2);
    }

    @Test
    @Requirement("MSEV-25")
    void whenGetStationStatsWithNoChargers_thenThrowException() {
        UUID stationId = UUID.randomUUID();
        
        when(chargerRepository.findByStationId(stationId)).thenReturn(List.of());
        
        assertThatThrownBy(() -> chargerService.getStationStats(stationId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("No chargers found for this station");
        
        verify(chargeSessionRepository, never()).findAllByChargerId(Mockito.any());
    }
}
