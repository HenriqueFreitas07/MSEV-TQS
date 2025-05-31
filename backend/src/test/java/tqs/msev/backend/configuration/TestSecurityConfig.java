package tqs.msev.backend.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import tqs.msev.backend.entity.User;

import java.util.UUID;

@TestConfiguration
public class TestSecurityConfig {
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            if (username.equals("test")) {
                return User.builder().id(UUID.randomUUID()).name("Test User").email("test@gmail.com").isOperator(false).build();
            }

            if (username.equals("test_operator")) {
                return User.builder().id(UUID.randomUUID()).name("Test Operator").email("test_operator@gmail.com").isOperator(true).build();
            }

            throw new UsernameNotFoundException("User not found");
        };
    }
}
