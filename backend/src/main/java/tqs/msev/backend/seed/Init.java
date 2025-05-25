package tqs.msev.backend.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tqs.msev.backend.entity.Charger;
import tqs.msev.backend.entity.Station;
import tqs.msev.backend.repository.ChargerRepository;
import tqs.msev.backend.repository.StationRepository;

import static tqs.msev.backend.entity.Station.StationStatus;

@Component
public class Init implements CommandLineRunner {
    private final StationRepository stationRepository;
    private final ChargerRepository chargerRepository;

    public Init(StationRepository stationRepository, ChargerRepository chargerRepository) {
        this.stationRepository = stationRepository;
        this.chargerRepository = chargerRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!stationRepository.findAll().isEmpty()) {
            chargerRepository.deleteAll();
            stationRepository.deleteAll();
        }
        Station station = Station.builder()
                //.id(UUID.fromString("e33ba518-2fd1-43d1-8b02-25b2758aa592"))
                .name("GetCharged")
                .address("Rua da Universidade de Aveiro")
                .latitude(40.631231)
                .longitude(-8.656277)
                .status(StationStatus.ENABLED)
                .build();

        stationRepository.saveAndFlush(station);

        Charger chargerA = Charger.builder()
                .station(station)
                .connectorType("Type 2")
                .price(23)
                .chargingSpeed(32)
                .status(Charger.ChargerStatus.AVAILABLE)
                .build();

        Charger chargerB = Charger.builder()
                .station(station)
                .connectorType("Type 2")
                .price(42)
                .chargingSpeed(72)
                .status(Charger.ChargerStatus.IN_USE)
                .build();

        chargerRepository.saveAndFlush(chargerA);
        chargerRepository.saveAndFlush(chargerB);
    }
}
