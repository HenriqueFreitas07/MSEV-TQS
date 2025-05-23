package tqs.msev.backend.service;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tqs.msev.backend.repository.ReservationRepository;
import tqs.msev.backend.entity.Reservation;
import app.getxray.xray.junit.customjunitxml.annotations.Requirement;

import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
        when(reservationRepository.findByChargerId(any())).thenReturn(List.of());
        when(reservationRepository.save(mockReservation)).thenReturn(mockReservation);
        Reservation reservation = reservationService.createReservation(mockReservation);
        
        assertEquals(mockReservation, reservation);
    }


   
}
