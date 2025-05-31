package tqs.msev.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import tqs.msev.backend.configuration.TestSecurityConfig;
import tqs.msev.backend.entity.Station;
import tqs.msev.backend.exception.GlobalExceptionHandler;
import tqs.msev.backend.service.JwtService;
import tqs.msev.backend.service.StationService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StationController.class)
@Import({GlobalExceptionHandler.class, TestSecurityConfig.class})
class StationControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private StationService service;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "test")
    void givenManyStations_whenGetStations_thenReturnStations() throws Exception {
        Station station1 = new Station();
        station1.setName("Station 1");

        Station station2 = new Station();
        station2.setName("Station 2");

        when(service.getAllStations()).thenReturn(List.of(station1, station2));

        mvc.perform(get("/api/v1/stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(station1.getName())))
                .andExpect(jsonPath("$[1].name", is(station2.getName())));
    }

    @Test
    @WithMockUser(username = "test")
    void givenOneStation_whenGetStation_thenReturnStation() throws Exception {
        UUID id = UUID.randomUUID();
        Station station1 = new Station();
        station1.setId(id);
        station1.setName("Station 1");

        when(service.getStationById(id)).thenReturn(station1);

        mvc.perform(get("/api/v1/stations/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(station1.getName())));
    }

    @Test
    @WithMockUser(username = "test")
    void whenGetInvalidStation_thenReturnNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        when(service.getStationById(id)).thenThrow(new NoSuchElementException("Invalid station id"));

        mvc.perform(get("/api/v1/stations/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test")
    void givenManyStations_whenSearchStationsByName_thenReturnStations() throws Exception {
        Station station1 = new Station();
        station1.setName("Station 1");

        when(service.searchByName("Station 1")).thenReturn(List.of(station1));

        mvc.perform(get("/api/v1/stations/search-by-name").param("name", "Station 1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(station1.getName())));
    }

    @Test
    @WithMockUser(username = "test")
    void givenManyStations_whenSearchStationsByAddress_thenReturnStations() throws Exception {
        Station station1 = new Station();
        station1.setName("Station 1");
        station1.setAddress("NY Street 1");

        when(service.searchByAddress("NY S")).thenReturn(List.of(station1));

        mvc.perform(get("/api/v1/stations/search-by-address").param("address", "NY S"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(station1.getName())));

    }

    @Test
    @WithUserDetails("test_operator")
    @Requirement("MSEV-13")
    void givenValidStation_whenCreateStation_thenReturnCreatedStation() throws Exception {
        Station station = new Station();
        station.setName("New Station");
        station.setAddress("New Address");
        station.setLatitude(40.7128);
        station.setLongitude(-74.0060);

        when(service.createStation(station)).thenReturn(station);

        mvc.perform(post("/api/v1/stations")
                .contentType("application/json")
                .with(csrf())
                .content("{\"name\":\"New Station\", \"address\":\"New Address\", \"latitude\":40.7128, \"longitude\":-74.0060}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(station.getName())))
                .andExpect(jsonPath("$.address", is(station.getAddress())))
                .andExpect(jsonPath("$.status", is("ENABLED")));
    }

    @Test
    @WithUserDetails("test_operator")
    @Requirement("MSEV-13")
    void givenInvalidStation_whenCreateStation_thenReturnBadRequest() throws Exception {
        Station station = new Station();
        station.setName("");

        when(service.createStation(station)).thenThrow(new IllegalArgumentException("Station name cannot be null or empty"));

        mvc.perform(post("/api/v1/stations")
                .contentType("application/json")
                .with(csrf())
                .content("{\"name\":\"\", \"address\":\"New Address\", \"latitude\":200.7128, \"longitude\":-74.0060}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenCreateStationWithoutAuthentication_thenReturnUnauthorized() throws Exception {
        Station station = new Station();
        station.setName("Unauthorized Station");
        station.setAddress("Unauthorized Address");
        station.setLatitude(40.7128);
        station.setLongitude(-74.0060);

        mvc.perform(post("/api/v1/stations")
                .contentType("application/json")
                .with(csrf())
                .content("{\"name\":\"Unauthorized Station\", \"address\":\"Unauthorized Address\", \"latitude\":40.7128, \"longitude\":-74.0060}"))
                .andExpect(status().isUnauthorized());
    }
}
