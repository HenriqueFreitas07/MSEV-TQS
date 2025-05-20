package tqs.msev.backend.controller;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import app.getxray.xray.junit.customjunitxml.annotations.XrayTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.msev.backend.entity.Station;
import tqs.msev.backend.service.StationService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StationController.class)
class StationControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private StationService service;

    @Test
    @XrayTest(key = "MSEV-56")
    @Requirement("MSEV-16")
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
    @XrayTest(key = "MSEV-57")
    @Requirement("MSEV-16")
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
    @Requirement("MSEV-16")
    void whenGetInvalidStation_thenReturnNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        when(service.getStationById(id)).thenThrow(new NoSuchElementException("Invalid station id"));

        mvc.perform(get("/api/v1/stations/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @Requirement("MSEV-16")
    void givenManyStations_whenSearchStationsByName_thenReturnStations() throws Exception {
        Station station1 = new Station();
        station1.setName("Station 1");

        when(service.searchByName("Station 1")).thenReturn(List.of(station1));

        mvc.perform(get("/api/v1/stations/search-by-name").param("name", "Station 1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(station1.getName())));
    }

    @Test
    @Requirement("MSEV-16")
    void givenManyStations_whenSearchStationsByAddress_thenReturnStations() throws Exception {
        Station station1 = new Station();
        station1.setName("Station 1");
        station1.setAddress("NY Street 1");

        when(service.searchByAddress("NY S")).thenReturn(List.of(station1));

        mvc.perform(get("/api/v1/stations/search-by-address").param("address", "NY S"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(station1.getName())));

    }
}
