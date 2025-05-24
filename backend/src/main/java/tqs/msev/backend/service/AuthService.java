package tqs.msev.backend.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import tqs.msev.backend.dto.LoginDTO;
import tqs.msev.backend.dto.SignupDTO;
import tqs.msev.backend.entity.User;
import tqs.msev.backend.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public void createUser(SignupDTO dto) {
        String email = dto.getEmail();
        if (!userRepository.existsUserByEmail(email)) {
            throw new IllegalArgumentException("A user with that email already exists!");
        }

        String password = dto.getPassword();

        User user = User
                .builder()
                .name(dto.getName())
                .password(passwordEncoder.encode(password))
                .email(dto.getEmail())
                .build();

        userRepository.save(user);
    }

    public User authenticate(LoginDTO dto){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );
        return userRepository.findUserByEmail(dto.getEmail()).orElseThrow();
    }
}
