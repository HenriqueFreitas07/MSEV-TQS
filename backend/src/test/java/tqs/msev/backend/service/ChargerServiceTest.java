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
import tqs.msev.backend.repository.ChargeSessionRepository;
import tqs.msev.backend.repository.ChargerRepository;
import tqs.msev.backend.entity.Charger;
import tqs.msev.backend.repository.ReservationRepository;
import tqs.msev.backend.repository.UserRepository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class ChargerServiceTest {
    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChargeSessionRepository chargeSessionRepository;

    @Mock
    private ChargerRepository chargerRepository;

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
                .startTimestamp(Date.from(Instant.now()))
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
                .startTimestamp(Date.from(Instant.now()))
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
}
