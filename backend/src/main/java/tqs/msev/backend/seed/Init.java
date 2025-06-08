package tqs.msev.backend.seed;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import tqs.msev.backend.entity.Charger;
import tqs.msev.backend.entity.Station;
import tqs.msev.backend.entity.User;
import tqs.msev.backend.repository.ChargerRepository;
import tqs.msev.backend.repository.StationRepository;
import tqs.msev.backend.repository.UserRepository;

import static tqs.msev.backend.entity.Station.StationStatus;

@Component
public class Init implements CommandLineRunner {
    private final StationRepository stationRepository;
    private final ChargerRepository chargerRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${operator.password}")
    private String operatorPassword;

    public Init(StationRepository stationRepository, ChargerRepository chargerRepository, UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.stationRepository = stationRepository;
        this.chargerRepository = chargerRepository;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findUserByEmail("operator@gmail.com").isEmpty()) {
            User user = User.builder()
                    .name("Mr. Operator")
                    .email("operator@gmail.com")
                    .password(bCryptPasswordEncoder.encode(operatorPassword))
                    .isOperator(true)
                    .build();

            userRepository.save(user);
        }

        if (userRepository.findUserByEmail("test_user@gmail.com").isEmpty()) {
            User user = User.builder()
                    .name("Test User")
                    .email("test_user@gmail.com")
                    .password(bCryptPasswordEncoder.encode(operatorPassword))
                    .isOperator(false)
                    .build();

            userRepository.save(user);
        }

        if (!stationRepository.findAll().isEmpty())
            return;

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
                .status(Charger.ChargerStatus.AVAILABLE)
                .build();

        chargerRepository.saveAndFlush(chargerA);
        chargerRepository.saveAndFlush(chargerB);
    }
}
