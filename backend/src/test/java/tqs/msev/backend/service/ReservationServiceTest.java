package tqs.msev.backend.service;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tqs.msev.backend.repository.ReservationRepository;
import tqs.msev.backend.entity.Reservation;
import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import tqs.msev.backend.entity.Charger;
import java.util.UUID;
import java.util.Date;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    ReservationRepository reservationRepository;

    @InjectMocks
    private  ReservationService reservationService;

    @Test
    @Requirement("MSEV-17")
    void whenReservationsExist_thenReturnReservations() {
        UUID chargerId = UUID.randomUUID();
        Reservation mockReservation = new Reservation();
        mockReservation.setStartTimestamp(new Date(System.currentTimeMillis() + 1000 * 60 * 60)); 
        Reservation mockReservation2 = new Reservation();
        mockReservation2.setStartTimestamp(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24));
        List<Reservation> mockReservations = List.of(mockReservation, mockReservation2);
        
        when(reservationRepository.findByChargerId(chargerId)).thenReturn(mockReservations);
        
        List<Reservation> reservations = reservationService.getFutureReservationsOnCharger(chargerId);
        
        assertEquals(mockReservations, reservations);
    }

    @Test
    @Requirement("MSEV-17")
    void whenReservationsNotInCloseFuture_thenReturnEmptyList() {
        UUID chargerId = UUID.randomUUID();
        
        Reservation mockReservation = new Reservation();
        mockReservation.setStartTimestamp(new Date(System.currentTimeMillis() - 1000 * 60 * 60)); 
        Reservation mockReservation2 = new Reservation();
        mockReservation2.setStartTimestamp(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 10));
        List<Reservation> mockReservations = List.of(mockReservation, mockReservation2);
        
        when(reservationRepository.findByChargerId(chargerId)).thenReturn(mockReservations);
        
        List<Reservation> reservations = reservationService.getFutureReservationsOnCharger(chargerId);
        
        assertEquals(0, reservations.size());
    }

    @Test
    @Requirement("MSEV-19")
    void whenReservationCorrect_thenReturnReservation() {
        Reservation mockReservation = new Reservation();
        mockReservation.setStartTimestamp(new Date(System.currentTimeMillis() + 1000 * 60 * 60)); 
        mockReservation.setEndTimestamp(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2)); 
        Charger mockCharger = new Charger();
        mockCharger.setId(UUID.randomUUID());
        mockReservation.setCharger(mockCharger);
        when(reservationRepository.findByChargerId(any())).thenReturn(List.of());
        when(reservationRepository.save(mockReservation)).thenReturn(mockReservation);
        Reservation reservation = reservationService.createReservation(mockReservation);
        
        assertEquals(mockReservation, reservation);
    }

    @Test
    @Requirement("MSEV-19")
    void whenReservationInThePast_thenThrowException() {
        Reservation mockReservation = new Reservation();
        mockReservation.setStartTimestamp(new Date(System.currentTimeMillis() - 1000 * 60 * 60)); 
        mockReservation.setEndTimestamp(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 2)); 
        
        try {
            reservationService.createReservation(mockReservation);
        } catch (IllegalArgumentException e) {
            assertEquals("Reservation cannot be in the past", e.getMessage());
        }
    }

    @Test
    @Requirement("MSEV-19")
    void whenReservationStartAfterEnd_thenThrowException() {
        Reservation mockReservation = new Reservation();
        mockReservation.setStartTimestamp(new Date(System.currentTimeMillis() + 1000 * 60 * 60)); 
        mockReservation.setEndTimestamp(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2)); 
        Charger mockCharger = new Charger();
        mockCharger.setId(UUID.randomUUID());
        mockReservation.setCharger(mockCharger);
        when(reservationRepository.findByChargerId(any())).thenReturn(List.of());
        
        try {
            reservationService.createReservation(mockReservation);
        } catch (IllegalArgumentException e) {
            assertEquals("Start timestamp must be before end timestamp", e.getMessage());
        }
    }

    @Test
    @Requirement("MSEV-19")
    void whenReservationOverlaps_thenThrowException() {
        Reservation mockReservation = new Reservation();
        mockReservation.setStartTimestamp(new Date(System.currentTimeMillis() + 1000 * 60 * 60)); 
        mockReservation.setEndTimestamp(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2)); 
        Charger mockCharger = new Charger();
        mockCharger.setId(UUID.randomUUID());
        mockReservation.setCharger(mockCharger);
        Reservation existingReservation = new Reservation();
        existingReservation.setStartTimestamp(new Date(System.currentTimeMillis() + 1000 * 60 * 30)); 
        existingReservation.setEndTimestamp(new Date(System.currentTimeMillis() + 1000 * 60 * 90)); 

        when(reservationRepository.findByChargerId(any())).thenReturn(List.of(existingReservation));
        
        try {
            reservationService.createReservation(mockReservation);
        } catch (IllegalArgumentException e) {
            assertEquals("Reservation overlaps with an existing reservation", e.getMessage());
        }
    }

    @Test
    @Requirement("MSEV-19")
    void whenReservationExists_thenCancelIt() {
        UUID reservationId = UUID.randomUUID();
        Reservation mockReservation = new Reservation();
        mockReservation.setId(reservationId);
        
        when(reservationRepository.findById(reservationId)).thenReturn(java.util.Optional.of(mockReservation));
        
        Reservation canceledReservation = reservationService.cancelReservation(reservationId);
        
        assertEquals(mockReservation, canceledReservation);
    }

    @Test
    @Requirement("MSEV-19")
    void whenReservationUsedOnTime_thenMarkAsUsed() {
        UUID reservationId = UUID.randomUUID();
        Reservation mockReservation = new Reservation();
        mockReservation.setId(reservationId);
        mockReservation.setStartTimestamp(new Date(System.currentTimeMillis() - 1000 * 60 * 60)); 
        mockReservation.setEndTimestamp(new Date(System.currentTimeMillis() + 1000 * 60 * 60)); 
        
        when(reservationRepository.findById(reservationId)).thenReturn(java.util.Optional.of(mockReservation));
        
        Reservation usedReservation = reservationService.markReservationAsUsed(reservationId);
        
        assertEquals(true, usedReservation.isUsed());
    }

    @Test
    @Requirement("MSEV-19")
    void whenReservationUsedAlready_thenThrowException() {
        UUID reservationId = UUID.randomUUID();
        Reservation mockReservation = new Reservation();
        mockReservation.setId(reservationId);
        mockReservation.setStartTimestamp(new Date(System.currentTimeMillis() - 1000 * 60 * 60)); 
        mockReservation.setEndTimestamp(new Date(System.currentTimeMillis() + 1000 * 60 * 60)); 
        mockReservation.setUsed(true);
        
        when(reservationRepository.findById(reservationId)).thenReturn(java.util.Optional.of(mockReservation));
        
        try {
            reservationService.markReservationAsUsed(reservationId);
        } catch (IllegalArgumentException e) {
            assertEquals("Reservation already marked as used", e.getMessage());
        }
    }

    @Test
    @Requirement("MSEV-19")
    void whenReservationNotStartedYet_thenThrowException() {
        UUID reservationId = UUID.randomUUID();
        Reservation mockReservation = new Reservation();
        mockReservation.setId(reservationId);
        mockReservation.setStartTimestamp(new Date(System.currentTimeMillis() + 1000 * 60 * 60)); 
        mockReservation.setEndTimestamp(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2)); 
        
        when(reservationRepository.findById(reservationId)).thenReturn(java.util.Optional.of(mockReservation));
        
        try {
            reservationService.markReservationAsUsed(reservationId);
        } catch (IllegalArgumentException e) {
            assertEquals("Reservation not started yet", e.getMessage());
        }
    }

    @Test
    @Requirement("MSEV-19")
    void whenReservationAlreadyEnded_thenThrowException() {
        UUID reservationId = UUID.randomUUID();
        Reservation mockReservation = new Reservation();
        mockReservation.setId(reservationId);
        mockReservation.setStartTimestamp(new Date(System.currentTimeMillis() - 1000 * 60 * 60)); 
        mockReservation.setEndTimestamp(new Date(System.currentTimeMillis() - 1000 * 60 * 30)); 
        
        when(reservationRepository.findById(reservationId)).thenReturn(java.util.Optional.of(mockReservation));
        
        try {
            reservationService.markReservationAsUsed(reservationId);
        } catch (IllegalArgumentException e) {
            assertEquals("Reservation already ended", e.getMessage());
        }
    }
    
   
}
