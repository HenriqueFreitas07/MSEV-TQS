package tqs.msev.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import tqs.msev.backend.dto.SignupDTO;
import tqs.msev.backend.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService service;

    @Test
    void whenCreateValidUser_thenReturnNothing() {
        SignupDTO dto = new SignupDTO("test", "test@gmail.com", "123");

        assertThatCode(() -> service.createUser(dto)).doesNotThrowAnyException();
    }

    @Test
    void whenCreateExistingUser_thenThrowException() {
        when(userRepository.existsUserByEmail("test@gmail.com")).thenReturn(true);

        SignupDTO dto = new SignupDTO("test", "test@gmail.com", "123");

        assertThatThrownBy(() -> service.createUser(dto)).isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, times(1)).existsUserByEmail("test@gmail.com");
    }
}
