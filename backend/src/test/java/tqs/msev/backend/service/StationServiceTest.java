package tqs.msev.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tqs.msev.backend.dto.Coordinates;
import tqs.msev.backend.entity.Station;
import tqs.msev.backend.repository.StationRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StationServiceTest {
    @Mock
    private GeocodingService geocodingService;

    @Mock
    private StationRepository repository;

    @InjectMocks
    private StationService service;

    @BeforeEach
    void setup() {
        UUID id1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Station station1 = new Station();
        station1.setId(id1);
        station1.setLatitude(40);
        station1.setLongitude(-0.5);
        station1.setName("Station 1");
        station1.setAddress("NY Street, 1");

        Station station2 = new Station();
        station2.setLatitude(40);
        station2.setLongitude(-8.0);
        station2.setName("Station 2");
        station2.setAddress("Idk Street, 2");

        when(repository.findAll()).thenReturn(List.of(station1, station2));
        when(repository.findById(id1)).thenReturn(Optional.of(station1));
    }

    @Test
    void whenGetAllStations_thenReturnAllStations() {
        List<Station> stations = service.getAllStations();

        assertThat(stations).hasSize(2);
        assertThat(stations).extracting(Station::getName).containsAll(List.of("Station 1", "Station 2"));
        verify(repository, times(1)).findAll();
    }

    @Test
    void whenGetExistingStation_thenReturnStation() {
        UUID id1 = UUID.fromString("11111111-1111-1111-1111-111111111111");

        Station station = service.getStationById(id1);

        assertThat(station.getId()).isEqualTo(id1);
        verify(repository, times(1)).findById(id1);
    }

    @Test
    void whenGetInvalidStation_thenThrowException() {
        UUID invalidId = UUID.fromString("11111111-1111-1111-1111-111111111112");

        assertThatThrownBy(() -> service.getStationById(invalidId)).isInstanceOf(NoSuchElementException.class);
        verify(repository, times(1)).findById(invalidId);
    }

    @Test
    void whenSearchStationByName_thenReturnStations() {
        String name = "stati";

        List<Station> stations = service.searchByName(name);

        assertThat(stations).hasSize(2);
        assertThat(stations).extracting(Station::getName).containsAll(List.of("Station 1", "Station 2"));

        stations = service.searchByName("Station 1");

        assertThat(stations).hasSize(1);
        assertThat(stations).extracting(Station::getName).containsAll(List.of("Station 1"));
        verify(repository, times(2)).findAll();
    }

    @Test
    void whenSearchStationByAddress_thenReturnStations() {
        when(geocodingService.getCoordinatesForAddress(Mockito.anyString())).thenReturn(new Coordinates(40.6254255, -8.6514061));
        List<Station> stations = service.searchByAddress("Avenida da Universidade Aveiro");

        assertThat(stations).hasSize(2);
        assertThat(stations.get(0)).extracting(Station::getName).isEqualTo("Station 2");
        assertThat(stations.get(1)).extracting(Station::getName).isEqualTo("Station 1");

        verify(repository, times(1)).findAll();
        verify(geocodingService, times(1)).getCoordinatesForAddress(Mockito.anyString());
    }
}
